package ru.andreysosnovyy.states;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.Bot;

public class BaseState extends State {

    public BaseState(Bot bot, Update update) {
        super(bot, update);
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
