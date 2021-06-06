package ru.andreysosnovyy.utils;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ActiveSessionsKeeper {

    public ActiveSessionsKeeper() {
        cleaner();
    }

    @Builder
    @Getter
    private static class ActiveSession {
        private final long userId;  // идентификатор пользователя
        private int page;           // номер страницы, на которой находится пользователь в хранилище
//        private String messageId;   // идентификатор сообщения, которое надо редактировать/удалять
        private long time;          // время последнего действия пользователя (нужно для проверки на таймауты)
    }

    @Builder
    @Getter
    private static class ActiveSettingsSession {
        private final long userId;
        private long time;
    }


    public final List<ActiveSession> activeSessions = new ArrayList<>();
    public final List<ActiveSettingsSession> activeSettingsSessions = new ArrayList<>();


    public void addActiveSession(long userId) {
        activeSessions.add(new ActiveSession(userId, 0, System.currentTimeMillis()));
    }


    public void addActiveSettingsSession(long userId) {
        activeSettingsSessions.add(new ActiveSettingsSession(userId, System.currentTimeMillis()));
    }


    // true - активная сессия / false - протухшая
    private boolean checkTimeout(long millis) {
        return System.currentTimeMillis() - millis < 300_000;
    }


    public boolean isActive(long userId) {
        for (ActiveSession session : activeSessions) {
            if (session.userId == userId && checkTimeout(session.time)) {
                return true;
            }
        }
        return false;
    }

    public boolean isActiveSettings(long userId) {
        for (ActiveSettingsSession session : activeSettingsSessions) {
            if (session.userId == userId && checkTimeout(session.time)) {
                return true;
            }
        }
        return false;
    }


    public void prolongSession(long userId) {
        for (ActiveSession session : activeSessions) {
            if (session.userId == userId) {
                session.time = System.currentTimeMillis();
                return;
            }
        }
    }

    public void prolongSettingsSession(long userId) {
        for (ActiveSettingsSession session : activeSettingsSessions) {
            if (session.userId == userId) {
                session.time = System.currentTimeMillis();
                return;
            }
        }
    }

    public void setPage(long userId, int page) {
        for (ActiveSession session : activeSessions) {
            if (session.getUserId() == userId) {
                session.page = page;
                return;
            }
        }
    }

    public int getPage(long userId) {
        for (ActiveSession session : activeSessions) {
            if (session.getUserId() == userId) {
                return session.page;
            }
        }
        return 0;
    }

//    public void setMessageId(long userId, String id) {
//        for (ActiveSession session : activeSessions) {
//            if (session.getUserId() == userId) {
//                session.messageId = id;
//                return;
//            }
//        }
//    }
//
//    public String getMessageId(long userId) {
//        for (ActiveSession session : activeSessions) {
//            if (session.getUserId() == userId) {
//                return session.messageId;
//            }
//        }
//        return null;
//    }


    private void cleaner() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            activeSessions.removeIf(session -> !checkTimeout(session.time));
            activeSettingsSessions.removeIf(session -> !checkTimeout(session.time));
        }, 0, 5_000, TimeUnit.MILLISECONDS);
    }
}
