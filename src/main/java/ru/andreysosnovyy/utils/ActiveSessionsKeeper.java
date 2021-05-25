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
        private final long userId;
        private long time;
    }


    private final List<ActiveSession> activeSessions = new ArrayList<>();


    public void addActiveSession(long userId) {
        activeSessions.add(new ActiveSession(userId, System.currentTimeMillis()));
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


    public void prolongSession(long userId) {
        for (ActiveSession session : activeSessions) {
            if (session.userId == userId) {
                session.time = System.currentTimeMillis();
                return;
            }
        }
    }


    private void cleaner() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> activeSessions.removeIf(session ->
                !checkTimeout(session.time)), 0, 5_000, TimeUnit.MILLISECONDS);
    }
}
