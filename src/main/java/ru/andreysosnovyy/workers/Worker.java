package ru.andreysosnovyy.workers;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.Bot;

public abstract class Worker extends Thread {
    protected Bot bot;
    protected Update update;

    Worker(Bot bot, Update update) {
        this.bot = bot;
        this.update = update;
        // логирование
    }

    public Update getUpdate() {
        return update;
    }

    public abstract void run();

    public abstract void commandHelp();

    public abstract void commandSettings();

    public abstract void commandCancel();

    public abstract void commandNew();

    public abstract void commandRepository();

    public abstract void commandGenerate();
}