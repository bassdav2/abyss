package ch.zhaw.abyss.application;

import ch.zhaw.abyss.domain.GameRun;
import ch.zhaw.abyss.domain.RunCheckpoint;
import ch.zhaw.abyss.domain.RunSetup;
import ch.zhaw.abyss.domain.Weapon;
import ch.zhaw.abyss.ports.GameRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Anwendungsfälle: Tauchgang beginnen, fortsetzen, sichern, Ergebnis verbuchen, im Archiv
 * freischalten und Aussehen wählen. Koordiniert Domäne und Repository, enthält aber weder
 * Spielregeln noch UI-Elemente.
 */
public final class GameService {
    private final GameRepository repository;
    private Profile profile;
    private Optional<RunCheckpoint> saved = Optional.empty();
    private GameRun run;
    private int accountedKills, accountedCores;
    private final java.util.EnumMap<Weapon, Integer> accountedWeaponKills =
            new java.util.EnumMap<>(Weapon.class);
    private CareerReport lastCareer = CareerReport.NONE;
    private boolean outcomeRecorded;
    private int lastCoreReward;
    private String storageMessage = "";
    private final List<Achievement> newAchievements = new ArrayList<>();

    /**
     * Lädt Profil und Raum-Sicherung. Defekte Dateien verhindern den Start nicht.
     *
     * @param repository Speicherzugriff
     */
    public GameService(GameRepository repository) {
        this.repository = repository;
        try {
            profile = repository.loadProfile();
        } catch (IOException e) {
            profile = Profile.fresh();
            storageMessage = e.getMessage();
        }
        try {
            saved = repository.loadCheckpoint();
            if (saved.isPresent())
                GameRun.restore(
                        saved.get(), profile.itemPool(), profile.weaponPool(saved.get().diver()));
        } catch (IOException | IllegalArgumentException e) {
            saved = Optional.empty();
            storageMessage = e.getMessage();
        }
    }

    /**
     * Seed des Tagestauchgangs: Alle, die am selben Tag tauchen, erhalten dieselbe Route.
     *
     * @param date Kalendertag
     * @return Seed in der Form JJJJMMTT
     */
    public static long dailySeed(LocalDate date) {
        return date.getYear() * 10_000L + date.getMonthValue() * 100L + date.getDayOfMonth();
    }

    /**
     * Beginnt einen neuen Tauchgang und ersetzt eine bestehende Raum-Sicherung.
     *
     * @param loadout Auswahl der Vorbereitung
     * @param seed Routen-Seed
     * @return neuer Tauchgang
     * @throws IllegalArgumentException wenn Klasse, Waffe, Modul oder Druckstufe gesperrt sind
     */
    public GameRun start(Loadout loadout, long seed) {
        if (!profile.owns(loadout.diver())
                || !(profile.owns(loadout.module()) || loadout.module() == loadout.diver().module())
                || !(profile.owns(loadout.weapon()) || loadout.weapon() == loadout.diver().weapon())
                || loadout.pressure() > profile.maxPressure())
            throw new IllegalArgumentException("Auswahl ist noch nicht freigeschaltet");
        var setup =
                new RunSetup(
                        seed,
                        loadout.diver(),
                        loadout.weapon(),
                        loadout.module(),
                        loadout.explorer(),
                        loadout.pressure(),
                        profile.itemPool(),
                        profile.weaponPool(loadout.diver()),
                        profile.bonusHealth(),
                        profile.bonusKits(),
                        profile.startSalvage(),
                        profile.meta(loadout.diver()));
        run = new GameRun(setup);
        accountedKills = 0;
        accountedCores = 0;
        accountedWeaponKills.clear();
        lastCareer = CareerReport.NONE;
        outcomeRecorded = false;
        var edit = profile.edit();
        edit.runs++;
        edit.loadout = loadout;
        edit.settings = profile.settings().withExplorer(loadout.explorer());
        profile = edit.build();
        persistProfile();
        saveRoom();
        return run;
    }

    /**
     * Setzt die gespeicherte Raum-Sicherung fort.
     *
     * @return fortgesetzter Tauchgang oder leer, wenn keine gültige Sicherung existiert
     */
    public Optional<GameRun> resume() {
        if (saved.isEmpty()) return Optional.empty();
        try {
            var checkpoint = saved.get();
            run =
                    GameRun.restore(
                            checkpoint,
                            profile.itemPool(),
                            profile.weaponPool(checkpoint.diver()),
                            profile.meta(checkpoint.diver()));
            accountedKills = run.kills();
            accountedCores = run.player().cores();
            accountedWeaponKills.clear();
            lastCareer = CareerReport.NONE;
            outcomeRecorded = false;
            return Optional.of(run);
        } catch (IllegalArgumentException e) {
            storageMessage = e.getMessage();
            return Optional.empty();
        }
    }

    /** Sichert den aktuellen Raumeingang und verbucht Zwischenstände im Profil. */
    public void saveRoom() {
        if (run == null) return;
        updateProgress(false, false);
        saved = Optional.of(run.checkpoint());
        try {
            repository.saveCheckpoint(run.checkpoint());
        } catch (IOException e) {
            storageMessage = "Raum konnte nicht gesichert werden: " + e.getMessage();
        }
    }

