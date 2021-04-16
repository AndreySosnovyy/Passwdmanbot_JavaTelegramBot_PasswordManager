package ru.andreysosnovyy;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.config.BotConfig;
import ru.andreysosnovyy.states.BaseState;
import ru.andreysosnovyy.states.State;

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

            // добавление нового пользователя в базу данных
//            DBHandler handler = new DBHandler();
//            long userId = message.getFrom().getId();
//            String firstName = message.getFrom().getFirstName();
//            String lastName = message.getFrom().getLastName();
//            String username = message.getFrom().getUserName();
//            handler.addUserInfo(userId, firstName, lastName, username);

//            switch (update.getMessage().getText()) {
//                case "/help" -> state.commandHelp();
//                case "/settings" -> state.commandSettings();
//                case "/repository", "/repo" -> state.commandRepository();
//                case "/new" -> state.commandNew();
//                case "/cancel" -> state.commandCancel();
//                case "/generate", "/gen" -> state.commandGenerate();
//                default -> { // обработка сообщения, если это не команда
//
//                }
//            }
        }
    }
}
