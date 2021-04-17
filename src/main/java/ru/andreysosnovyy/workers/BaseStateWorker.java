package ru.andreysosnovyy.workers;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.Bot;

public class BaseStateWorker extends Worker {

    public BaseStateWorker(Bot bot, Update update) {
        super(bot, update);
    }

    @Override
    public void run() {

    }

    @Override
    public void commandHelp() {

    }

    @Override
    public void commandSettings() {

    }

    @Override
    public void commandCancel() {

    }

    @Override
    public void commandNew() {

    }

    @Override
    public void commandRepository() {

    }

    @Override
    public void commandGenerate() {

    }
}