    /** Verbucht Sieg oder Niederlage genau einmal und entfernt die Raum-Sicherung. */
    public void recordOutcome() {
        if (run == null
                || outcomeRecorded
                || run.phase() != GameRun.Phase.DEFEAT && run.phase() != GameRun.Phase.VICTORY)
            return;
        outcomeRecorded = true;
        boolean win = run.phase() == GameRun.Phase.VICTORY;
        updateProgress(win, true);
        saved = Optional.empty();
        try {
            repository.clearCheckpoint();
        } catch (IOException e) {
            storageMessage = "Alte Raum-Sicherung konnte nicht entfernt werden.";
        }
    }

    /**
     * Beginnt nach einem Sieg den nächsten Zyklus mit demselben Build.
     *
     * @return {@code true}, wenn ein neuer Zyklus begann
     */
    public boolean nextCycle() {
        if (run == null || !run.nextCycle()) return false;
        outcomeRecorded = false;
        saveRoom();
        return true;
    }

    /**
     * Laufbahnwert der Abschüsse eines Tauchgangs. Die ersten 500 zählen voll, danach wächst der
     * Wert mit der Wurzel: Endgame-Schwärme mit zehntausenden Abschüssen bringen viel, aber nicht
     * die ganze Laufbahn auf einmal.
     *
     * @param kills Abschüsse im Tauchgang
     * @return Erfahrung dafür
     */
    static long killCredit(int kills) {
        if (kills <= KILL_CREDIT_FULL) return Math.max(0, kills);
        return KILL_CREDIT_FULL + Math.round(18 * Math.sqrt(kills - KILL_CREDIT_FULL));
    }

    /** Abschüsse pro Tauchgang, die voll in die Laufbahn zählen. */
    static final int KILL_CREDIT_FULL = 500;

    private void updateProgress(boolean win, boolean finished) {
        int reached =
                run.phase() == GameRun.Phase.DEFEAT ? run.room().depth() : run.room().depth() + 1;
        var edit = profile.edit();
        int newKills = Math.max(0, run.kills() - accountedKills);
        accountedKills = run.kills();
        int newCores = Math.max(0, run.player().cores() - accountedCores);
        accountedCores = run.player().cores();
        lastCoreReward = 0;
        var career = profile.career();
        if (finished) {
            int bonus = reached / 4 + (win ? 3 + run.pressure() : 0);
            double factor = 1 + career.sum(SkillTree.Perk.CORES, null);
            bonus =
                    (int) Math.round((bonus + run.player().cores()) * factor)
                            - run.player().cores();
            newCores += bonus;
            lastCoreReward = run.player().cores() + bonus;
        }
        // Laufbahn: Abschüsse zählen sofort, Tiefe, Wächter und Sieg beim Abschluss.
        double multiplier = 1 + .5 * run.cycle() + .2 * run.pressure();
        long gained = killCredit(run.kills()) - killCredit(run.kills() - newKills);
        if (finished) gained += 50 + reached * 25L + (reached / 6) * 150L + (win ? 600 : 0);
        gained = Math.round(gained * multiplier);
        var weaponGains = new java.util.EnumMap<Weapon, Integer>(Weapon.class);
        run.weaponKills()
                .forEach(
                        (weapon, kills) -> {
                            int fresh =
                                    (int)
                                            (killCredit(kills)
                                                    - killCredit(
                                                            accountedWeaponKills.getOrDefault(
                                                                    weapon, 0)));
                            if (fresh > 0) weaponGains.put(weapon, fresh);
                            accountedWeaponKills.put(weapon, kills);
                        });
        var diver = run.player().diver();
        var updated = career.gain(gained, diver, weaponGains);
        int rankUps = updated.rank() - career.rank();
        var masteryUps = new ArrayList<Weapon>();
        for (var weapon : weaponGains.keySet())
            if (updated.mastery(weapon) > career.mastery(weapon)) masteryUps.add(weapon);
        lastCareer =
                lastCareer.add(
                        gained,
                        updated.rank(),
                        rankUps,
                        updated.rank(diver),
                        updated.rank(diver) - career.rank(diver),
                        masteryUps);
        edit.career = updated;
        newCores += 3 * Math.max(0, rankUps);
        edit.totalKills += newKills;
        edit.cores += newCores;
        edit.bestRoom = Math.max(edit.bestRoom, reached);
        edit.bestCycle = Math.max(edit.bestCycle, run.cycle());
        edit.discovered.addAll(run.player().items().keySet());
        if (win) {
            edit.wins++;
            if (run.pressure() >= edit.maxPressure) edit.maxPressure = run.pressure() + 1;
        }
        profile = edit.build();
        // Logbuch: erst nach der Verbuchung prüfen, damit Summen wie Abschüsse aktuell sind.
        var earned = EnumSet.noneOf(Achievement.class);
        earned.addAll(profile.achievements());
        edit = profile.edit();
        for (var achievement : Achievement.values())
            if (!earned.contains(achievement) && achievement.met(run, profile, earned)) {
                earned.add(achievement);
                edit.achievements.add(achievement);
                edit.cores += achievement.reward();
                newAchievements.add(achievement);
            }
        profile = edit.build();
        persistProfile();
    }

