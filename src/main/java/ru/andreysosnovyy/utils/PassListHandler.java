package ru.andreysosnovyy.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.andreysosnovyy.tables.PasswordRecord;

import java.util.ArrayList;
import java.util.List;

public class PassListHandler {

    List<PasswordRecord> passwords;

    public PassListHandler(List<PasswordRecord> passwords) {
        this.passwords = passwords;
    }

    public InlineKeyboardMarkup getInlineKeyboardMarkup() {

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        for (PasswordRecord password : passwords) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(password.getServiceName());
            button.setCallbackData(password.getServiceName());
            row.add(button);
            rowList.add(row);
        }
        markup.setKeyboard(rowList);
        return markup;
    }
}
