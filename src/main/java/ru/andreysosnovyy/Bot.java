package ru.andreysosnovyy;

import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.config.BotConfig;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.tables.User;
import ru.andreysosnovyy.tables.UserState;
import ru.andreysosnovyy.utils.*;
import ru.andreysosnovyy.workers.BaseKeyboardWorker;
import ru.andreysosnovyy.utils.DeleteMessageWorker;
import ru.andreysosnovyy.workers.GenerateWorker;
import ru.andreysosnovyy.workers.RepositoryWorker;

import java.sql.SQLException;

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
        // todo: логирование

        // инициализация объектов для работы с неподтвержденными паролями от хранилищ,
        // хранителя активных сессий и билдера записей в базу данных
        if (repoPassWitness == null) repoPassWitness = new RepoPassWitness();
        if (activeSessionsKeeper == null) activeSessionsKeeper = new ActiveSessionsKeeper();
        if (dbPasswordRecordsBuilder == null) dbPasswordRecordsBuilder = new DBPasswordRecordsBuilder();

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

            // поиск нужно обработчика по состоянию пользователя для полученного сообщения
            switch (userState) {
                case UserState.Names.BASE_NO_REPOSITORY_PASSWORD -> {
                    long userId = message.getFrom().getId();

                    // Удаление пароля
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(message.getChatId().toString());
                    deleteMessage.setMessageId(message.getMessageId());
                    new DeleteMessageWorker(this, deleteMessage, 0).start();

                    if (repoPassWitness.checkIfExists(userId)) { // если есть
                        if (repoPassWitness.checkUnconfirmedRepoPass(userId, message.getText())) { // если совпал
                            String hashedPassword = Hash.getHash(message.getText());
                            handler.addRepositoryPasswordHash(userId, hashedPassword); // добавить пароль
                            handler.setUserState(userId, UserState.Names.BASE); // сменить состояние пользователя
                            execute(new SendMessage(String.valueOf(userId), Messages.CREATED_SUCCESSFUL)); // уведомить
                            new BaseKeyboardWorker(this, update).start(); // вывести клавиатуру
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
                        new BaseKeyboardWorker(this, update).start();
                    } else if (message.getText().equals(Messages.VIEW_REPOSITORY)) { // обработчик для хранилища
                        // запросить мастер-пароль
                        handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_PASS);
                        execute(new SendMessage(message.getChatId().toString(), Messages.ENTER_REPO_PASS));
                    } else if (message.getText().equals(Messages.GENERATE_PASSWORD)) {
                        new GenerateWorker(this, update).start();
                    } else if (message.getText().equals(Messages.SETTINGS)) {
                        // обработчик для настроек
                        // todo: 1) изменить мастер пароль
                        // todo: 2) удалить хранилище
                    } else {
                        // если пришел пароль, то его надо будет удалить
                        if (PasswordGenerator.checkIfPassword(message.getText())) {
                            new DeleteMessageWorker(this, new DeleteMessage(
                                    message.getChatId().toString(), message.getMessageId()),
                                    15_000).start();
                        }
                        // вернуть стартовую клавиатуру
                        new BaseKeyboardWorker(this, update).start();
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
                    new DeleteMessageWorker(this, new DeleteMessage(
                            message.getChatId().toString(), message.getMessageId()), 0).start();
                }

                case UserState.Names.REPOSITORY_LIST -> {
                    // проверка на активность сессии
                    if (activeSessionsKeeper.isActive(message.getChatId())) {
                        if (message.getText().equals(Messages.ADD_NEW_PASSWORD)) {
                            dbPasswordRecordsBuilder.addRecord(message.getChatId());
                            handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_ADD_SERVICE_NAME);
                            execute(new SendMessage(message.getChatId().toString(), Messages.ENTER_SERVICE_NAME));
                        } else if (message.getText().equals(Messages.SEARCH)) {

                        } else if (message.getText().equals(Messages.BACK)) {
                            handler.setUserState(message.getChatId(), UserState.Names.BASE);
                            new BaseKeyboardWorker(this, update).start();
                        } else {

                        }
                    } else {
                        execute(new SendMessage(message.getChatId().toString(), Messages.SESSION_NOT_ACTIVE));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                        new BaseKeyboardWorker(this, update).start();
                    }
                }

                case UserState.Names.REPOSITORY_SEARCH -> {

                }

                // todo : кастомная клавиатура ("Отмена")
                case UserState.Names.REPOSITORY_ADD_SERVICE_NAME -> {
                    try {
                        dbPasswordRecordsBuilder.setServiceName(message.getChatId(), message.getText());
                    } catch (DBPasswordRecordsBuilder.NoActiveSessionFoundException e) {
                        execute(new SendMessage(message.getChatId().toString(), Messages.TIME_RAN_OUT));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                        new BaseKeyboardWorker(this, update).start();
                        return;
                    }
                    handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_ADD_LOGIN);
                    execute(new SendMessage(message.getChatId().toString(), Messages.ENTER_LOGIN));
                }

                // todo : кастомная клавиатура ("Отмена", "Сгенерировать пароль")
                case UserState.Names.REPOSITORY_ADD_LOGIN -> {
                    try {
                        dbPasswordRecordsBuilder.setLogin(message.getChatId(), message.getText());
                    } catch (DBPasswordRecordsBuilder.NoActiveSessionFoundException e) {
                        execute(new SendMessage(message.getChatId().toString(), Messages.TIME_RAN_OUT));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                        new BaseKeyboardWorker(this, update).start();
                        return;
                    }
                    handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_ADD_PASSWORD);
                    execute(new SendMessage(message.getChatId().toString(), Messages.ENTER_PASSWORD));
                }

                // todo: кастомная клавиатура ("Отмена") + удалять пароль
                case UserState.Names.REPOSITORY_ADD_PASSWORD -> {
                    try {
                        dbPasswordRecordsBuilder.setPassword(message.getChatId(), message.getText());
                    } catch (DBPasswordRecordsBuilder.NoActiveSessionFoundException e) {
                        execute(new SendMessage(message.getChatId().toString(), Messages.TIME_RAN_OUT));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                        new BaseKeyboardWorker(this, update).start();
                        return;
                    }
                    handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_ADD_COMMENT);
                    execute(new SendMessage(message.getChatId().toString(), Messages.ENTER_COMMENT));
                }

                // todo: кастомная клавиатура ("Отмена")
                case UserState.Names.REPOSITORY_ADD_COMMENT -> {
                    try {
                        if (!message.getText().equals("-")) {
                            dbPasswordRecordsBuilder.setComment(message.getChatId(), message.getText());
                        } else {
                            dbPasswordRecordsBuilder.setComment(message.getChatId(), "");
                        }
                    } catch (DBPasswordRecordsBuilder.NoActiveSessionFoundException e) {
                        execute(new SendMessage(message.getChatId().toString(), Messages.TIME_RAN_OUT));
                        handler.setUserState(message.getChatId(), UserState.Names.BASE);
                        new BaseKeyboardWorker(this, update).start();
                        return;
                    }
                    handler.setUserState(message.getChatId(), UserState.Names.REPOSITORY_LIST);

                    // добавить запись в базу данных
                    try {
                        handler.addPasswordRecord(dbPasswordRecordsBuilder.buildAndGet(message.getChatId()));
                    } catch (SQLException e) {
                        execute(new SendMessage(message.getChatId().toString(), Messages.RECORD_NOT_ADDED));
                        return;
                    }
                    execute(new SendMessage(message.getChatId().toString(), Messages.RECORD_SUCCESSFULLY_ADDED));
                    new RepositoryWorker(this, update).start();
                }
            }
        }
    }
}


