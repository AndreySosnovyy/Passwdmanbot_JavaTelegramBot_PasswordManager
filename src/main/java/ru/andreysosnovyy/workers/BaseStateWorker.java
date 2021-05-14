package ru.andreysosnovyy.workers;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.andreysosnovyy.Bot;
import ru.andreysosnovyy.DBHandler;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.tables.User;
import ru.andreysosnovyy.tables.UserState;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BaseStateWorker extends Worker {

    public BaseStateWorker(Bot bot, Update update) {
        super(bot, update);
    }

    DBHandler handler = new DBHandler();

    @Override
    public void run() {

        Message message = update.getMessage();
        User user = User.builder() // пользователь, от которого пришло сообщение
                .id(message.getFrom().getId())
                .firstName(message.getFrom().getFirstName())
                .lastName(message.getFrom().getLastName())
                .username(message.getFrom().getUserName())
                .build();

        ResultSet userResultSet = handler.getUser(user.getId()); // поиск пользователя в базе
        try {
            if (!userResultSet.next()) { // пользователь не найден в базе данных
                handler.addNewUser(user); // добавить пользователя в базу данных
                handler.addNewUserState(user); // добавить состояние чата пользователя в базу данных
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // получить предыдущее состояние пользователя + установить новое
        String previousState = handler.getUserState(user.getId());
        handler.setUserState(user.getId(), UserState.Names.BASE);

        this.showKeyboard();
    }


    private void showKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        ArrayList<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(Messages.ADD_NEW_PASSWORD);
        firstKeyboardRow.add(Messages.VIEW_STORAGE);
        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(Messages.GENERATE_PASSWORD);
        secondKeyboardRow.add(Messages.SETTINGS);
        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        SendMessage sendMessage = SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .replyMarkup(replyKeyboardMarkup)
                .text("Воспользуйтесь вспомогательным меню:")
                .build();
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
