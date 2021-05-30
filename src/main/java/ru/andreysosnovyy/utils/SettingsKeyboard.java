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

public class SettingsKeyboard extends Worker {

    public SettingsKeyboard(Bot bot, Update update) {
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
        firstKeyboardRow.add(Messages.CHANGE_MASTER_PASS);
        firstKeyboardRow.add(Messages.RESTORE_MASTER_PASS);
        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(Messages.DELETE_REPO);
        secondKeyboardRow.add(Messages.EXIT_SETTINGS);
        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        SendMessage sendMessage = SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .replyMarkup(replyKeyboardMarkup)
                .text(Messages.USE_MENU)
                .build();
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
