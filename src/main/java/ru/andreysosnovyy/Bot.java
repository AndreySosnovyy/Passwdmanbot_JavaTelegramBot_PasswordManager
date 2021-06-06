package ru.andreysosnovyy;

import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.andreysosnovyy.config.BotConfig;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.tables.PasswordRecord;
import ru.andreysosnovyy.tables.User;
import ru.andreysosnovyy.tables.UserState;
import ru.andreysosnovyy.utils.*;
import ru.andreysosnovyy.workers.GenerateWorker;
import ru.andreysosnovyy.workers.RepositoryWorker;

import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.toIntExact;

public class Bot extends TelegramLongPollingBot {

    public RepoPassWitness repoPassWitness = null;
    public ActiveSessionsKeeper activeSessionsKeeper = null;
    public DBPasswordRecordsBuilder dbPasswordRecordsBuilder = null;

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        // инициализация объектов для работы с неподтвержденными паролями от хранилищ,
        // хранителя активных сессий и билдера записей в базу данных
        if (repoPassWitness == null) repoPassWitness = new RepoPassWitness();
        if (activeSessionsKeeper == null) activeSessionsKeeper = new ActiveSessionsKeeper();
        if (dbPasswordRecordsBuilder == null)
            dbPasswordRecordsBuilder = new DBPasswordRecordsBuilder(activeSessionsKeeper);

        DBHandler handler = new DBHandler(); // хэнлдер для работы с базой данных

        if (update.hasMessage() && update.getMessage().hasText()) {

            Message message = update.getMessage(); // сообщение из апдейта

            // если сообщение от администратора
            if (message.getFrom().getId() == BotConfig.ADMIN_ID) {
                switch (message.getText()) {
                    case "/clear_db" -> {
                        handler.clearDB();
                        return;
                    }

                    case "/stop_bot" -> {
                        System.exit(1);
                    }
                }
            }

            // состояние пользователя
            String userState = handler.getUserState(update.getMessage().getChatId());

            // если пользователь не был найден в базе данных
            if (userState == null) {
                User user = User.builder()
                        .firstName(message.getFrom().getFirstName())
                        .lastName(message.getFrom().getLastName())
                        .username(message.getFrom().getUserName())
                        .id(message.getChatId())
                        .build();
                // добавить пользователя в базу данных
                handler.addNewUser(user);
                handler.addNewUserState(user);
                // отправить сообщение о необходимости создать мастер-пароль
                execute(new SendMessage(message.getChatId().toString(), Messages.CREATE_REPOSITORY_PASSWORD));
                return;
            }

            // поиск нужно обработчика по состоянию пользователя для полученного сообщения
            switch (userState) {
                case UserState.Names.BASE_NO_REPOSITORY_PASSWORD -> {
                    long userId = message.getFrom().getId();

                    // Удаление пароля
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(message.getChatId().toString());
                    deleteMessage.setMessageId(message.getMessageId());
                    new DeleteMessageUtil(this, deleteMessage, 0).start();

                    if (repoPassWitness.checkIfExists(userId)) { // если есть
                        if (repoPassWitness.checkUnconfirmedRepoPass(userId, message.getText())) { // если совпал
                            String hashedPassword = Hash.getHash(message.getText());
                            handler.addRepositoryPasswordHash(userId, hashedPassword); // добавить пароль
                            handler.setUserState(userId, UserState.Names.BASE); // сменить состояние пользователя
                            execute(new SendMessage(String.valueOf(userId), Messages.CREATED_SUCCESSFUL)); // уведомить
                            new BaseKeyboard(this, update).start(); // вывести клавиатуру
                        } else { // есть, но не совпал
                            execute(new SendMessage(String.valueOf(userId), Messages.TRY_AGAIN_REPOSITORY_PASSWORD));
                        }
                    } else { // записи нет, надо добавить
                        repoPassWitness.addUnconfirmedRepoPass(message.getFrom().getId(), message.getText());
                        execute(new SendMessage(String.valueOf(userId), Messages.CONFIRM_REPOSITORY_PASSWORD));
                    }
                }

                case UserState.Names.BASE -> {
                    if (message.getText().equals("/start")) {
                        new BaseKeyboard(this, update).start();
                    } else if (message.getText().equals(Messages.VIEW_REPOSITORY)) { // обработчик для хранилища
                        // запросить мастер-пароль
                        handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_PASS);
                        execute(new SendMessage(message.getChatId().toString(), Messages.ENTER_REPO_PASS));
                    } else if (message.getText().equals(Messages.GENERATE_PASSWORD)) {
                        new GenerateWorker(this, update).start();
                    } else if (message.getText().equals(Messages.SETTINGS)) {
                        // обработчик для настроек
                        handler.setUserState(message.getChatId(), UserState.Names.SETTINGS_ENTER_REPO_PASS);
                        execute(new SendMessage(message.getChatId().toString(), Messages.ENTER_REPO_PASS));
                    } else {
                        // если пришел пароль, то его надо будет удалить
                        if (PasswordGenerator.checkIfPassword(message.getText())) {
                            new DeleteMessageUtil(this, new DeleteMessage(
                                    message.getChatId().toString(), message.getMessageId()),
                                    15_000).start();
                        }
                        // вернуть стартовую клавиатуру
                        new BaseKeyboard(this, update).start();
                    }
                }

                case UserState.Names.REPOSITORY_PASS -> {
                    // проверка хэша мастер-пароля
                    if (Hash.checkPassword(message.getText(), handler.getRepositoryPasswordHash(message.getChatId()))) {
                        activeSessionsKeeper.addActiveSession(message.getChatId()); // активировать сессию
                        handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_LIST); // сменить состояние
                        new RepositoryWorker(this, update).start(null);
                    } else {
                        execute(new SendMessage(message.getChatId().toString(), Messages.WRONG_REPO_PASS));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                    }
                    // удаление пароля
                    new DeleteMessageUtil(this, new DeleteMessage(
                            message.getChatId().toString(), message.getMessageId()), 0).start();
                }