    /**
     * Gibt neu erreichte Logbuch-Einträge einmalig zur Anzeige heraus.
     *
     * @return seit dem letzten Aufruf erreichte Einträge
     */
    public List<Achievement> takeAchievements() {
        var result = List.copyOf(newAchievements);
        newAchievements.clear();
        return result;
    }

    /**
     * Kauft einen Knoten eines Skill-Baums mit Punkten der Laufbahn.
     *
     * @param node Knoten
     * @return {@code true}, wenn gekauft und gespeichert wurde
     */
    public boolean purchase(SkillTree.SkillNode node) {
        if (!profile.career().canBuy(node, profile)) return false;
        var edit = profile.edit();
        edit.career = profile.career().with(node);
        profile = edit.build();
        persistProfile();
        return true;
    }

    /**
     * @return Laufbahn-Ergebnis des letzten Tauchgangs für die Auswertung
     */
    public CareerReport lastCareer() {
        return lastCareer;
    }

    /**
     * Laufbahn-Ergebnis eines Tauchgangs.
     *
     * @param xp gewonnene Laufbahnerfahrung
     * @param rank Laufbahnrang danach
     * @param rankUps gewonnene Laufbahnränge
     * @param classRank Rang der Klasse danach
     * @param classRankUps gewonnene Klassenränge
     * @param masteryUps Waffen mit neuer Meisterschaftsstufe
     */
    public record CareerReport(
            long xp,
            int rank,
            int rankUps,
            int classRank,
            int classRankUps,
            List<Weapon> masteryUps) {
        /** Noch kein Ergebnis. */
        public static final CareerReport NONE = new CareerReport(0, 0, 0, 0, 0, List.of());

        /** Kopiert die Liste. */
        public CareerReport {
            masteryUps = List.copyOf(masteryUps);
        }

        CareerReport add(
                long gained, int rank, int ups, int classRank, int classUps, List<Weapon> mastery) {
            var weapons = new ArrayList<>(masteryUps);
            for (var weapon : mastery) if (!weapons.contains(weapon)) weapons.add(weapon);
            return new CareerReport(
                    xp + gained, rank, rankUps + ups, classRank, classRankUps + classUps, weapons);
        }
    }

    /**
     * Kauft eine Freischaltung im Archiv.
     *
     * @param unlock Freischaltung
     * @return {@code true}, wenn gekauft und gespeichert wurde
     */
    public boolean purchase(Unlock unlock) {
        if (!profile.canBuy(unlock)) return false;
        var edit = profile.edit();
        edit.cores -= unlock.cost();
        edit.unlocks.add(unlock.id());
        profile = edit.build();
        persistProfile();
        return true;
    }

    /**
     * Speichert ein neues Aussehen, sofern alle Optionen freigeschaltet sind.
     *
     * @param cosmetics Aussehen
     * @return {@code true}, wenn übernommen
     */
    public boolean cosmetics(Cosmetics cosmetics) {
        if (!profile.owns(Cosmetics.unlockId("suit", cosmetics.suit()))
                || !profile.owns(Cosmetics.unlockId("helmet", cosmetics.helmet()))
                || !profile.owns(Cosmetics.unlockId("visor", cosmetics.visor()))
                || !profile.owns(Cosmetics.unlockId("trim", cosmetics.trim()))) return false;
        var edit = profile.edit();
        edit.cosmetics = cosmetics;
        profile = edit.build();
        persistProfile();
        return true;
    }

    /**
     * Merkt sich die Auswahl der Vorbereitung ohne zu starten.
     *
     * @param loadout Auswahl
     */
    public void rememberLoadout(Loadout loadout) {
        var edit = profile.edit();
        edit.loadout = loadout;
        profile = edit.build();
        persistProfile();
    }

    /**
     * Übernimmt neue Einstellungen.
     *
     * @param settings Einstellungen
     */
    public void settings(Settings settings) {
        profile = profile.withSettings(settings);
        persistProfile();
    }

    private void persistProfile() {
        try {
            repository.saveProfile(profile);
        } catch (IOException e) {
            storageMessage = "Profil konnte nicht gespeichert werden: " + e.getMessage();
        }
    }

    /**
     * @return aktuelles Profil
     */
    public Profile profile() {
        return profile;
    }

    /**
     * @return gespeicherte Raum-Sicherung
     */
    public Optional<RunCheckpoint> saved() {
        return saved;
    }

    /**
     * @return laufender Tauchgang oder {@code null}
     */
    public GameRun run() {
        return run;
    }

    /**
     * @return beim letzten Abschluss insgesamt gutgeschriebene Datenkerne
     */
    public int lastCoreReward() {
        return lastCoreReward;
    }

    /**
     * Entnimmt die letzte Speichermeldung.
     *
     * @return Meldung oder leerer Text
     */
    public String takeStorageMessage() {
        var message = storageMessage;
        storageMessage = "";
        return message;
    }
}
