package ch.zhaw.abyss.domain;

public enum Upgrade {
    SERVO("Servoverstärker", "+18 % Werkzeugschaden", "Mehr Kraft hinter jedem Schlag.", "amber"),
    PLATING(
            "Verbundpanzerung",
            "−12 % eingehender Schaden",
            "Geschichteter Stahl nimmt die Wucht.",
            "blue"),
    CAPACITOR(
            "Kondensator",
            "+20 Energie\n+15 % Modulschaden",
            "Speichert die Energie des Bootes.",
            "cyan"),
    MEDICAL(
            "Notfallreserve",
            "+20 maximale Integrität\n+30 Heilung",
            "Ein zusätzlicher Puffer für den nächsten Raum.",
            "red"),
    COOLANT(
            "Kühlkreislauf",
            "−12 % Abklingzeit\nfür Ausweichen und Modul",
            "Ein kühler Antrieb reagiert schneller.",
            "blue"),
    RECOVERY(
            "Rückgewinnung",
            "+3 Integrität pro besiegtem Gegner",
            "Restenergie wird zu deiner Reserve.",
            "green"),
    LANCE(
            "Teleskopstange",
            "+25 Angriffsreichweite",
            "Erreiche Drohnen und halte Abstand.",
            "amber"),
    OVERCLOCK(
            "Übertakter",
            "−8 % Zeit zwischen Schlägen",
            "Der Werkzeugantrieb läuft über Nenndrehzahl.",
            "red"),
    THRUSTER(
            "Strömungsantrieb",
            "+10 % Lauf- und Dash-Tempo",
            "Eine kleine Turbine verändert deinen Rhythmus.",
            "blue"),
    SIPHON(
            "Energiesiphon",
            "+4 Energie pro Gegner\n+1 Energie pro Sekunde",
            "Speist das aktive Modul aus Restladung.",
            "cyan"),
    ARC_COIL(
            "Teslaspule",
            "Dritter Treffer: Kettenblitz\n15 Schaden pro Stufe",
            "Springt auf einen zweiten Gegner in der Nähe.",
            "cyan"),
    REGEN(
            "Reparaturschwarm",
            "+7 Integrität nach jedem Raum",
            "Kleine Wartungsdrohnen schliessen die Lecks.",
            "green");

    public static final int MAX_STACKS = 3;
    private final String title, effect, description, color;

    Upgrade(String title, String effect, String description, String color) {
        this.title = title;
        this.effect = effect;
        this.description = description;
        this.color = color;
    }

    public String title() {
        return title;
    }

    public String effect() {
        return effect;
    }

    public String description() {
        return description;
    }

    public String color() {
        return color;
    }
}
