package ru.andreysosnovyy;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.config.BotConfig;
import ru.andreysosnovyy.tables.User;

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

        // логирование

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();

            // todo: добавить таблицу "users_states" в базу
            //  (id, состояние, время последнего сообщения)

            // todo: проверка на наличие пользователя в базе
            //  если есть, то получить его состояние
            //  иначе добавить его в таблицу users_info (+) и users_states (-)

            // пользователь, от которого пришло сообщение
            User user = User.builder()
                    .id(message.getFrom().getId())
                    .firstName(message.getFrom().getFirstName())
                    .lastName(message.getFrom().getLastName())
                    .username(message.getFrom().getUserName())
                    .build();

            DBHandler handler = new DBHandler(); // хэнлдер для работы с базой данных
            ResultSet resultSet = handler.getUser(user.getId());
            try {
                if (resultSet.next()) { // пользователь найден в базе данных
                    // todo: получить состояние чата пользователя
                    // todo: обработать запрос воркером
                } else { // пользователь является новым
                    handler.addNewUser(user); // добавить пользователя в базу данных
                    // todo: добавить состояние чата пользователя в базу данных
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
