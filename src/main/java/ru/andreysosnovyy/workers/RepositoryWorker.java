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
        showKeyboard();
    }

    private void showKeyboard() {

        // todo: запоминать id сообщения, чтобы можно было его потом редактировать (при смене страниц в хранилище)

        // todo: где-то хранить номер страницы

        try {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(update.getMessage().getChatId().toString())
                    .replyMarkup(new PassListHandler(new DBHandler().getUserPasswords(update.getMessage().getChatId()), 0).getInlineKeyboardMarkup())
                    .text(Messages.USE_REPO_MENU)
                    .build();
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
