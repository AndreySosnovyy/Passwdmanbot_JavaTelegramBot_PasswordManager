package ru.andreysosnovyy.workers;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.andreysosnovyy.Bot;
import ru.andreysosnovyy.DBHandler;
import ru.andreysosnovyy.tables.User;
import ru.andreysosnovyy.tables.UserState;
import ru.andreysosnovyy.utils.PasswordGenerator;

public class GenerateStateWorker extends Worker {

    public GenerateStateWorker(Bot bot, Update update) {
        super(bot, update);
    }

    DBHandler handler = new DBHandler();

    @Override
    public void run() {

        Message message = update.getMessage(); // пришедшее сообщение
        User user = User.builder() // пользователь, от которого пришло сообщение
                .id(message.getFrom().getId())
                .firstName(message.getFrom().getFirstName())
                .lastName(message.getFrom().getLastName())
                .username(message.getFrom().getUserName())
                .build();

        // получить предыдущее состояние пользователя + установить новое
        String previousState = handler.getUserState(user.getId());
        handler.setUserState(user.getId(), UserState.Names.GENERATE);

        // генерации пяти паролей с настройками по умолчанию
        for (int i = 0; i < 5; i++) {
            String pass = GeneratePassword(16, true, true, true, true);
            try {
                bot.execute(new SendMessage(update.getMessage().getChatId().toString(), pass));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        // todo: после предложенных паролей с настройками по умолчанию,
        //  добавить возможность настройки генерируемого пароля
        //  (длина, компоненты + добавить выход (/cancel) )
    }

    private String GeneratePassword(int length, boolean useDigits, boolean useLower,
                                    boolean useUpper, boolean usePunctuation) {
        return new PasswordGenerator.PasswordGeneratorBuilder()
                .useDigits(useDigits)
                .useLower(useLower)
                .useUpper(useUpper)
                .usePunctuation(usePunctuation)
                .build()
                .generate(length);
    }
}

