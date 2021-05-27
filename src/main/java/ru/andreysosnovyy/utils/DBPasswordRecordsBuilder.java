package ru.andreysosnovyy.utils;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DBPasswordRecordsBuilder {

    public DBPasswordRecordsBuilder() {
        cleaner();
    }


    public static class NoActiveSessionFoundException extends Exception {
        public NoActiveSessionFoundException(long userId) {
            super("No active session for user " + userId);
        }
    }


    @Getter
    public class DBPasswordRecord {

        private DBPasswordRecord(long userId) {
            this.userId = userId;
            this.time = System.currentTimeMillis();
        }

        private void setServiceName(String serviceName) {
            this.serviceName = serviceName;
            this.time = System.currentTimeMillis();
        }

        private void setLogin(String login) {
            this.login = login;
            this.time = System.currentTimeMillis();
        }

        private void setPassword(String password) {
            this.password = password;
            this.time = System.currentTimeMillis();
        }

        private void setComment(String comment) {
            this.comment = comment;
            this.time = System.currentTimeMillis();
        }

        private final long userId;
        private String serviceName;
        private String login;
        private String password;
        private String comment;
        private long time;
    }


    private final List<DBPasswordRecord> dbPasswordRecords = new ArrayList<>();

    public void addRecord(long userId) {
        dbPasswordRecords.add(new DBPasswordRecord(userId));
    }

    public void setServiceName(long userId, String serviceName) throws NoActiveSessionFoundException {
        for (DBPasswordRecord record : dbPasswordRecords) {
            if (record.userId == userId && checkTimeout(record.time)) {
                record.setServiceName(serviceName);
            } else {
                throw new NoActiveSessionFoundException(userId);
            }
        }
    }

    public void setLogin(long userId, String login) throws NoActiveSessionFoundException {
        for (DBPasswordRecord record : dbPasswordRecords) {
            if (record.userId == userId && checkTimeout(record.time)) {
                record.setLogin(login);
            } else {
                throw new NoActiveSessionFoundException(userId);
            }
        }
    }

    public void setPassword(long userId, String password) throws NoActiveSessionFoundException {
        for (DBPasswordRecord record : dbPasswordRecords) {
            if (record.userId == userId && checkTimeout(record.time)) {
                record.setPassword(password);
            } else {
                throw new NoActiveSessionFoundException(userId);
            }
        }
    }

    public void setComment(long userId, String comment) throws NoActiveSessionFoundException {
        for (DBPasswordRecord record : dbPasswordRecords) {
            if (record.userId == userId && checkTimeout(record.time)) {
                record.setComment(comment);
            } else {
                throw new NoActiveSessionFoundException(userId);
            }
        }
    }


    // true - активная сессия / false - протухшая
    private boolean checkTimeout(long millis) {
        return System.currentTimeMillis() - millis < 30_000;
    } // todo: 150_000

    public DBPasswordRecord buildAndGet(long userId) {
        for (DBPasswordRecord record : dbPasswordRecords) {
            if (record.getUserId() == userId) {
                // todo: шифрование
                return record;
            }
        }
        return null;
    }


    private void cleaner() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> dbPasswordRecords.removeIf(record ->
                !checkTimeout(record.time)), 0, 5_000, TimeUnit.MILLISECONDS);
    }
}
