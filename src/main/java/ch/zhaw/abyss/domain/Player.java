package ch.zhaw.abyss.domain;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Die gesteuerte Figur mit Ausrüstung, Ressourcen und Kampfzeitgebern. Abgeleitete Werte liegen in
 * {@link StatSheet} und werden bei jeder Ausrüstungsänderung neu berechnet.
 */
public final class Player extends Actor {
    /** Standardhöhe der Trefferzone. */
    public static final double HEIGHT = 112;

    final DiverClass diver;
    final ActiveModule module;
    final boolean explorer;
    final double bonusHealth;
    final EnumMap<Item, Integer> items = new EnumMap<>(Item.class);
    Weapon weapon;
    int weaponLevel;
    StatSheet stats;

    double energy, shield;
    int salvage, repairKits, cores;
    boolean reviveUsed;
    int barrierCharges, killsTowardRush, rushStacks;
    double healthPenalty;

    Swing swing;
    double swingTime, comboTimer, lungeVelocity;
    int comboStep, swingIndex;
    boolean swingFired, afterburner, chainedThisSwing;
    final Set<Long> swingHits = new HashSet<>();
    final Set<SupplyCrate> swingCrates = new HashSet<>();
    final Set<Long> dashHits = new HashSet<>();

    double dashTime, dashCooldown, invulnerableTime, abilityCooldown;
    double shieldTime, overdriveTime, droneTime, droneCooldown, shieldRegenDelay;
    double coyoteTime, jumpBuffer;
    int airJumps;
    boolean airDashUsed, slamming, jumpCut;
    double lastHitTime = 99;

    Player(DiverClass diver, Weapon weapon, ActiveModule module, boolean explorer, double bonus) {
        super(1, 170, GameRun.FLOOR, 48, HEIGHT, 100);
        this.diver = diver;
        this.weapon = weapon;
        this.module = module;
        this.explorer = explorer;
        this.bonusHealth = bonus + (explorer ? 50 : 0);
        recompute();
        health = maxHealth;
        energy = stats.maxEnergy();
        shield = stats.maxShield();
        repairKits = diver == DiverClass.MECHANIC ? 2 : 1;
    }

    /** Berechnet abgeleitete Werte neu und hält Ressourcen in gültigen Grenzen. */
    void recompute() {
        stats = StatSheet.compute(diver, weapon, weaponLevel, items, bonusHealth - healthPenalty);
        maxHealth = stats.maxHealth();
        health = Math.min(health, maxHealth);
        energy = Math.min(energy, stats.maxEnergy());
        shield = Math.min(shield, stats.maxShield());
        repairKits = Math.min(repairKits, stats.maxRepairKits());
    }

    /**
     * Installiert eine weitere Stufe.
     *
     * @param item Modul
     * @throws IllegalStateException bei bereits maximaler Stufe
     */
    void install(Item item) {
        int current = stacks(item);
        if (current >= item.maxStacks())
            throw new IllegalStateException("Modul bereits auf Höchststufe");
        items.put(item, current + 1);
        recompute();
        if (item == Item.MEDICAL) heal(30);
        if (item == Item.CAPACITOR) energy = Math.min(stats.maxEnergy(), energy + 20);
        if (item == Item.TOOLBELT) repairKits = Math.min(stats.maxRepairKits(), repairKits + 1);
        if (item == Item.SHIELD_CELL) shield = stats.maxShield();
    }

    /**
     * Heilt ohne das Maximum zu überschreiten.
     *
     * @param amount Heilmenge, negative Werte werden ignoriert
     * @return tatsächlich geheilte Menge
     */
    double heal(double amount) {
        double before = health;
        health = Math.min(maxHealth, health + Math.max(0, amount));
        return health - before;
    }

    /**
     * @param amount zusätzliche Energie
     */
    void addEnergy(double amount) {
        energy = Math.max(0, Math.min(stats.maxEnergy(), energy + amount));
    }

    /**
     * @param amount Schrott; wird mit dem Schrottfaktor verrechnet
     */
    int addSalvage(double amount) {
        int gained = (int) Math.round(amount * stats.scrapGain());
        salvage = Math.min(9999, salvage + gained);
        return gained;
    }

