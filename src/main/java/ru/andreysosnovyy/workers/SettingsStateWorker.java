package ru.andreysosnovyy.workers;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.Bot;

public class SettingsStateWorker extends Worker {

    public SettingsStateWorker(Bot bot, Update update) {
        super(bot, update);
    }


    @Override
    public void run() {

    }


}
