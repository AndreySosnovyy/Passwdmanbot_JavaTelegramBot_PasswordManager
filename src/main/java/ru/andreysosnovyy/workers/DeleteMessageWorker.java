package ru.andreysosnovyy.workers;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.andreysosnovyy.Bot;

public class DeleteMessageWorker extends Thread {

    private final Bot bot;
    private final DeleteMessage deleteMessage;
    private final long timeout;

    public DeleteMessageWorker(Bot bot, DeleteMessage deleteMessage, long timeout) {
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
