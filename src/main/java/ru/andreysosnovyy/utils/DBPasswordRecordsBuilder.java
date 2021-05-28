package ru.andreysosnovyy.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DBPasswordRecordsBuilder {

    ActiveSessionsKeeper activeSessionsKeeper;

    public DBPasswordRecordsBuilder(ActiveSessionsKeeper activeSessionsKeeper) {
        this.activeSessionsKeeper = activeSessionsKeeper;
        cleaner();
    }


    @Getter
    @Setter
    public class DBPasswordRecord {

        private DBPasswordRecord(long userId) {
            this.userId = userId;
        }

        private final long userId;
        private String serviceName;
        private String login;
        private String password;
        private String comment;
    }


    private final List<DBPasswordRecord> dbPasswordRecords = new ArrayList<>();

    public void addRecord(long userId) {
        dbPasswordRecords.add(new DBPasswordRecord(userId));
    }

    public void removeRecord(long userId) {
        dbPasswordRecords.removeIf(record -> record.getUserId() == userId);
    }

    public void setServiceName(long userId, String serviceName) throws Exception {
        for (DBPasswordRecord record : dbPasswordRecords) {
            if (record.userId == userId) {
                record.setServiceName(serviceName);
                activeSessionsKeeper.prolongSession(userId);
                return;
            } else {
                throw new Exception("Unable to set service name for user " + userId);
            }
        }
    }

    public void setLogin(long userId, String login) throws Exception {
        for (DBPasswordRecord record : dbPasswordRecords) {
            if (record.userId == userId) {
                record.setLogin(login);
                activeSessionsKeeper.prolongSession(userId);
                return;
            } else {
                throw new Exception("Unable to set login for user " + userId);
            }
        }
    }

    public void setPassword(long userId, String password) throws Exception {
        for (DBPasswordRecord record : dbPasswordRecords) {
            if (record.userId == userId) {
                record.setPassword(password);
                activeSessionsKeeper.prolongSession(userId);
                return;
            } else {
                throw new Exception("Unable to set password for user " + userId);
            }
        }
    }

    public void setComment(long userId, String comment) throws Exception {
        for (DBPasswordRecord record : dbPasswordRecords) {
            if (record.userId == userId) {
                record.setComment(comment);
                activeSessionsKeeper.prolongSession(userId);
                return;
            } else {
                throw new Exception("Unable to set comment for user " + userId);
            }
        }
    }

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
                !activeSessionsKeeper.isActive(record.getUserId())), 0, 5_000, TimeUnit.MILLISECONDS);
    }
}
