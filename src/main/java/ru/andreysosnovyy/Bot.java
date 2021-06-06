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

        // инициализаци€ объектов дл€ работы с неподтвержденными парол€ми от хранилищ,
        // хранител€ активных сессий и билдера записей в базу данных
        if (repoPassWitness == null) repoPassWitness = new RepoPassWitness();
        if (activeSessionsKeeper == null) activeSessionsKeeper = new ActiveSessionsKeeper();
        if (dbPasswordRecordsBuilder == null)
            dbPasswordRecordsBuilder = new DBPasswordRecordsBuilder(activeSessionsKeeper);

        DBHandler handler = new DBHandler(); // хэнлдер дл€ работы с базой данных

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

            // состо€ние пользовател€
            String userState = handler.getUserState(update.getMessage().getChatId());

            // если пользователь не был найден в базе данных
            if (userState == null) {
                User user = User.builder()
                        .firstName(message.getFrom().getFirstName())
                        .lastName(message.getFrom().getLastName())
                        .username(message.getFrom().getUserName())
                        .id(message.getChatId())
                        .build();
                // добавить пользовател€ в базу данных
                handler.addNewUser(user);
                handler.addNewUserState(user);
                // отправить сообщение о необходимости создать мастер-пароль
                execute(new SendMessage(message.getChatId().toString(), Messages.CREATE_REPOSITORY_PASSWORD));
                return;
            }

            // поиск нужно обработчика по состо€нию пользовател€ дл€ полученного сообщени€
            switch (userState) {
                case UserState.Names.BASE_NO_REPOSITORY_PASSWORD -> {
                    long userId = message.getFrom().getId();

                    // ”даление парол€
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(message.getChatId().toString());
                    deleteMessage.setMessageId(message.getMessageId());
                    new DeleteMessageUtil(this, deleteMessage, 0).start();

                    if (repoPassWitness.checkIfExists(userId)) { // если есть
                        if (repoPassWitness.checkUnconfirmedRepoPass(userId, message.getText())) { // если совпал
                            String hashedPassword = Hash.getHash(message.getText());
                            handler.addRepositoryPasswordHash(userId, hashedPassword); // добавить пароль
                            handler.setUserState(userId, UserState.Names.BASE); // сменить состо€ние пользовател€
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
                    } else if (message.getText().equals(Messages.VIEW_REPOSITORY)) { // обработчик дл€ хранилища
                        // запросить мастер-пароль
                        handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_PASS);
                        execute(new SendMessage(message.getChatId().toString(), Messages.ENTER_REPO_PASS));
                    } else if (message.getText().equals(Messages.GENERATE_PASSWORD)) {
                        new GenerateWorker(this, update).start();
                    } else if (message.getText().equals(Messages.SETTINGS)) {
                        // обработчик дл€ настроек
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
                    // проверка хэша мастер-парол€
                    if (Hash.checkPassword(message.getText(), handler.getRepositoryPasswordHash(message.getChatId()))) {
                        activeSessionsKeeper.addActiveSession(message.getChatId()); // активировать сессию
                        handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_LIST); // сменить состо€ние
                        new RepositoryWorker(this, update).start();
                    } else {
                        execute(new SendMessage(message.getChatId().toString(), Messages.WRONG_REPO_PASS));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                    }
                    // удаление парол€
                    new DeleteMessageUtil(this, new DeleteMessage(
                            message.getChatId().toString(), message.getMessageId()), 0).start();
                }

                case UserState.Names.REPOSITORY_LIST -> {
                    if (activeSessionsKeeper.isActive(message.getChatId())) {
                        // обрабатываетс€ колбэком
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
                            new RepositoryWorker(this, update).start();
                        } else {
                            // проверка на повторение имена сервисов в базе
                            List<PasswordRecord> passwordRecords = handler.getUserPasswords(message.getChatId());
                            for (PasswordRecord passwordRecord : passwordRecords) {
                                if (passwordRecord.getServiceName().toLowerCase().equals(message.getText().toLowerCase())) {
                                    // совпадение в базе найдено
                                    execute(new SendMessage(message.getChatId().toString(), Messages.SERVICE_NAME_EXISTS));
                                    handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_LIST);
//                                    new RepositoryWorker(this, update).start();
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
                            new RepositoryWorker(this, update).start();
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
                            new RepositoryWorker(this, update).start();
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
                        new RepositoryWorker(this, update).start();
                    } else {
                        execute(new SendMessage(message.getChatId().toString(), Messages.SESSION_NOT_ACTIVE));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                        new BaseKeyboard(this, update).start();
                    }
                }

                case UserState.Names.SETTINGS_ENTER_REPO_PASS -> {
                    // проверка хэша мастер-парол€
                    if (Hash.checkPassword(message.getText(), handler.getRepositoryPasswordHash(message.getChatId()))) {
                        activeSessionsKeeper.addActiveSettingsSession(message.getChatId()); // активировать сессию
                        handler.setUserState(message.getChatId(), UserState.Names.SETTINGS); // сменить состо€ние
                        new SettingsKeyboard(this, update).start(); // запустить воркер
                    } else {
                        execute(new SendMessage(message.getChatId().toString(), Messages.WRONG_REPO_PASS));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                    }
                    // удаление парол€
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
                    } else { // сесси€ не активна
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
            }
        } else if (update.hasCallbackQuery()) { // колбэки используютс€ только в хранилище!

//            String userState = handler.getUserState(update.getMessage().getChatId()); // состо€ние пользовател€
//            String id = update.getCallbackQuery().getId();

            CallbackQuery callback = update.getCallbackQuery();

            // создание обновлени€ сообщени€ (заполн€тс€ будет в if)
//            EditMessageText editMessage = new EditMessageText();
//            editMessage.setMessageId(Integer.valueOf(id));
//            editMessage.setChatId(callback.getFrom().getId().toString());

            if (activeSessionsKeeper.isActive(callback.getFrom().getId())) {
                if (callback.getData().equals("exitButton")) {
                    handler.setUserState(callback.getFrom().getId(), UserState.Names.BASE);
                    new BaseKeyboard(this, update).start();

                } else if (callback.getData().equals("addButton")) {
                    dbPasswordRecordsBuilder.addRecord(callback.getFrom().getId());
                    handler.setUserState(callback.getFrom().getId(), UserState.Names.REPOSITORY_ADD_SERVICE_NAME);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(callback.getFrom().getId().toString());
                    sendMessage.setText(Messages.ENTER_SERVICE_NAME);
                    sendMessage.setReplyMarkup(BaseKeyboard.getCancelKeyboard());
                    execute(sendMessage);

                } else if (callback.getData().equals("searchButton")) {

                } else if (callback.getData().equals("beginPageButton") &&
                        activeSessionsKeeper.getPage(callback.getFrom().getId()) != 0) {
                    activeSessionsKeeper.setPage(callback.getFrom().getId(), 0);
//                    editMessage.setReplyMarkup(new PassListHandler(handler.getUserPasswords
//                            (callback.getFrom().getId()), 0).getInlineKeyboardMarkup());
                } else if (callback.getData().equals("previousPageButton") &&
                        activeSessionsKeeper.getPage(callback.getFrom().getId()) > 0) {
                    int currentPage = activeSessionsKeeper.getPage(callback.getFrom().getId()) + 1;
                    activeSessionsKeeper.setPage(callback.getFrom().getId(), currentPage - 1);
//                    editMessage.setReplyMarkup(new PassListHandler(handler.getUserPasswords
//                            (callback.getFrom().getId()), currentPage - 1).getInlineKeyboardMarkup());
                } else if (callback.getData().equals("currentPageButton")) {

                } else if (callback.getData().equals("nextPageButton")) {
                    int currentPage = activeSessionsKeeper.getPage(callback.getFrom().getId()) + 1;
                    if (RepositoryWorker.validatePage(callback.getFrom().getId(), currentPage + 1)) {
                        activeSessionsKeeper.setPage(callback.getFrom().getId(), currentPage + 1);
//                        editMessage.setReplyMarkup(new PassListHandler(handler.getUserPasswords
//                                (callback.getFrom().getId()), currentPage + 1).getInlineKeyboardMarkup());
                    }
                } else if (callback.getData().equals("lastPageButton")) {
                    int lastPage = RepositoryWorker.getLastPage(callback.getFrom().getId());
                    activeSessionsKeeper.setPage(callback.getFrom().getId(), lastPage);
//                    editMessage.setReplyMarkup(new PassListHandler(handler.getUserPasswords
//                            (callback.getFrom().getId()), lastPage).getInlineKeyboardMarkup());
                } else { // нажата кнопка записи хранилища

                }

                // обновление созданного и заполненного выше сообщени€
//                execute(editMessage);
                execute(new AnswerCallbackQuery(callback.getId()));

            } else { // сесси€ не активна
                execute(new SendMessage(callback.getFrom().getId().toString(), Messages.SESSION_NOT_ACTIVE));
                handler.setUserState(callback.getFrom().getId(), UserState.Names.BASE);
                new BaseKeyboard(this, update).start();
            }
        }
    }
}
