package ru.andreysosnovyy.workers;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.andreysosnovyy.Bot;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.utils.PasswordGenerator;

import java.util.ArrayList;
import java.util.List;

public class GenerateWorker extends Worker {

    public GenerateWorker(Bot bot, Update update) {
        super(bot, update);
    }

    @Override
    public void run() {

        // генерации пяти паролей с настройками по умолчанию
        List<String> passwords = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            passwords.add(GeneratePassword(16, true, true, true, true));
        }

        // клавиатура
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        ArrayList<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(passwords.get(0));
        firstKeyboardRow.add(passwords.get(1));
        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(passwords.get(2));
        secondKeyboardRow.add(passwords.get(3));
        KeyboardRow thirdKeyboardRow = new KeyboardRow();
        thirdKeyboardRow.add(passwords.get(4));
        thirdKeyboardRow.add(Messages.BACK);
        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        keyboard.add(thirdKeyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        // отправка сообщения с клавиатурой
        SendMessage sendMessage = SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .replyMarkup(replyKeyboardMarkup)
                .text(Messages.TAP_TO_CHOOSE)
                .build();
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public static String GeneratePassword(int length, boolean useDigits, boolean useLower,
                                          boolean useUpper, boolean usePunctuation) {
        return new PasswordGenerator.PasswordGeneratorBuilder()
                .useDigits(useDigits)
                .useLower(useLower)
                .useUpper(useUpper)
                .usePunctuation(usePunctuation)
                .build()
                .generate(length);
    }
}

