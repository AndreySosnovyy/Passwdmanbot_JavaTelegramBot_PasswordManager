package ru.andreysosnovyy;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.config.BotConfig;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.tables.User;
import ru.andreysosnovyy.tables.UserState;
import ru.andreysosnovyy.utils.PasswordGenerator;
import ru.andreysosnovyy.workers.BaseKeyboardWorker;
import ru.andreysosnovyy.workers.DeleteMessageWorker;
import ru.andreysosnovyy.workers.GenerateWorker;

public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // todo: логирование

        if (update.hasMessage() && update.getMessage().hasText()) {
            DBHandler handler = new DBHandler(); // хэнлдер для работы с базой данных
            String userState; // состояние пользователя, отправившего сообщение
            Message message = update.getMessage(); // сообщение из апдейта

            // получить текущее состояние чата пользователя
            userState = handler.getUserState(update.getMessage().getChatId());
            if (userState == null) {
                // пользователь не был найден в базе данных -> добавить
                User user = User.builder()
                        .firstName(message.getFrom().getFirstName())
                        .lastName(message.getFrom().getLastName())
                        .username(message.getFrom().getUserName())
                        .id(message.getChatId())
                        .build();
                handler.addNewUser(user);
                handler.addNewUserState(user);
                userState = UserState.Names.BASE;
            }

            switch (userState) { // поиск нужно обработчика по состоянию пользователя для полученного сообщения
                case UserState.Names.BASE -> {
                    switch (message.getText()) {
                        case "/start" -> {
                            new BaseKeyboardWorker(this, update).start();
                        }

                        case Messages.VIEW_REPOSITORY -> {
                            // обработчик для хранилища
                        }

                        case Messages.GENERATE_PASSWORD -> {
                            new GenerateWorker(this, update).start();
                        }

                        case Messages.SETTINGS -> {
                            // обработчик для настроек
                        }

                        default -> {
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
                }
            }
        }
    }
}

