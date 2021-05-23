package ru.andreysosnovyy.workers;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
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
import ru.andreysosnovyy.utils.PasswordGenerator;

import java.util.ArrayList;
import java.util.List;

public class GenerateStateWorker extends Worker {

    public GenerateStateWorker(Bot bot, Update update) {
        super(bot, update);
    }

    DBHandler handler = new DBHandler();

    @Override
    public void run() {

        Message message = update.getMessage(); // пришедшее сообщение
        User user = User.builder() // пользователь, от которого пришло сообщение
                .id(message.getFrom().getId())
                .firstName(message.getFrom().getFirstName())
                .lastName(message.getFrom().getLastName())
                .username(message.getFrom().getUserName())
                .build();

        // получить предыдущее состояние пользователя + установить новое
        String previousState = handler.getUserState(user.getId());
        handler.setUserState(user.getId(), UserState.Names.GENERATE);

        if (message.getText().equals(Messages.GENERATE_PASSWORD)) {
            // генерации пяти паролей с настройками по умолчанию
            List<String> passwords = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                passwords.add(GeneratePassword(16, true, true, true, true));
            }

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

            SendMessage sendMessage = SendMessage.builder()
                    .chatId(update.getMessage().getChatId().toString())
                    .replyMarkup(replyKeyboardMarkup)
                    .text(Messages.TAP_TO_COPY)
                    .build();
            try {
                bot.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            // todo: проверять не целые строки, а посимвольно
            if (message.getText().contains(PasswordGenerator.PUNCTUATION) &&
                    message.getText().contains(PasswordGenerator.DIGITS) &&
                    message.getText().contains(PasswordGenerator.LOWER) &&
                    message.getText().contains(PasswordGenerator.UPPER) &&
                    message.getText().length() == 16) {
                try {
                    // todo: не работает
                    bot.execute(new DeleteMessage(message.getChatId().toString(), message.getMessageId()));
                    
                    // =============================================
                    System.out.println("DELETE MESSAGE WITH PASS");
                    // =============================================

                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (message.getText().equals(Messages.BACK)) {
                handler.setUserState(user.getId(), UserState.Names.BASE);
            }
        }


        // todo: после предложенных паролей с настройками по умолчанию,
        //  добавить возможность настройки генерируемого пароля
        //  (длина, компоненты + добавить выход (/cancel) )
    }

    private String GeneratePassword(int length, boolean useDigits, boolean useLower,
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

