package ch.zhaw.abyss.application;

import ch.zhaw.abyss.domain.ActiveModule;

import java.util.Set;

public record Profile(
        int runs,
        int wins,
        int bestRoom,
        int bestCycle,
        int totalKills,
        Set<ActiveModule> unlocked,
        Settings settings) {
    public static Profile fresh() {
        return new Profile(0, 0, 0, 0, 0, Set.of(ActiveModule.PULSE), Settings.DEFAULT);
    }

    public Profile {
        unlocked = Set.copyOf(unlocked);
        if (runs < 0
                || wins < 0
                || bestRoom < 0
                || bestRoom > ch.zhaw.abyss.domain.RoomGenerator.ROOM_COUNT
                || bestCycle < 0
                || totalKills < 0
                || !unlocked.contains(ActiveModule.PULSE)
                || settings == null) throw new IllegalArgumentException("Ungültiges Profil");
    }

    public Profile withSettings(Settings next) {
        return new Profile(runs, wins, bestRoom, bestCycle, totalKills, unlocked, next);
    }
}
