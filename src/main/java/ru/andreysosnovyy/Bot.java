package ru.andreysosnovyy;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.config.BotConfig;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.tables.User;
import ru.andreysosnovyy.tables.UserState;
import ru.andreysosnovyy.utils.PasswordGenerator;
import ru.andreysosnovyy.workers.BaseKeyboardWorker;
import ru.andreysosnovyy.workers.DeleteMessageWorker;
import ru.andreysosnovyy.workers.GenerateWorker;

public class Bot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // todo: �����������

        if (update.hasMessage() && update.getMessage().hasText()) {
            DBHandler handler = new DBHandler(); // ������� ��� ������ � ����� ������
            String userState; // ��������� ������������, ������������ ���������
            Message message = update.getMessage(); // ��������� �� �������

            // �������� ������� ��������� ���� ������������
            userState = handler.getUserState(update.getMessage().getChatId());
            if (userState == null) {
                // ������������ �� ��� ������ � ���� ������ -> ��������
                User user = User.builder()
                        .firstName(message.getFrom().getFirstName())
                        .lastName(message.getFrom().getLastName())
                        .username(message.getFrom().getUserName())
                        .id(message.getChatId())
                        .build();
                handler.addNewUser(user);
                handler.addNewUserState(user);
                userState = UserState.Names.BASE;
            }

            switch (userState) { // ����� ����� ����������� �� ��������� ������������ ��� ����������� ���������
                case UserState.Names.BASE -> {
                    switch (message.getText()) {
                        case "/start" -> {
                            new BaseKeyboardWorker(this, update).start();
                        }

                        case Messages.VIEW_REPOSITORY -> {
                            // ���������� ��� ���������
                        }

                        case Messages.GENERATE_PASSWORD -> {
                            new GenerateWorker(this, update).start();
                        }

                        case Messages.SETTINGS -> {
                            // ���������� ��� ��������
                        }

                        default -> {
                            // ���� ������ ������, �� ��� ���� ����� �������
                            if (PasswordGenerator.checkIfPassword(message.getText())) {
                                new DeleteMessageWorker(this, new DeleteMessage(
                                        message.getChatId().toString(), message.getMessageId()),
                                        15_000).start();
                            }
                            // ������� ��������� ����������
                            new BaseKeyboardWorker(this, update).start();
                        }
                    }
                }
            }
        }
    }
}

