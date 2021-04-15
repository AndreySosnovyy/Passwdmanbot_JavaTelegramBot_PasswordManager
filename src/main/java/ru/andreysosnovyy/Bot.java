package ru.andreysosnovyy;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.states.BaseState;
import ru.andreysosnovyy.states.State;

public class Bot extends TelegramLongPollingBot {

    private State state; // текущее состояние бота

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public Bot() {
        this.state = new BaseState(this); // инициализация бота начальным состоянием
    }

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    // todo: нужно получать состояния для каждого чата индивидуально и хранить их в парах

    @Override
    public void onUpdateReceived(Update update) {
        // обработка полученных сообщений
        if (update.hasMessage() && update.getMessage().hasText()) {
            // логирование
            state.setUpdate(update);
            switch (update.getMessage().getText()) {
                case "/help" -> state.commandHelp();
                case "/settings" -> state.commandSettings();
                case "/repository", "/repo" -> state.commandRepository();
                case "/new" -> state.commandNew();
                case "/cancel" -> state.commandCancel();
                case "/generate", "/gen" -> state.commandGenerate();
                default -> { // обработка сообщения, если это не команда

                }
            }
        }
    }


}
