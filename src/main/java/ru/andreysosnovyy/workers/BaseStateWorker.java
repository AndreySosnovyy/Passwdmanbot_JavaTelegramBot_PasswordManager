package ru.andreysosnovyy.workers;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.andreysosnovyy.Bot;
import ru.andreysosnovyy.config.Messages;

import java.util.ArrayList;

public class BaseStateWorker extends Worker {

    public BaseStateWorker(Bot bot, Update update) {
        super(bot, update);
    }

    @Override
    public void run() {
        showKeyboard();
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
