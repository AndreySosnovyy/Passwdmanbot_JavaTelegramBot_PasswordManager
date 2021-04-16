package ru.andreysosnovyy.states;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.andreysosnovyy.Bot;
import ru.andreysosnovyy.utils.PasswordGenerator;

public class GenerateState extends State {

    public GenerateState(Bot bot, Update update) {
        super(bot, update);

        PasswordGenerator passwordGenerator = new PasswordGenerator.PasswordGeneratorBuilder()
                .useDigits(true)
                .useLower(true)
                .useUpper(true)
                .usePunctuation(true)
                .build();

        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());

        for (int i = 0; i < 5; i++) {
            String password = passwordGenerator.generate(16);
            message.setText(password);
            try {
                bot.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void commandHelp() {

    }

    @Override
    public void commandSettings() {

    }

    @Override
    public void commandCancel() {

    }

    @Override
    public void commandNew() {

    }

    @Override
    public void commandRepository() {

    }

    @Override
    public void commandGenerate() {

    }
}
