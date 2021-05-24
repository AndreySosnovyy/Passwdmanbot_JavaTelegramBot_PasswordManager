package ru.andreysosnovyy;

import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.andreysosnovyy.config.BotConfig;
import ru.andreysosnovyy.config.Messages;
import ru.andreysosnovyy.tables.User;
import ru.andreysosnovyy.tables.UserState;
import ru.andreysosnovyy.utils.Hash;
import ru.andreysosnovyy.utils.PasswordGenerator;
import ru.andreysosnovyy.utils.RepoPassWitness;
import ru.andreysosnovyy.workers.BaseKeyboardWorker;
import ru.andreysosnovyy.workers.DeleteMessageWorker;
import ru.andreysosnovyy.workers.GenerateWorker;

public class Bot extends TelegramLongPollingBot {

    RepoPassWitness repoPassWitness = null;

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
        // todo: �����������

        // ������������� ������� ��� ������ � ����������������� �������� �� ��������
        if (repoPassWitness == null) repoPassWitness = new RepoPassWitness();

        if (update.hasMessage() && update.getMessage().hasText()) {
            DBHandler handler = new DBHandler(); // ������� ��� ������ � ����� ������
            String userState = handler.getUserState(update.getMessage().getChatId()); // ��������� ������������
            Message message = update.getMessage(); // ��������� �� �������

            // ���� ��������� �� ��������������
            if (message.getFrom().getId() == BotConfig.ADMIN_ID) {
                switch (message.getText()) {
                    case "/clear_db" -> {
                        handler.clearDB();
                        return;
                    }
                }
            }

            // ���� ������������ �� ��� ������ � ���� ������
            if (userState == null) {
                User user = User.builder()
                        .firstName(message.getFrom().getFirstName())
                        .lastName(message.getFrom().getLastName())
                        .username(message.getFrom().getUserName())
                        .id(message.getChatId())
                        .build();
                // �������� ������������ � ���� ������
                handler.addNewUser(user);
                handler.addNewUserState(user);
                // ��������� ��������� � ������������� ������� ������-������
                execute(new SendMessage(message.getChatId().toString(), Messages.CREATE_REPOSITORY_PASSWORD));
                return;
            }

            // ����� ����� ����������� �� ��������� ������������ ��� ����������� ���������
            switch (userState) {
                case UserState.Names.BASE_NO_REPOSITORY_PASSWORD -> {
                    long userId = message.getFrom().getId();
                    if (repoPassWitness.checkIfExists(userId)) { // ���� ����
                        if (repoPassWitness.checkUnconfirmedRepoPass(userId, message.getText())) { // ���� ������
                            String hashedPassword = Hash.getHash(message.getText());
                            handler.addRepositoryPasswordHash(userId, hashedPassword); // �������� ������
                            handler.setUserState(userId, UserState.Names.BASE); // ������� ��������� ������������
                            execute(new SendMessage(String.valueOf(userId), Messages.CREATED_SUCCESSFUL)); // ���������
                            new BaseKeyboardWorker(this, update).start(); // ������� ����������
                        } else { // ����, �� �� ������
                            execute(new SendMessage(String.valueOf(userId), Messages.TRY_AGAIN_REPOSITORY_PASSWORD));
                        }
                    } else { // ������ ���, ���� ��������
                        repoPassWitness.addUnconfirmedRepoPass(message.getFrom().getId(), message.getText());
                        execute(new SendMessage(String.valueOf(userId), Messages.CONFIRM_REPOSITORY_PASSWORD));
                    }
                }

                case UserState.Names.BASE -> {
                    switch (message.getText()) {
                        case "/start" -> {
                            new BaseKeyboardWorker(this, update).start();
                        }

                        case Messages.VIEW_REPOSITORY -> {
                            // ���������� ��� ���������

                            // todo: ��������� ������-������
                            // todo: ������� ������ ������ � ���������� �������� 2 ������ ����� ���������� ��������
                        }

                        case Messages.GENERATE_PASSWORD -> {
                            new GenerateWorker(this, update).start();
                        }

                        case Messages.SETTINGS -> {
                            // ���������� ��� ��������

                            // todo: 1) �������� ������ ������
                            // todo: 2) ������� ��������� � ������-������ (+ ������� ���������)
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

