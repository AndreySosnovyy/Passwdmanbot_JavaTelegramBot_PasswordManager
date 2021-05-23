package ru.andreysosnovyy;

import lombok.Data;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.config.BotConfig;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.tables.UserState;
import ru.andreysosnovyy.workers.BaseStateWorker;
import ru.andreysosnovyy.workers.GenerateStateWorker;

public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // todo: логирование
        DBHandler handler = new DBHandler();
        if (update.hasMessage() && update.getMessage().hasText()) {
            //DBHandler handler = new DBHandler(); // хэнлдер для работы с базой данных

            switch (update.getMessage().getText()) {

                case "/start" -> {
                    new BaseStateWorker(this, update).start();
                }

                case Messages.GENERATE_PASSWORD -> {
                    new GenerateStateWorker(this, update).start(); // запустить обработчика
                }

                default -> {
                    String state = handler.getUserState(update.getMessage().getChatId());
                    String text = update.getMessage().getText();

                    if (state.equals(UserState.Names.GENERATE)) {
                        new GenerateStateWorker(this, update).start();
                    }
                }
            }
        }
    }
}
