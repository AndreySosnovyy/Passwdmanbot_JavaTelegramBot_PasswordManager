package ru.andreysosnovyy.workers;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.andreysosnovyy.Bot;
import ru.andreysosnovyy.DBHandler;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.utils.PassListHandler;

public class RepositoryWorker {

    Bot bot;
    Update update;

    public RepositoryWorker(Bot bot, Update update) {
        this.bot = bot;
        this.update = update;
    }

    public void start(String search) {
        int page = bot.activeSessionsKeeper.getPage(update.getMessage().getChatId());
        try {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(update.getMessage().getChatId().toString())
                    .replyMarkup(new PassListHandler(new DBHandler().getUserPasswords(
                            update.getMessage().getChatId()), page).getInlineKeyboardMarkup(search))
                    .text(Messages.USE_REPO_MENU)
                    .build();
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static boolean validatePage(long userId, int page) {
        return page <= getLastPage(userId) && page >= 0;
    }

    public static int getLastPage(long userId) {
        return new DBHandler().getUserPasswords(userId).size() / 10;
    }
}
