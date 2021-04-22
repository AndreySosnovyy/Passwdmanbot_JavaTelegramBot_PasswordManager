package ru.andreysosnovyy.workers;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.Bot;
import ru.andreysosnovyy.tables.UserState;
import ru.andreysosnovyy.utils.PasswordGenerator;

public class GenerateStateWorker extends Worker {

    public GenerateStateWorker(Bot bot, Update update) {
        super(bot, update);
    }

    @Override
    public void run() {

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

