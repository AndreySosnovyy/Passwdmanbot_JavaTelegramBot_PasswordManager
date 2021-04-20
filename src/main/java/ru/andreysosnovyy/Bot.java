package ru.andreysosnovyy;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.config.BotConfig;
import ru.andreysosnovyy.tables.UserState;
import ru.andreysosnovyy.tables.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

        // логирование

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();

            // пользователь, от которого пришло сообщение
            User user = User.builder()
                    .id(message.getFrom().getId())
                    .firstName(message.getFrom().getFirstName())
                    .lastName(message.getFrom().getLastName())
                    .username(message.getFrom().getUserName())
                    .build();

            DBHandler handler = new DBHandler(); // хэнлдер для работы с базой данных
            ResultSet userResultSet = handler.getUser(user.getId()); // поиск пользователя в базе
            try {
                if (userResultSet.next()) { // пользователь найден в базе данных
                    ResultSet stateResultSet = handler.getUserState(user.getId()); // получить состояние чата пользователя
                    UserState userState = new UserState(stateResultSet);

//                    String pattern = "dd/MM/yyyy HH:mm:ss";
//                    DateFormat df = new SimpleDateFormat(pattern);
//                    System.out.println(df.format(userState.getDatetime()));

                } else { // пользователь является новым
                    handler.addNewUser(user); // добавить пользователя в базу данных
                    handler.addNewUserState(user); // добавить состояние чата пользователя в базу данных
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // todo: обработать запрос воркером
        }
    }
}
