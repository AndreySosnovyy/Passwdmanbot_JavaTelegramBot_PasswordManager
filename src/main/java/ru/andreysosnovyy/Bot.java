package ru.andreysosnovyy;

import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.andreysosnovyy.config.BotConfig;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.tables.User;
import ru.andreysosnovyy.tables.UserState;
import ru.andreysosnovyy.utils.*;
import ru.andreysosnovyy.workers.GenerateWorker;
import ru.andreysosnovyy.workers.RepositoryWorker;

import java.util.ArrayList;

public class Bot extends TelegramLongPollingBot {

    RepoPassWitness repoPassWitness = null;
    ActiveSessionsKeeper activeSessionsKeeper = null;
    DBPasswordRecordsBuilder dbPasswordRecordsBuilder = null;

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

        if (update.hasMessage() && update.getMessage().hasText()) {

            DBHandler handler = new DBHandler(); // хэнлдер для работы с базой данных
            String userState = handler.getUserState(update.getMessage().getChatId()); // состояние пользователя
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

            // разметка для клавиатуры с кнопкой "отмена"
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            ArrayList<KeyboardRow> keyboard = new ArrayList<>();
            KeyboardRow firstKeyboardRow = new KeyboardRow();
            firstKeyboardRow.add(Messages.CANCEL);
            keyboard.add(firstKeyboardRow);
            replyKeyboardMarkup.setKeyboard(keyboard);

            // поиск нужно обработчика по состоянию пользователя для полученного сообщения
            switch (userState) {
                case UserState.Names.BASE_NO_REPOSITORY_PASSWORD -> {
                    long userId = message.getFrom().getId();

                    // Удаление пароля
                    org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage deleteMessage = new org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage();
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
                        handler.setUserState(message.getChatId(), UserState.Names.SETTINGS);
                        new SettingsKeyboard(this, update).start();
                    } else {
                        // если пришел пароль, то его надо будет удалить
                        if (PasswordGenerator.checkIfPassword(message.getText())) {
                            new DeleteMessageUtil(this, new org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage(
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
                        new RepositoryWorker(this, update).start(); // запустить воркер
                    } else {
                        execute(new SendMessage(message.getChatId().toString(), Messages.WRONG_REPO_PASS));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                    }
                    // удаление пароля
                    new DeleteMessageUtil(this, new org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage(
                            message.getChatId().toString(), message.getMessageId()), 0).start();
                }

                case UserState.Names.REPOSITORY_LIST -> {
                    // проверка на активность сессии
                    if (activeSessionsKeeper.isActive(message.getChatId())) {
                        if (message.getText().equals(Messages.ADD_NEW_PASSWORD)) {
                            dbPasswordRecordsBuilder.addRecord(message.getChatId());
                            handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_ADD_SERVICE_NAME);
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(message.getChatId().toString());
                            sendMessage.setText(Messages.ENTER_SERVICE_NAME);
                            sendMessage.setReplyMarkup(replyKeyboardMarkup);
                            execute(sendMessage);
                        } else if (message.getText().equals(Messages.SEARCH)) {

                        } else if (message.getText().equals(Messages.EXIT_REPO)) {
                            handler.setUserState(message.getChatId(), UserState.Names.BASE);
                            new BaseKeyboard(this, update).start();
                        } else {

                        }
                    } else {
                        execute(new SendMessage(message.getChatId().toString(), Messages.SESSION_NOT_ACTIVE));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                        new BaseKeyboard(this, update).start();
                    }
                }

                case UserState.Names.REPOSITORY_SEARCH -> {

                }

                case UserState.Names.REPOSITORY_ADD_SERVICE_NAME -> {
                    if (activeSessionsKeeper.isActive(message.getChatId())) {
                        if (message.getText().equals(Messages.CANCEL)) {
                            dbPasswordRecordsBuilder.removeRecord(message.getChatId());
                            handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_LIST);
                            new RepositoryWorker(this, update).start();
                        } else {
                            dbPasswordRecordsBuilder.setServiceName(message.getChatId(), message.getText());
                            handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_ADD_LOGIN);
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(message.getChatId().toString());
                            sendMessage.setText(Messages.ENTER_LOGIN);
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
                        sendMessage.setReplyMarkup(replyKeyboardMarkup);
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

                case UserState.Names.SETTINGS -> {
                    if (message.getText().equals(Messages.CHANGE_MASTER_PASS)) {

                    } else if (message.getText().equals(Messages.DELETE_REPO)) {

                    } else if (message.getText().equals(Messages.RESTORE_MASTER_PASS)) {

                    } else {

                    }
                }
            }
        }
    }
}
