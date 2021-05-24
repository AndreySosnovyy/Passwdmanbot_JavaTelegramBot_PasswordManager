package ru.andreysosnovyy.utils;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RepoPassWitness {

    public RepoPassWitness() {
        cleaner();
    }

    @Builder
    @Getter
    private static class UnconfirmedRepoPass {
        private final long userId;
        private final String password;
        private final long time;
    }

    private final List<UnconfirmedRepoPass> unconfirmedRepoPasses = new ArrayList<>();

    // true - свежий / false - протух
    private boolean checkTimeout(long millis) {
        return System.currentTimeMillis() - millis < 60_000;
    }

    public void addUnconfirmedRepoPass(long userId, String pass) {
        unconfirmedRepoPasses.add(new UnconfirmedRepoPass(userId, pass, System.currentTimeMillis()));
    }

    public boolean checkUnconfirmedRepoPass(long userId, String pass) {
        for (UnconfirmedRepoPass unconfirmedRepoPass : unconfirmedRepoPasses) {
            if (unconfirmedRepoPass.userId == userId &&
                    unconfirmedRepoPass.password.equals(pass) &&
                    checkTimeout(unconfirmedRepoPass.time)) {
                unconfirmedRepoPasses.remove(unconfirmedRepoPass);
                return true;
            }
        }
        return false;
    }

    public boolean checkIfExists(long userId) {
        for (UnconfirmedRepoPass unconfirmedRepoPass : unconfirmedRepoPasses)
            if (unconfirmedRepoPass.userId == userId) return true;
        return false;
    }

    public void cleaner() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> unconfirmedRepoPasses.removeIf(unconfirmedRepoPass ->
                !checkTimeout(unconfirmedRepoPass.time)), 0, 5_000, TimeUnit.MILLISECONDS);
    }
}
