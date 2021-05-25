package ru.andreysosnovyy.workers;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.andreysosnovyy.Bot;

public class EditMessageWorker extends Thread {

    private final Bot bot;
    private final EditMessageText editMessageText;
    private final long timeout;

    public EditMessageWorker(Bot bot, EditMessageText editMessageText, long timeout) {
        this.editMessageText = editMessageText;
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
            bot.execute(editMessageText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
