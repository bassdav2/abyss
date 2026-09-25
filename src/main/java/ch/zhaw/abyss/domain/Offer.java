package ch.zhaw.abyss.domain;

/**
 * Ein wählbares Angebot aus Bergung, Werkstatt oder Schwarzmarkt. Belohnungen haben Preis 0.
 *
 * @param type Angebotsart
 * @param item Modul bei {@link Type#ITEM}, sonst {@code null}
 * @param weapon Waffe bei {@link Type#WEAPON}, sonst {@code null}
 * @param price Schrottpreis
 */
public record Offer(Type type, Item item, Weapon weapon, int price) {
    /** Angebotsarten. */
    public enum Type {
        ITEM,
        WEAPON,
        REPAIR_KIT,
        HEAL,
        WEAPON_UPGRADE,
        SUPPLIES
    }

    /** Stellt sicher, dass Art und Nutzlast zusammenpassen. */
    public Offer {
        if (price < 0) throw new IllegalArgumentException("Negativer Preis");
        if ((type == Type.ITEM) != (item != null) || (type == Type.WEAPON) != (weapon != null))
            throw new IllegalArgumentException("Angebot passt nicht zur Art");
    }

    /**
     * @param item Modul
     * @param price Preis
     * @return Modulangebot
     */
    public static Offer item(Item item, int price) {
        return new Offer(Type.ITEM, item, null, price);
    }

    /**
     * @param weapon Waffe
     * @param price Preis
     * @return Waffenangebot
     */
    public static Offer weapon(Weapon weapon, int price) {
        return new Offer(Type.WEAPON, null, weapon, price);
    }

    /**
     * @param type Dienstleistung ohne Nutzlast
     * @param price Preis
     * @return Angebot
     */
    public static Offer service(Type type, int price) {
        return new Offer(type, null, null, price);
    }

    /**
     * @return angezeigter Titel
     */
    public String title() {
        return switch (type) {
            case ITEM -> item.title();
            case WEAPON -> weapon.title();
            case REPAIR_KIT -> "Reparaturset";
            case HEAL -> "Notreparatur";
            case WEAPON_UPGRADE -> "Waffe verbessern";
            case SUPPLIES -> "Vorräte";
        };
    }

    /**
     * @return angezeigte Wirkung
     */
    public String effect() {
        return switch (type) {
            case ITEM -> item.effect();
            case WEAPON -> weapon.description();
            case REPAIR_KIT -> "+1 Reparaturset\nQ: +35 Integrität";
            case HEAL -> "+40 Integrität sofort";
            case WEAPON_UPGRADE -> "+15 % Waffenschaden\nnächste Stufe";
            case SUPPLIES -> "+25 Integrität, +35 Energie\n+10 Schrott";
        };
    }
}
