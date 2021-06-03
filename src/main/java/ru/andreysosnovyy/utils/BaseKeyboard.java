package ru.andreysosnovyy.utils;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.andreysosnovyy.Bot;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.workers.Worker;

import java.util.ArrayList;

public class BaseKeyboard extends Worker {

    public BaseKeyboard(Bot bot, Update update) {
        super(bot, update);
    }

    @Override
    public void run() {
        this.showKeyboard();
    }


    private void showKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        ArrayList<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(Messages.VIEW_REPOSITORY);
        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(Messages.GENERATE_PASSWORD);
        secondKeyboardRow.add(Messages.SETTINGS);
        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setText(Messages.USE_MENU);
        if (update.hasCallbackQuery()) {
            sendMessage.setChatId(update.getCallbackQuery().getFrom().getId().toString());
        } else {
            sendMessage.setChatId(update.getMessage().getFrom().getId().toString());
        }

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static ReplyKeyboardMarkup getCancelKeyboard() {
        // разметка для клавиатуры с кнопкой "отмена"
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        ArrayList<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(Messages.CANCEL);
        keyboard.add(firstKeyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }
}
