package ru.andreysosnovyy;

import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.andreysosnovyy.config.BotConfig;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.utils.ActiveSessionsKeeper;
import ru.andreysosnovyy.utils.DBPasswordRecordsBuilder;
import ru.andreysosnovyy.utils.PassListHandler;
import ru.andreysosnovyy.utils.RepoPassWitness;

import java.util.ArrayList;
import java.util.List;

class TestBot extends TelegramLongPollingBot {

    RepoPassWitness repoPassWitness = null;
    ActiveSessionsKeeper activeSessionsKeeper = null;
    DBPasswordRecordsBuilder dbPasswordRecordsBuilder = null;

    public static void main(String[] args) {
        TelegramBotsApi telegramBotsApi;
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new TestBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        if (repoPassWitness == null) repoPassWitness = new RepoPassWitness();
        if (activeSessionsKeeper == null) activeSessionsKeeper = new ActiveSessionsKeeper();
        if (dbPasswordRecordsBuilder == null)
            dbPasswordRecordsBuilder = new DBPasswordRecordsBuilder(activeSessionsKeeper);

        DBHandler handler = new DBHandler(); // хэнлдер для работы с базой данных
        Message message = update.getMessage(); // сообщение из апдейта

        if (update.hasMessage() && update.getMessage().hasText()) {

            String userState = handler.getUserState(update.getMessage().getChatId()); // состояние пользователя

            System.out.println("Message");

            if (update.getMessage().getText().equals("Hello")) {
                new Keyboarder(this, update).start();
            }

        } else if (update.hasCallbackQuery()) {

            System.out.println("Callback");

            try {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText(update.getCallbackQuery().getData());
                sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }

    public static class Keyboarder extends Thread {

        TestBot bot;
        Update update;

        public Keyboarder(TestBot bot, Update update) {
            this.bot = bot;
            this.update = update;
        }

        @Override
        public void run() {
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

    public static SendMessage sendInlineKeyBoardMessage(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        // кнопка поиска
        InlineKeyboardButton searchButton = new InlineKeyboardButton();
        searchButton.setText(Messages.SEARCH);
        searchButton.setCallbackData("searchButton");

        // кнопка добавления
        InlineKeyboardButton addButton = new InlineKeyboardButton();
        addButton.setText(Messages.ADD_NEW_PASSWORD);
        addButton.setCallbackData("addButton");

        List<InlineKeyboardButton> headerRow = new ArrayList<>();
        headerRow.add(searchButton);
        headerRow.add(addButton);
        rowList.add(headerRow);

        // ряд с кнопками переходов по страницам
        InlineKeyboardButton beginPageButton = new InlineKeyboardButton();
        beginPageButton.setCallbackData("beginPageButton");
        beginPageButton.setText(Messages.LEFT_ARROW + Messages.LEFT_ARROW);

        InlineKeyboardButton previousPageButton = new InlineKeyboardButton();
        previousPageButton.setCallbackData("previousPageButton");
        previousPageButton.setText(Messages.LEFT_ARROW + " " + (10 - 1));

        InlineKeyboardButton currentPageButton = new InlineKeyboardButton();
        currentPageButton.setCallbackData("currentPageButton");
        currentPageButton.setText(String.valueOf(10));

        InlineKeyboardButton nextPageButton = new InlineKeyboardButton();
        nextPageButton.setCallbackData("nextPageButton");
        nextPageButton.setText((10 + 1) + " " + Messages.RIGHT_ARROW);

        InlineKeyboardButton lastPageButton = new InlineKeyboardButton();
        lastPageButton.setCallbackData("lastPageButton");
        lastPageButton.setText(Messages.RIGHT_ARROW + Messages.RIGHT_ARROW);

        List<InlineKeyboardButton> pagesRow = new ArrayList<>();
        pagesRow.add(beginPageButton);
        pagesRow.add(previousPageButton);
        pagesRow.add(currentPageButton);
        pagesRow.add(nextPageButton);
        pagesRow.add(lastPageButton);
        rowList.add(pagesRow);

        // кнопка выхода
        InlineKeyboardButton exitButton = new InlineKeyboardButton();
        exitButton.setText(Messages.EXIT_REPO);
        exitButton.setCallbackData("exitButton");

        List<InlineKeyboardButton> footerRow = new ArrayList<>();
        footerRow.add(exitButton);
        rowList.add(footerRow);

        markup.setKeyboard(rowList);
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Hi!");
        message.setReplyMarkup(markup);
        return message;
    }
}