                case UserState.Names.REPOSITORY_LIST -> {
                    if (activeSessionsKeeper.isActive(message.getChatId())) {
                        // обрабатывается колбэком
                    } else {
                        execute(new SendMessage(message.getChatId().toString(), Messages.SESSION_NOT_ACTIVE));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                        new BaseKeyboard(this, update).start();
                    }
                }

                case UserState.Names.REPOSITORY_ADD_SERVICE_NAME -> {
                    if (activeSessionsKeeper.isActive(message.getChatId())) {
                        if (message.getText().equals(Messages.CANCEL)) {
                            dbPasswordRecordsBuilder.removeRecord(message.getChatId());
                            handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_LIST);
                            new RepositoryWorker(this, update).start(null);
                        } else {
                            // проверка на повторение имена сервисов в базе
                            List<PasswordRecord> passwordRecords = handler.getUserPasswords(message.getChatId());
                            for (PasswordRecord passwordRecord : passwordRecords) {
                                if (passwordRecord.getServiceName().toLowerCase().equals(message.getText().toLowerCase())) {
                                    // совпадение в базе найдено
                                    execute(new SendMessage(message.getChatId().toString(), Messages.SERVICE_NAME_EXISTS));
                                    handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_LIST);
                                    return;
                                }
                            }

                            dbPasswordRecordsBuilder.setServiceName(message.getChatId(), message.getText());
                            handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_ADD_LOGIN);
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(message.getChatId().toString());
                            sendMessage.setText(Messages.ENTER_LOGIN);
                            sendMessage.setReplyMarkup(BaseKeyboard.getCancelKeyboard());
                            execute(sendMessage);
                            new DeleteMessageUtil(this, new DeleteMessage(
                                    message.getChatId().toString(), message.getMessageId()), 60_000).start();
                        }
                    } else {
                        execute(new SendMessage(message.getChatId().toString(), Messages.SESSION_NOT_ACTIVE));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                        new BaseKeyboard(this, update).start();
                    }
                }

                case UserState.Names.REPOSITORY_ADD_LOGIN -> {
                    if (activeSessionsKeeper.isActive(message.getChatId())) {
                        if (message.getText().equals(Messages.CANCEL)) {
                            dbPasswordRecordsBuilder.removeRecord(message.getChatId());
                            handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_LIST);
                            new RepositoryWorker(this, update).start(null);
                        } else {
                            dbPasswordRecordsBuilder.setLogin(message.getChatId(), message.getText());
                            handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_ADD_PASSWORD);
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(message.getChatId().toString());
                            sendMessage.setText(Messages.ENTER_PASSWORD);
                            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                            replyKeyboardMarkup.setResizeKeyboard(true);
                            replyKeyboardMarkup.setOneTimeKeyboard(true);
                            ArrayList<KeyboardRow> newKeyboard = new ArrayList<>();
                            KeyboardRow newFirstKeyboardRow = new KeyboardRow();
                            newFirstKeyboardRow.add(Messages.GENERATE_PASSWORD);
                            newFirstKeyboardRow.add(Messages.CANCEL);
                            newKeyboard.add(newFirstKeyboardRow);
                            replyKeyboardMarkup.setKeyboard(newKeyboard);
                            sendMessage.setReplyMarkup(replyKeyboardMarkup);
                            execute(sendMessage);
                            new DeleteMessageUtil(this, new DeleteMessage(
                                    message.getChatId().toString(), message.getMessageId()), 60_000).start();
                        }
                    } else {
                        execute(new SendMessage(message.getChatId().toString(), Messages.SESSION_NOT_ACTIVE));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                        new BaseKeyboard(this, update).start();
                    }
                }

                case UserState.Names.REPOSITORY_ADD_PASSWORD -> {
                    if (activeSessionsKeeper.isActive(message.getChatId())) {
                        if (message.getText().equals(Messages.CANCEL)) {
                            dbPasswordRecordsBuilder.removeRecord(message.getChatId());
                            handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_LIST);
                            new RepositoryWorker(this, update).start(null);
                            return;
                        } else if (message.getText().equals(Messages.GENERATE_PASSWORD)) {
                            dbPasswordRecordsBuilder.setPassword(message.getChatId(),
                                    GenerateWorker.GeneratePassword(16, true, true, true, true));
                        } else {
                            dbPasswordRecordsBuilder.setPassword(message.getChatId(), message.getText());
                            new DeleteMessageUtil(this, new DeleteMessage(
                                    message.getChatId().toString(), message.getMessageId()), 0).start();
                        }
                        handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_ADD_COMMENT);
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(message.getChatId().toString());
                        sendMessage.setText(Messages.ENTER_COMMENT);
                        sendMessage.setReplyMarkup(BaseKeyboard.getCancelKeyboard());
                        execute(sendMessage);
                    } else {
                        execute(new SendMessage(message.getChatId().toString(), Messages.SESSION_NOT_ACTIVE));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                        new BaseKeyboard(this, update).start();
                    }
                }

                case UserState.Names.REPOSITORY_ADD_COMMENT -> {
                    if (activeSessionsKeeper.isActive(message.getChatId())) {
                        if (message.getText().equals(Messages.CANCEL)) {
                            dbPasswordRecordsBuilder.removeRecord(message.getChatId());
                        } else {
                            dbPasswordRecordsBuilder.setComment(message.getChatId(), message.getText());
                            // добавить запись в базу данных
                            if (handler.addPasswordRecord(dbPasswordRecordsBuilder.buildAndGet(message.getChatId()))) {
                                execute(new SendMessage(message.getChatId().toString(), Messages.RECORD_SUCCESSFULLY_ADDED));
                            } else {
                                execute(new SendMessage(message.getChatId().toString(), Messages.RECORD_NOT_ADDED));
                            }
                            new DeleteMessageUtil(this, new DeleteMessage(
                                    message.getChatId().toString(), message.getMessageId()), 60_000).start();
                        }
                        handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_LIST);
                        new RepositoryWorker(this, update).start(null);
                    } else {
                        execute(new SendMessage(message.getChatId().toString(), Messages.SESSION_NOT_ACTIVE));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                        new BaseKeyboard(this, update).start();
                    }
                }

                case UserState.Names.SETTINGS_ENTER_REPO_PASS -> {
                    // проверка хэша мастер-пароля
                    if (Hash.checkPassword(message.getText(), handler.getRepositoryPasswordHash(message.getChatId()))) {
                        activeSessionsKeeper.addActiveSettingsSession(message.getChatId()); // активировать сессию
                        handler.setUserState(message.getChatId(), UserState.Names.SETTINGS); // сменить состояние
                        new SettingsKeyboard(this, update).start(); // запустить воркер
                    } else {
                        execute(new SendMessage(message.getChatId().toString(), Messages.WRONG_REPO_PASS));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                    }
                    // удаление пароля
                    new DeleteMessageUtil(this, new DeleteMessage(
                            message.getChatId().toString(), message.getMessageId()), 0).start();
                }

                case UserState.Names.SETTINGS -> {
                    if (activeSessionsKeeper.isActiveSettings(message.getChatId())) {
                        activeSessionsKeeper.prolongSettingsSession(message.getChatId());
                        if (message.getText().equals(Messages.CHANGE_MASTER_PASS)) {
                            execute(new SendMessage(message.getChatId().toString(), Messages.ENTER_NEW_REPO_PASS));
                            handler.setUserState(message.getChatId(), UserState.Names.SETTINGS_CHANGE_MASTER_PASS);
                        } else if (message.getText().equals(Messages.DELETE_REPO)) {
                            handler.setUserState(message.getChatId(), UserState.Names.SETTINGS_DELETE_REPO);
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(message.getChatId().toString());
                            sendMessage.setText(Messages.CONFIRM_DELETE_REPO);
                            ReplyKeyboardMarkup newreplyKeyboardMarkup = new ReplyKeyboardMarkup();
                            newreplyKeyboardMarkup.setResizeKeyboard(true);
                            newreplyKeyboardMarkup.setOneTimeKeyboard(true);
                            ArrayList<KeyboardRow> newkeyboard = new ArrayList<>();
                            KeyboardRow newfirstKeyboardRow = new KeyboardRow();
                            newfirstKeyboardRow.add(Messages.CANCEL);
                            newfirstKeyboardRow.add(Messages.YES);
                            newkeyboard.add(newfirstKeyboardRow);
                            newreplyKeyboardMarkup.setKeyboard(newkeyboard);
                            sendMessage.setReplyMarkup(newreplyKeyboardMarkup);
                            execute(sendMessage);
                        } else if (message.getText().equals(Messages.RESTORE_MASTER_PASS)) {
                            execute(new SendMessage(message.getChatId().toString(), Messages.CONTACT_ADMIN));
                        } else if (message.getText().equals(Messages.EXIT_SETTINGS)) {
                            handler.setUserState(message.getChatId(), UserState.Names.BASE);
                            new BaseKeyboard(this, update).start();
                        }
                    } else { // сессия не активна
                        execute(new SendMessage(message.getChatId().toString(), Messages.SESSION_NOT_ACTIVE));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                        new BaseKeyboard(this, update).start();
                    }
                }

                case UserState.Names.SETTINGS_CHANGE_MASTER_PASS -> {
                    activeSessionsKeeper.prolongSettingsSession(message.getChatId());
                    handler.changeRepoPass(message.getChatId(), Hash.getHash(message.getText()));
                    handler.setUserState(message.getChatId(), UserState.Names.SETTINGS);
                    execute(new SendMessage(message.getChatId().toString(), Messages.REPO_PASS_CHANGED));
                    new SettingsKeyboard(this, update).start();
                }

                case UserState.Names.SETTINGS_DELETE_REPO -> {
                    activeSessionsKeeper.prolongSettingsSession(message.getChatId());
                    if (message.getText().equals(Messages.YES)) {
                        handler.deleteRepo(message.getChatId());
                        execute(new SendMessage(message.getChatId().toString(), Messages.SEE_YOU));
                    } else if (message.getText().equals(Messages.CANCEL)) {
                        handler.setUserState(message.getChatId(), UserState.Names.SETTINGS);
                        new SettingsKeyboard(this, update).start();
                    }
                }

                case UserState.Names.REPOSITORY_SEARCH -> {
                    activeSessionsKeeper.prolongSettingsSession(message.getChatId());
                    handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_LIST);
                    new RepositoryWorker(this, update).start(message.getText());
                }

                case UserState.Names.REPOSITORY_RECORD -> {
                    if (message.getText().equals(Messages.BACK)) {
                        activeSessionsKeeper.prolongSession(message.getChatId());
                        WatchingRecordsController.remove(message.getChatId());
                        handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_LIST);
                        new RepositoryWorker(this, update).start(null);

                    } else if (message.getText().equals(Messages.EDIT_RECORD_PASSWORD)) {
                        execute(new SendMessage(message.getChatId().toString(), Messages.ENTER_PASSWORD));
                        handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_EDIT_RECORD_PASSWORD);

                    } else if (message.getText().equals(Messages.EDIT_RECORD_COMMENT)) {
                        execute(new SendMessage(message.getChatId().toString(), Messages.ENTER_COMMENT));
                        handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_EDIT_RECORD_COMMENT);

                    } else if (message.getText().equals(Messages.DELETE_RECORD)) {
                        activeSessionsKeeper.prolongSession(message.getChatId());
                        handler.deleteRecord(message.getChatId(), WatchingRecordsController.getServiceName(message.getChatId()));
                        WatchingRecordsController.remove(message.getChatId());
                        handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_LIST);
                        new RepositoryWorker(this, update).start(null);
                    }
                }

                case UserState.Names.REPOSITORY_EDIT_RECORD_PASSWORD -> {
                    activeSessionsKeeper.prolongSession(message.getChatId());
                    handler.editRecordPassword(message.getChatId(),
                            WatchingRecordsController.getServiceName(message.getChatId()), message.getText());
                    handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_LIST);
                    new DeleteMessageUtil(this, new DeleteMessage(message.getChatId().toString(), message.getMessageId()), 0).start();
                    new RepositoryWorker(this, update).start(null);
                    WatchingRecordsController.remove(message.getChatId());
                }

                case UserState.Names.REPOSITORY_EDIT_RECORD_COMMENT -> {
                    activeSessionsKeeper.prolongSession(message.getChatId());
                    handler.editRecordComment(message.getChatId(),
                            WatchingRecordsController.getServiceName(message.getChatId()), message.getText());
                    handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_LIST);
                    new RepositoryWorker(this, update).start(null);
                    new DeleteMessageUtil(this, new DeleteMessage(message.getChatId().toString(), message.getMessageId()), 0).start();
                    WatchingRecordsController.remove(message.getChatId());
                }
            }

        } else if (update.hasCallbackQuery()) { // колбэки используются только в хранилище!

            CallbackQuery callback = update.getCallbackQuery();
            String callbackData = callback.getData();
            long messageId = callback.getMessage().getMessageId();
            long chatId = callback.getMessage().getChatId();

            // создание обновления сообщения (заполнятся будет в if)
            EditMessageText editMessage = new EditMessageText();
            editMessage.setMessageId(toIntExact(messageId));
            editMessage.setChatId(Long.toString(chatId));
            editMessage.setText(Messages.USE_REPO_MENU);

            if (activeSessionsKeeper.isActive(chatId)) {

                if (callbackData.equals("exitButton")) {
                    execute(new DeleteMessage(String.valueOf(chatId), (int) messageId));
                    handler.setUserState(chatId, UserState.Names.BASE);
                    new BaseKeyboard(this, update).start();
                    execute(editMessage); // обновление сообщения

                } else if (callbackData.equals("addButton")) {
                    dbPasswordRecordsBuilder.addRecord(chatId);
                    handler.setUserState(chatId, UserState.Names.REPOSITORY_ADD_SERVICE_NAME);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(String.valueOf(chatId));
                    sendMessage.setText(Messages.ENTER_SERVICE_NAME);
                    sendMessage.setReplyMarkup(BaseKeyboard.getCancelKeyboard());
                    execute(sendMessage);

                } else if (callbackData.equals("searchButton")) {
                    execute(new SendMessage(String.valueOf(chatId), Messages.ENTER_SEARCH_STRING));
                    handler.setUserState(chatId, UserState.Names.REPOSITORY_SEARCH);
                    execute(new DeleteMessage(String.valueOf(chatId), (int) messageId));

                } else if (callbackData.equals("beginPageButton")) {
                    if (activeSessionsKeeper.getPage(chatId) != 0) {
                        activeSessionsKeeper.setPage(chatId, 0);
                        editMessage.setReplyMarkup(new PassListHandler(handler.getUserPasswords
                                (chatId), 0).getInlineKeyboardMarkup(null));
                        execute(editMessage); // обновление сообщения
                    } else {
                        execute(new AnswerCallbackQuery(callback.getId()));
                    }

                } else if (callbackData.equals("previousPageButton")) {
                    if (activeSessionsKeeper.getPage(chatId) > 0) {
                        int currentPage = activeSessionsKeeper.getPage(chatId);
                        activeSessionsKeeper.setPage(chatId, currentPage - 1);
                        editMessage.setReplyMarkup(new PassListHandler(handler.getUserPasswords
                                (chatId), currentPage - 1).getInlineKeyboardMarkup(null));
                        execute(editMessage); // обновление сообщения
                    } else {
                        execute(new AnswerCallbackQuery(callback.getId()));
                    }

                } else if (callbackData.equals("currentPageButton")) {
                    execute(new AnswerCallbackQuery(callback.getId()));

                } else if (callbackData.equals("nextPageButton")) {
                    int currentPage = activeSessionsKeeper.getPage(chatId);
                    if (RepositoryWorker.validatePage(chatId, currentPage + 1)) {
                        activeSessionsKeeper.setPage(chatId, currentPage + 1);
                        editMessage.setReplyMarkup(new PassListHandler(handler.getUserPasswords
                                (chatId), currentPage + 1).getInlineKeyboardMarkup(null));
                        execute(editMessage); // обновление сообщения
                    }

                } else if (callbackData.equals("lastPageButton")) {
                    int lastPage = RepositoryWorker.getLastPage(chatId);
                    activeSessionsKeeper.setPage(chatId, lastPage);
                    editMessage.setReplyMarkup(new PassListHandler(handler.getUserPasswords
                            (chatId), lastPage).getInlineKeyboardMarkup(null));
                    execute(editMessage); // обновление сообщения

                } else { // нажата кнопка записи хранилища
                    handler.setUserState(chatId, UserState.Names.REPOSITORY_RECORD);
                    execute(new DeleteMessage(String.valueOf(chatId), (int) messageId));
                    WatchingRecordsController.add(chatId, callbackData);
                    WatchingRecordsController.showKeyboard(this, chatId, callbackData);
                }

            } else { // сессия не активна
                execute(new SendMessage(String.valueOf(chatId), Messages.SESSION_NOT_ACTIVE));
                handler.setUserState(chatId, UserState.Names.BASE);
                new BaseKeyboard(this, update).start();
            }
        }
    }
}
