package ru.andreysosnovyy;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.utils.PassListHandler;

import java.util.ArrayList;
import java.util.List;

class HabrBot extends TelegramLongPollingBot {

    private static final String botUserName = "passwdmanbot";
    private static final String token = "1703194746:AAHE_kbHHnTcZ8AeQwPgjym7ZiP6Dsr0bog";

    public static void main(String[] args) {
        TelegramBotsApi telegramBotsApi;
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new HabrBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            if (update.getMessage().getText().equals("Hello")) {
                new Keyboarder(this, update).start();
            }
        } else if (update.hasCallbackQuery()) {
            try {
                SendMessage message = new SendMessage();
                message.setText(update.getCallbackQuery().getData());
                message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Keyboarder extends Thread {

        HabrBot bot;
        Update update;

        public Keyboarder(HabrBot bot, Update update) {
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