    /**
     * @return Klasse der Figur
     */
    public DiverClass diver() {
        return diver;
    }

    /**
     * @return aktuelle Waffe
     */
    public Weapon weapon() {
        return weapon;
    }

    /**
     * @return Werkstattstufe der Waffe
     */
    public int weaponLevel() {
        return weaponLevel;
    }

    /**
     * @return aktives Modul
     */
    public ActiveModule module() {
        return module;
    }

    /**
     * @return {@code true} im zugänglicheren Entdeckermodus
     */
    public boolean explorer() {
        return explorer;
    }

    /**
     * @return abgeleitete Kampfwerte
     */
    public StatSheet stats() {
        return stats;
    }

    /**
     * @return aktuelle Energie
     */
    public double energy() {
        return energy;
    }

    /**
     * @return maximale Energie
     */
    public double maxEnergy() {
        return stats.maxEnergy();
    }

    /**
     * @return aktueller Energieschild
     */
    public double shield() {
        return shield;
    }

    /**
     * @return Schrott des Runs
     */
    public int salvage() {
        return salvage;
    }

    /**
     * @return mitgeführte Reparatursets
     */
    public int repairKits() {
        return repairKits;
    }

    /**
     * @return in diesem Run gesammelte Datenkerne
     */
    public int cores() {
        return cores;
    }

    /**
     * @return {@code true}, wenn die Notfallkapsel bereits ausgelöst hat
     */
    public boolean reviveUsed() {
        return reviveUsed;
    }

    /**
     * @param item Modul
     * @return installierte Stufe, 0 wenn nicht vorhanden
     */
    public int stacks(Item item) {
        return items.getOrDefault(item, 0);
    }

    /**
     * @return Kopie aller installierten Module
     */
    public Map<Item, Integer> items() {
        return Map.copyOf(items);
    }

    /**
     * @return laufender Angriff oder {@code null}
     */
    public Swing swing() {
        return swing;
    }

    /**
     * @return verstrichene Zeit im laufenden Angriff
     */
    public double swingTime() {
        return swingTime;
    }

    /**
     * @return Index des nächsten Kombinationsschlags
     */
    public int comboStep() {
        return comboStep;
    }

    /**
     * @return verbleibende Ausweichzeit
     */
    public double dashTime() {
        return dashTime;
    }

    /**
     * @return Abklingzeit des Ausweichens
     */
    public double dashCooldown() {
        return dashCooldown;
    }

    /**
     * @return Unverwundbarkeit nach Treffer oder Ausweichen
     */
    public double invulnerableTime() {
        return invulnerableTime;
    }

    /**
     * @return Abklingzeit des aktiven Moduls
     */
    public double abilityCooldown() {
        return abilityCooldown;
    }

    /**
     * @return aktuelle volle Abklingzeit des Moduls nach Modulen
     */
    public double abilityCooldownTotal() {
        return module.cooldown() * stats.cooldown() * stats.moduleCooldown();
    }

    /**
     * @return verbleibender Druckschild
     */
    public double shieldTime() {
        return shieldTime;
    }

    /**
     * @return verbleibende Überlastung
     */
    public double overdriveTime() {
        return overdriveTime;
    }

    /**
     * @return verbleibende Einsatzzeit der Begleitdrohne
     */
    public double droneTime() {
        return droneTime;
    }

    /**
     * @return {@code true} während eines Bodenstampfers
     */
    public boolean slamming() {
        return slamming;
    }

    /**
     * @return Stufen des Tiefenrauschs
     */
    public int rushStacks() {
        return rushStacks;
    }

    /**
     * @return aktueller Angriffsschaden des nächsten Schlags ohne Kritik
     */
    public double attackDamage() {
        var next = weapon.combo().get(comboStep % weapon.combo().size());
        return next.damage() * stats.damage() * (explorer ? 1.2 : 1);
    }

    /**
     * @return aktuelle Grundreichweite
     */
    public double attackReach() {
        return weapon.combo().getFirst().reach() * stats.reach();
    }

    /**
     * @return {@code true}, solange ein Bodenstampfer oder Angriff läuft
     */
    public boolean attacking() {
        return swing != null;
    }
}
