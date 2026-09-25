package ch.zhaw.abyss.domain;

/** Zeitlich begrenzte Zustände auf Figuren. */
public enum Status {
    /** Schaden über Zeit. */
    BURN,
    /** Verlangsamt Bewegung und Angriffsvorbereitung. */
    CHILL,
    /** Vollständig eingefroren; Bosse werden stattdessen verlangsamt. */
    FREEZE,
    /** Erleidet 50 % mehr Schaden. */
    MARK,
    /** Kurze Lähmung durch Strom. */
    SHOCK
}
