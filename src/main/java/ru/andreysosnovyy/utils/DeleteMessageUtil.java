package ru.andreysosnovyy.utils;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.andreysosnovyy.Bot;

public class DeleteMessageUtil extends Thread {

    private final Bot bot;
    private final org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage deleteMessage;
    private final long timeout;

    public DeleteMessageUtil(Bot bot, org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage deleteMessage, long timeout) {
        this.deleteMessage = deleteMessage;
        this.timeout = timeout;
        this.bot = bot;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            bot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
