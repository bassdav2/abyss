package ch.zhaw.abyss.ui.render;

import ch.zhaw.abyss.application.Cosmetics;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.EnemyKind;
import ch.zhaw.abyss.domain.RoomPlan;
import ch.zhaw.abyss.domain.Weapon;
import ch.zhaw.abyss.ui.art.DiverArt;
import ch.zhaw.abyss.ui.art.EnemyArt;
import ch.zhaw.abyss.ui.art.RoomArt;
import ch.zhaw.abyss.ui.pixel.Sprite;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Zwischenspeicher für prozedural gemalte Grafiken. Figuren werden pro Aussehen und Waffe einmal
 * gemalt, Räume pro Raumplan.
 */
public final class SpriteBank {
    private final Map<String, Map<DiverArt.Anim, List<Sprite>>> divers = new HashMap<>();
    private final Map<EnemyKind, Map<EnemyArt.Anim, List<Sprite>>> enemies =
            new EnumMap<>(EnemyKind.class);
    private RoomPlan roomPlan;
    private long roomSeed;
    private RoomArt room;

    /**
     * @param look Aussehen
     * @param weapon Waffe
     * @param diver Klasse
     * @return alle Animationen der Figur
     */
    public Map<DiverArt.Anim, List<Sprite>> diver(Cosmetics look, Weapon weapon, DiverClass diver) {
        return divers.computeIfAbsent(
                look + "|" + weapon + "|" + diver, key -> DiverArt.build(look, weapon, diver));
    }

    /**
     * @param kind Gegnerart
     * @return alle Animationen
     */
    public Map<EnemyArt.Anim, List<Sprite>> enemy(EnemyKind kind) {
        return enemies.computeIfAbsent(kind, EnemyArt::build);
    }

    /**
     * @param plan Raumplan
     * @param seed Routen-Seed
     * @return gemalter Raum, bei gleichem Plan zwischengespeichert
     */
    public RoomArt room(RoomPlan plan, long seed) {
        if (plan != roomPlan || seed != roomSeed) {
            roomPlan = plan;
            roomSeed = seed;
            room = RoomArt.build(plan, seed);
        }
        return room;
    }

    /** Malt alle Gegner vorab, damit der erste Kampf nicht ruckelt. */
    public void warmUp() {
        for (var kind : EnemyKind.values()) enemy(kind);
    }
}
