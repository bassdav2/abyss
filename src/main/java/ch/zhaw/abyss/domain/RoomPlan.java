package ch.zhaw.abyss.domain;

import java.util.List;

public record RoomPlan(
        int depth,
        int branch,
        int sector,
        int variant,
        Kind kind,
        String title,
        String description,
        List<EnemyKind> enemies,
        int salvageReward) {
    public enum Kind {
        COMBAT,
        ELITE,
        WORKSHOP,
        CACHE,
        BOSS,
        BRIDGE
    }

    public RoomPlan {
        enemies = List.copyOf(enemies);
    }

    public int waveCount() {
        return switch (kind) {
            case COMBAT -> depth >= 6 ? 2 : 1;
            case ELITE -> 3;
            case BOSS, BRIDGE -> 1;
            case CACHE, WORKSHOP -> 0;
        };
    }

    public int rewardRanks() {
        return kind == Kind.ELITE || kind == Kind.BOSS ? 2 : 1;
    }

    public String sectorName() {
        return switch (sector) {
            case 0 -> "HECKSEKTION";
            case 1 -> "MASCHINENDECK";
            default -> "KOMMANDODECK";
        };
    }

    public String typeName() {
        return switch (kind) {
            case COMBAT -> "Patrouille";
            case ELITE -> "Schwer bewacht";
            case WORKSHOP -> "Werkstatt";
            case CACHE -> "Versorgungsraum";
            case BOSS -> "Sektorwächter";
            case BRIDGE -> "Brücke";
        };
    }
}
