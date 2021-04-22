package ru.andreysosnovyy;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.andreysosnovyy.config.BotConfig;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.tables.User;
import ru.andreysosnovyy.tables.UserState;
import ru.andreysosnovyy.workers.BaseStateWorker;

import java.sql.ResultSet;
import java.sql.SQLException;

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
            Message message = update.getMessage();
            User user = User.builder() // пользователь, от которого пришло сообщение
                    .id(message.getFrom().getId())
                    .firstName(message.getFrom().getFirstName())
                    .lastName(message.getFrom().getLastName())
                    .username(message.getFrom().getUserName())
                    .build();

            switch (message.getText()) {
                case "/start" -> {
                    try {
                        ResultSet userResultSet = handler.getUser(user.getId()); // поиск пользователя в базе
                        if (!userResultSet.next()) { // пользователь не найден в базе данных
                            handler.addNewUser(user); // добавить пользователя в базу данных
                            handler.addNewUserState(user); // добавить состояние чата пользователя в базу данных
                            new BaseStateWorker(this, update).start(); // запустить обработчика
                        } else {
                            try {
                                execute(new SendMessage(message.getChatId().toString(), "Повторите ввод"));
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                case "/cancel" -> {
                    // todo: убрать последствия всех предыдущих действий
                    handler.setUserState(message.getChatId(), UserState.StateNames.BASE_STATE);
                    new BaseStateWorker(this, update).start(); // перейти в начальное состояние
                }

                case Messages.GENERATE_PASSWORD -> {
                }
            }


        }
    }
}
