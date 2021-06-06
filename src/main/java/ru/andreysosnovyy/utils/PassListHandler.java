package ru.andreysosnovyy.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.tables.PasswordRecord;

import java.util.ArrayList;
import java.util.List;

public class PassListHandler {

    // todo: что-то сделать с повторными именами сервисов
    //  (скорее всего надо будет как-то выдавать разные колбэки)

    private final int recordsPerPage = 10;

    private final List<PasswordRecord> passwords;
    private final int page;

    public PassListHandler(List<PasswordRecord> passwords, int page) {
        this.passwords = passwords;
        this.page = page;
    }

    public InlineKeyboardMarkup getInlineKeyboardMarkup(String search) {

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

        for (int i = page * recordsPerPage; i < page * recordsPerPage + 10; i++) {
            if (passwords.size() > i) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();

                if (search == null || search.isEmpty()) {
                    button.setText(passwords.get(i).getServiceName());
                    // todo: устанавливать уникальные колбэки или
                    //  добавить проверку на повторение имен сервисов в базе при добавлении
                    button.setCallbackData(passwords.get(i).getServiceName());
                } else if (passwords.get(i).getServiceName().toLowerCase().contains(search.toLowerCase())) {

                }

                row.add(button);
                rowList.add(row);
            }
        }

        // ряд с кнопками переходов по страницам
        InlineKeyboardButton beginPageButton = new InlineKeyboardButton();
        beginPageButton.setCallbackData("beginPageButton");
        beginPageButton.setText(Messages.LEFT_ARROW + Messages.LEFT_ARROW);

        InlineKeyboardButton previousPageButton = new InlineKeyboardButton();
        previousPageButton.setCallbackData("previousPageButton");
        if (page > 0) {
            previousPageButton.setText(Messages.LEFT_ARROW + " " + page);
        } else {
            previousPageButton.setText(Messages.LEFT_ARROW);
        }


        InlineKeyboardButton currentPageButton = new InlineKeyboardButton();
        currentPageButton.setCallbackData("currentPageButton");
        currentPageButton.setText(String.valueOf(page + 1));

        InlineKeyboardButton nextPageButton = new InlineKeyboardButton();
        nextPageButton.setCallbackData("nextPageButton");
        nextPageButton.setText((page + 2) + " " + Messages.RIGHT_ARROW);

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
        return markup;
    }
}
