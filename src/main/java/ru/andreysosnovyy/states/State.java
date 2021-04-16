package ru.andreysosnovyy.states;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.Bot;

public abstract class State {
    private Bot bot;
    private Update update;

    State(Bot bot) {
        this.bot = bot;
        // логирование
    }

    public void setUpdate(Update update) {
        this.update = update;
    }

    public Update getUpdate() {
        return update;
    }

    public abstract void commandHelp();

    public abstract void commandSettings();

    public abstract void commandCancel();

    public abstract void commandNew();

    public abstract void commandRepository();

    public void commandGenerate() { // сгенерировать пароль

    }
}
