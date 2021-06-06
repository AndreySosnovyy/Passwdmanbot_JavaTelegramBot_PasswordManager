package ru.andreysosnovyy.utils;

import lombok.Getter;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.andreysosnovyy.Bot;
import ru.andreysosnovyy.DBHandler;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.tables.PasswordRecord;

import java.util.ArrayList;
import java.util.List;

public class WatchingRecordsController {

    @Getter
    public static class WatchingRecord {

        private final long userId;
        private final String serviceName;

        public WatchingRecord(long userId, String serviceName) {
            this.userId = userId;
            this.serviceName = serviceName;
        }
    }

    public static List<WatchingRecord> watchingRecords = new ArrayList<>();

    public static void add(long userId, String serviceName) {
        watchingRecords.add(new WatchingRecord(userId, serviceName));
    }

    public static void remove(long userId) {
        watchingRecords.removeIf(record -> record.userId == userId);
    }

    public static String getServiceName(long userId) {
        for (WatchingRecord record : watchingRecords) {
            if (record.userId == userId) {
                return record.getServiceName();
            }
        }
        return null;
    }

    @SneakyThrows
    public static void showKeyboard(Bot bot, long chatId,String serviceName) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        ArrayList<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow zeroKeyboardRow = new KeyboardRow();
        zeroKeyboardRow.add(Messages.BACK);
        KeyboardRow firstKeyboardRow = new KeyboardRow();
        firstKeyboardRow.add(Messages.EDIT_RECORD_PASSWORD);
        firstKeyboardRow.add(Messages.EDIT_RECORD_COMMENT);
        KeyboardRow secondKeyboardRow = new KeyboardRow();
        secondKeyboardRow.add(Messages.DELETE_RECORD);
        keyboard.add(zeroKeyboardRow);
        keyboard.add(firstKeyboardRow);
        keyboard.add(secondKeyboardRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        List<PasswordRecord> passwordRecords = new DBHandler().getUserPasswords(chatId);
        PasswordRecord targetRecord = null;
        for (PasswordRecord record : passwordRecords) {
            if (record.getServiceName().equals(serviceName)) {
                targetRecord = record;
            }
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(String.valueOf(chatId));

        assert targetRecord != null;
        sendMessage.setText(Messages.WATCHING_RECORD_INSTRUCTION);
        long messageID = bot.execute(sendMessage).getMessageId(); // пояснительное сообщение
        new DeleteMessageUtil(bot, new DeleteMessage(String.valueOf(chatId), (int) messageID), 30_000).start();
        sendMessage.setText(targetRecord.getServiceName());
        messageID = bot.execute(sendMessage).getMessageId(); // название сервиса
        new DeleteMessageUtil(bot, new DeleteMessage(String.valueOf(chatId), (int) messageID), 30_000).start();
        sendMessage.setText(targetRecord.getLogin());
        messageID = bot.execute(sendMessage).getMessageId(); // логин
        new DeleteMessageUtil(bot, new DeleteMessage(String.valueOf(chatId), (int) messageID), 30_000).start();
        sendMessage.setText(targetRecord.getPassword());
        messageID = bot.execute(sendMessage).getMessageId(); // пароль
        new DeleteMessageUtil(bot, new DeleteMessage(String.valueOf(chatId), (int) messageID), 30_000).start();
        sendMessage.setText(targetRecord.getComment());
        messageID = bot.execute(sendMessage).getMessageId(); // комментарий
        new DeleteMessageUtil(bot, new DeleteMessage(String.valueOf(chatId), (int) messageID), 30_000).start();
    }
}
