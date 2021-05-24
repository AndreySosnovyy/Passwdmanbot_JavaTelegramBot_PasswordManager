package ru.andreysosnovyy.workers;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.Bot;

public class SettingsWorker extends Worker {

    public SettingsWorker(Bot bot, Update update) {
        super(bot, update);
    }


    @Override
    public void run() {

    }


}
