package ch.zhaw.abyss.domain;

import java.util.EnumMap;

/** Verwaltet Restdauern und Brandstärke der Zustände einer einzelnen Figur. */
public final class StatusSet {
    private final EnumMap<Status, Double> remaining = new EnumMap<>(Status.class);
    private double burnPerSecond;

    /**
     * Setzt oder verlängert einen Zustand; kürzere neue Dauern verkürzen nichts.
     *
     * @param status Zustand
     * @param seconds Dauer
     */
    void apply(Status status, double seconds) {
        remaining.merge(status, seconds, Math::max);
    }

    /**
     * Entzündet die Figur. Stärkerer Brand ersetzt schwächeren.
     *
     * @param seconds Dauer
     * @param damagePerSecond Schaden pro Sekunde
     */
    void ignite(double seconds, double damagePerSecond) {
        apply(Status.BURN, seconds);
        burnPerSecond = Math.max(burnPerSecond, damagePerSecond);
    }

    /**
     * Lässt Zeit vergehen.
     *
     * @param dt Sekunden
     * @return in diesem Schritt entstandener Brandschaden
     */
    double update(double dt) {
        double burn = active(Status.BURN) ? burnPerSecond * dt : 0;
        remaining.replaceAll((status, time) -> time - dt);
        remaining.values().removeIf(time -> time <= 0);
        if (!active(Status.BURN)) burnPerSecond = 0;
        return burn;
    }

    /**
     * @param status Zustand
     * @return {@code true}, solange der Zustand wirkt
     */
    public boolean active(Status status) {
        return remaining.getOrDefault(status, 0.0) > 0;
    }

    /**
     * @param status Zustand
     * @return Restdauer in Sekunden
     */
    public double remaining(Status status) {
        return remaining.getOrDefault(status, 0.0);
    }

    /** Entfernt alle Zustände, zum Beispiel beim Raumwechsel. */
    void clear() {
        remaining.clear();
        burnPerSecond = 0;
    }

    /**
     * @return Faktor für Bewegung und Zeitabläufe durch Kälte
     */
    double slowFactor() {
        return active(Status.CHILL) ? .6 : 1;
    }
}
