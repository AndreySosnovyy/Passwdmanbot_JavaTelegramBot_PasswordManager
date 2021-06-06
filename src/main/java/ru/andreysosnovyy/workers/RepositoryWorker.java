package ru.andreysosnovyy.workers;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.andreysosnovyy.Bot;
import ru.andreysosnovyy.DBHandler;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.utils.PassListHandler;

public class RepositoryWorker extends Worker {

    public RepositoryWorker(Bot bot, Update update) {
        super(bot, update);
    }

    @Override
    public void run() {
        int page = bot.activeSessionsKeeper.getPage(update.getMessage().getChatId());
        try {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(update.getMessage().getChatId().toString())
                    .replyMarkup(new PassListHandler(new DBHandler().getUserPasswords(
                            update.getMessage().getChatId()), page).getInlineKeyboardMarkup(null))
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
