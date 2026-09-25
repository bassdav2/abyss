package ch.zhaw.abyss.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Aggregat eines Tauchgangs und Konsistenzgrenze aller Spielregeln. Läuft ohne JavaFX, Audio oder
 * Dateizugriff. Kampfregeln delegiert es an {@link Combat}, Gegnerentscheidungen an die
 * Verhaltensstrategien der {@link EnemyKind}.
 */
public final class GameRun {
    /** Breite eines Bildschirms in Welteinheiten. */
    public static final double VIEW_WIDTH = 1600;

    /** Höhe eines Raums in Welteinheiten. */
    public static final double HEIGHT = 900;

    /** Fusshöhe des Bodens. */
    public static final double FLOOR = 620;

    /** Sprunggeschwindigkeit der spielenden Figur. */
    static final double JUMP = 880;

    /** Lauftempo der spielenden Figur ohne Module. */
    static final double RUN = 320;

    /** Ablauf eines Tauchgangs. */
    public enum Phase {
        RUNNING,
        ROOM_CLEARED,
        DEFEAT,
        VICTORY
    }

    final RunSetup setup;
    final Player player;
    final List<Enemy> enemies = new ArrayList<>();
    final List<Projectile> projectiles = new ArrayList<>();
    final List<Hazard> hazards = new ArrayList<>();
    final List<Pickup> pickups = new ArrayList<>();
    final List<SupplyCrate> crates = new ArrayList<>();
    final List<GameEvent> events = new ArrayList<>();
    final Combat combat = new Combat(this);
    final PlayerMotor motor = new PlayerMotor(this);
    final Arsenal arsenal = new Arsenal(this);
    final Ballistics ballistics = new Ballistics(this);
    final Loot loot = new Loot(this);
    final Rewards rewards = new Rewards(this);
    final List<Fixture> fixtures = new ArrayList<>();
    final Machinery machinery = new Machinery(this);
    Random rng = new Random(1);
    private final List<Integer> route = new ArrayList<>();
    private final Set<RoomCondition> clearedConditions = EnumSet.noneOf(RoomCondition.class);
    private final Set<Synergy> resonances = EnumSet.noneOf(Synergy.class);
    private RoomGenerator generator;
    private RoomPlan room;
    private Phase phase;
    private int cycle, kills, wave;
    int smugglersCaught;
    private double breach, lastBreachSpawn, list;
    private int listSide = 1;

    /** Dauer eines Hüllenbruchs in Sekunden. */
    public static final double BREACH_TIME = 40;

    private long nextId = 10;
    private double elapsed, roomTime, waveDelay = -1;
    double clearTime;
    private RunCheckpoint checkpoint;

    /**
     * Beginnt einen neuen Tauchgang im Heck.
     *
     * @param setup Startbedingungen
     */
    public GameRun(RunSetup setup) {
        this.setup = setup;
        player =
                new Player(
                        setup.diver(),
                        setup.weapon(),
                        setup.module(),
                        setup.explorer(),
                        setup.bonusHealth());
        player.repairKits =
                Math.min(player.stats.maxRepairKits(), player.repairKits + setup.bonusKits());
        player.salvage = setup.startSalvage();
        generator = new RoomGenerator(setup.seed(), 0, setup.pressure());
        enterRoom(generator.room(0, 0));
    }

    /**
     * Rekonstruiert einen gespeicherten Raumeingang.
     *
     * @param saved Raum-Sicherung
     * @param itemPool aktuell freigeschaltete Module
     * @param weaponPool aktuell freigeschaltete Waffen
     * @return fortgesetzter Tauchgang
     * @throws IllegalArgumentException bei unplausiblen oder manipulierten Werten
     */
    public static GameRun restore(RunCheckpoint saved, Set<Item> itemPool, Set<Weapon> weaponPool) {
        if (saved == null
                || saved.diver() == null
                || saved.weapon() == null
                || saved.module() == null
                || saved.cycle() < 0
                || saved.pressure() < 0
                || saved.pressure() > RunSetup.MAX_PRESSURE
                || saved.depth() < 0
                || saved.depth() >= RoomGenerator.ROOM_COUNT
                || saved.branch() < 0
                || saved.branch() > 1
                || saved.kills() < 0
                || saved.cores() < 0
                || saved.weaponLevel() < 0
                || saved.weaponLevel() > Weapon.MAX_LEVEL
                || !Double.isFinite(saved.health())
                || !Double.isFinite(saved.energy())
                || !Double.isFinite(saved.elapsed())
                || !Double.isFinite(saved.healthPenalty())
                || !Double.isFinite(saved.bonusHealth())
                || saved.healthPenalty() < 0
                || saved.bonusHealth() < 0
                || saved.elapsed() < 0
                || saved.salvage() < 0
                || saved.salvage() > 9999
                || saved.repairKits() < 0
                || saved.rushStacks() < 0)
            throw new IllegalArgumentException("Ungültiger Spielstand");
        var setup =
                new RunSetup(
                        saved.seed(),
                        saved.diver(),
                        saved.weapon(),
                        saved.module(),
                        saved.explorer(),
                        saved.pressure(),
                        itemPool,
                        weaponPool,
                        saved.bonusHealth(),
                        0,
                        0);
        var run = new GameRun(setup);
        var p = run.player;
        p.items.clear();
        for (var entry : saved.items().entrySet()) {
            if (entry.getKey() == null
                    || entry.getValue() < 0
                    || entry.getValue() > entry.getKey().maxStacks())
                throw new IllegalArgumentException("Ungültige Modulstufe");
            if (entry.getValue() > 0) p.items.put(entry.getKey(), entry.getValue());
        }
        p.weaponLevel = saved.weaponLevel();
        p.healthPenalty = saved.healthPenalty();
        p.recompute();
        run.resonances.addAll(Synergy.activeIn(p.items));
        if (saved.health() <= 0
                || saved.health() > p.maxHealth + 1e-6
                || saved.energy() < 0
                || saved.energy() > p.maxEnergy() + 1e-6
                || saved.repairKits() > p.stats.maxRepairKits())
            throw new IllegalArgumentException("Ungültige Ressourcen");
        run.cycle = saved.cycle();
        run.generator = new RoomGenerator(saved.seed(), saved.cycle(), saved.pressure());
        if (saved.branch() >= run.generator.choices(saved.depth()).size())
            throw new IllegalArgumentException("Nicht erreichbarer Raum");
        if (saved.route().size() != saved.depth() + 1
                || saved.route().stream().anyMatch(b -> b == null || b < 0 || b > 1)
                || saved.route().getLast() != saved.branch())
            throw new IllegalArgumentException("Ungültige Route");
        for (int depth = 0; depth < saved.route().size(); depth++)
            run.generator.room(depth, saved.route().get(depth));
        p.health = saved.health();
        p.energy = saved.energy();
        p.salvage = saved.salvage();
        p.repairKits = saved.repairKits();
        p.cores = saved.cores();
        p.reviveUsed = saved.reviveUsed();
        p.rushStacks = saved.rushStacks();
        run.kills = saved.kills();
        run.elapsed = saved.elapsed();
        run.route.clear();
        run.route.addAll(saved.route().subList(0, saved.depth()));
        run.enterRoom(run.generator.room(saved.depth(), saved.branch()));
        run.events.clear();
        return run;
    }

    // ---------------------------------------------------------------------------------------------
    // Simulation
    // ---------------------------------------------------------------------------------------------

    /**
     * Führt einen Simulationsschritt aus.
     *
     * @param seconds Schrittweite, höchstens 1/30 Sekunde
     * @param input Eingaben dieses Schritts
     */
    public void update(double seconds, InputFrame input) {
        if (seconds <= 0 || !Double.isFinite(seconds)) return;
        if (seconds > 1.0 / 30 + 1e-8)
            throw new IllegalArgumentException("Simulationsschritt zu gross");
        if (phase == Phase.DEFEAT || phase == Phase.VICTORY) return;
        elapsed += seconds;
        roomTime += seconds;
        double burn = player.statuses.update(seconds);
        if (burn > 0 && phase == Phase.RUNNING) player.health = Math.max(0, player.health - burn);
        updatePlayer(seconds, input);
        machinery.update(seconds);
        if (phase == Phase.RUNNING && room.condition() == RoomCondition.LIST) updateList(seconds);
        if (phase == Phase.RUNNING) {
            updateHazards(seconds);
            updateEnemies(seconds);
            ballistics.update(seconds);
            enemies.removeIf(e -> !e.alive());
            updateWaves(seconds);
        } else ballistics.update(seconds);
        loot.update(seconds);
        for (var crate : crates) crate.hitTime = Math.max(0, crate.hitTime - seconds);
        if (!player.alive() && phase != Phase.DEFEAT && phase != Phase.VICTORY) {
            phase = Phase.DEFEAT;
            player.swing = null;
            emit(GameEvent.at(GameEvent.Type.DEFEAT, player.x, player.y));
        }
    }

    private void updatePlayer(double dt, InputFrame input) {
        if (input.heal()) useRepairKit();
        motor.tick(dt);
        arsenal.updateDrone(dt);
        motor.move(dt, input);
        arsenal.update(dt, input);
        player.animationTime += dt * (Math.abs(player.vx) > 1 ? 1 : .6);
    }

    private void updateHazards(double dt) {
        for (var hazard : hazards) {
            hazard.update(dt);
            if (hazard.active() && hazard.bounds().intersects(player.bounds())) {
                if (combat.hurtPlayer(hazard.damage() * enemyDamage(), hazard.x(), null, false)
                        && hazard.kind() == Hazard.Kind.FIRE) player.statuses.ignite(2, 3);
            }
        }
        hazards.removeIf(Hazard::expired);
    }

    private void updateEnemies(double dt) {
        for (int i = 0; i < enemies.size(); i++) {
            var e = enemies.get(i);
            if (!e.alive()) continue;
            e.hurtTime = Math.max(0, e.hurtTime - dt);
            e.animationTime += dt;
            double burn = e.statuses.update(dt);
            if (burn > 0) combat.hitEnemy(e, burn, Combat.Source.BURN, 0, e.x);
            if (!e.alive()) continue;
            e.regenDelay -= dt;
            if (e.affix == Affix.REGENERATING && e.regenDelay <= 0)
                e.health = Math.min(e.maxHealth, e.health + e.maxHealth * .05 * dt);
            if (e.affix == Affix.SHIELDED && e.regenDelay <= 0)
                e.eliteShield = Math.min(e.maxHealth * .35, e.eliteShield + e.maxHealth * .1 * dt);
            if (e.statuses.active(Status.FREEZE)) {
                e.vx = 0;
                if (e.flying()) e.vy = 0;
            } else if (e.statuses.active(Status.SHOCK) && !e.kind.boss()) e.vx = 0;
            else e.behavior.update(e, this, dt * e.statuses.slowFactor());
            double own = e.vx;
            if (e.state != Enemy.State.HIDDEN) {
                e.vx = own + e.knockVx + machinery.push(e);
                Physics.move(e, layout(), dt);
                e.vx = own;
            }
            e.knockVx *= Math.exp(-9 * dt);
        }
    }

    private void updateWaves(double dt) {
        if (phase != Phase.RUNNING || !player.alive()) return;
        if (room.condition() == RoomCondition.BREACH) {
            updateBreach(dt);
            return;
        }
        if (enemies.stream().anyMatch(Enemy::alive)) return;
        if (wave + 1 < room.waveCount()) {
            if (waveDelay < 0) {
                waveDelay = 1.8;
                emit(
                        new GameEvent(
                                GameEvent.Type.REINFORCEMENTS,
                                player.x,
                                FLOOR,
                                wave + 2,
                                "Weitere Patrouille nähert sich"));
            } else {
                waveDelay -= dt;
                if (waveDelay <= 0) {
                    wave++;
                    spawnWave();
                    waveDelay = -1;
                }
            }
        } else clearRoom();
    }

    // ---------------------------------------------------------------------------------------------
    // Räume und Belohnungen
    // ---------------------------------------------------------------------------------------------

    private void enterRoom(RoomPlan next) {
        room = next;
        nextId = 10;
        roomTime = 0;
        clearTime = 0;
        enemies.clear();
        projectiles.clear();
        hazards.clear();
        pickups.clear();
        crates.clear();
        fixtures.clear();
        breach = 0;
        lastBreachSpawn = 0;
        list = 0;
        listSide = 1;
        wave = 0;
        waveDelay = -1;
        rewards.reset();
        rng =
                new Random(
                        setup.seed() * 31
                                + next.depth() * 7919L
                                + next.branch() * 131L
                                + cycle * 1_000_003L);
        var p = player;
        p.x = 170;
        p.y = FLOOR;
        p.vx = 0;
        p.vy = 0;
        p.facing = 1;
        p.grounded = true;
        p.platform = null;
        p.swing = null;
        p.slamming = false;
        p.dashTime = 0;
        p.dashCooldown = 0;
        p.invulnerableTime = 1;
        p.shieldTime = 0;
        p.overdriveTime = 0;
        p.droneTime = 0;
        p.abilityCooldown = 0;
        p.hurtTime = 0;
        p.lungeVelocity = 0;
        p.comboStep = 0;
        p.comboTimer = 0;
        p.animationTime = 0;
        p.afterburner = false;
        p.statuses.clear();
        p.barrierCharges = p.stacks(Item.BARRIER);
        route.add(next.branch());
        for (var slot : next.hazards()) hazards.add(Hazard.periodic(slot.x(), slot.kind()));
        for (var slot : next.crates()) crates.add(new SupplyCrate(slot.x(), slot.y(), slot.kind()));
        for (var slot : next.fixtures())
            fixtures.add(
                    new Fixture(
                            slot.kind(), slot.x(), slot.direction(), next.sector(), slot.start()));
        phase = Phase.RUNNING;
        checkpoint =
                new RunCheckpoint(
                        setup.seed(),
                        cycle,
                        setup.pressure(),
                        next.depth(),
                        next.branch(),
                        p.diver,
                        p.weapon,
                        p.weaponLevel,
                        p.module,
                        p.explorer,
                        p.health,
                        p.energy,
                        p.salvage,
                        p.repairKits,
                        p.items,
                        kills,
                        elapsed,
                        route,
                        p.cores,
                        p.reviveUsed,
                        p.rushStacks,
                        p.healthPenalty,
                        setup.bonusHealth());
        emit(new GameEvent(GameEvent.Type.DOOR, 90, FLOOR, next.depth(), next.title()));
        if (next.condition() != RoomCondition.NONE)
            emit(new GameEvent(GameEvent.Type.CONDITION, 90, FLOOR, 0, next.condition().name()));
        if (next.hostile()) spawnWave();
        else clearRoom();
    }

    /** Hüllenbruch: Gegner strömen im Takt nach, bis das Leck nach 40 Sekunden dicht ist. */
    private void updateBreach(double dt) {
        double before = breach;
        breach += dt;
        if (breach >= BREACH_TIME) {
            for (var e : enemies)
                if (e.alive()) {
                    e.escaped = true;
                    emit(new GameEvent(GameEvent.Type.MACHINE, e.x, e.y, 0, "FLEE"));
                }
            emit(new GameEvent(GameEvent.Type.MACHINE, player.x, FLOOR, 0, "SEALED"));
            clearRoom();
            return;
        }
        long alive = enemies.stream().filter(Enemy::alive).count();
        boolean tick = Math.floor(before / 9) != Math.floor(breach / 9);
        if ((tick || alive == 0 && breach - lastBreachSpawn > 3) && alive < 6) {
            wave = (wave + 1) % room.waveCount();
            lastBreachSpawn = breach;
            emit(
                    new GameEvent(
                            GameEvent.Type.REINFORCEMENTS,
                            player.x,
                            FLOOR,
                            wave + 1,
                            "Durch das Leck dringen Gegner ein"));
            spawnWave();
        }
    }

    /** Taktlänge der Schlagseite in Sekunden. */
    static final double LIST_CYCLE = 11;

    /** Schlagseite: alle 11 Sekunden krängt das Boot, vorher knarzt und warnt es. */
    private void updateList(double dt) {
        double before = list % LIST_CYCLE;
        list += dt;
        double t = list % LIST_CYCLE;
        if (before < 7.5 && t >= 7.5) {
            listSide = -listSide;
            emit(new GameEvent(GameEvent.Type.MACHINE, player.x, FLOOR, listSide, "LIST_WARN"));
            for (int i = 0; i < 3; i++) {
                double x = 200 + rng.nextDouble() * (layout().width() - 400);
                hazards.add(new Hazard(x, 110, Hazard.Kind.BARRAGE, -1.5, 1.9));
            }
        }
        if (before < 9 && t >= 9)
            emit(new GameEvent(GameEvent.Type.MACHINE, player.x, FLOOR, listSide, "LIST"));
    }

    /**
     * @return Neigung des Bootes -1 bis 1 während einer Krängung, sonst 0
     */
    public double listTilt() {
        if (room.condition() != RoomCondition.LIST || phase != Phase.RUNNING) return 0;
        double t = list % LIST_CYCLE;
        if (t >= 9) return listSide * Math.sin((t - 9) / 2 * Math.PI);
        if (t >= 7.5) return listSide * .08 * Math.sin(t * 40);
        return 0;
    }

    /**
     * @return verbleibende Sekunden eines Hüllenbruchs, sonst 0
     */
    public double breachRemaining() {
        return room.condition() == RoomCondition.BREACH && phase == Phase.RUNNING
                ? Math.max(0, BREACH_TIME - breach)
                : 0;
    }

    /**
     * @return Wasserstand eines Hüllenbruchs 0 bis 1; nach dem Abdichten wird abgepumpt
     */
    public double breachLevel() {
        if (room.condition() != RoomCondition.BREACH) return 0;
        return phase == Phase.RUNNING
                ? Math.min(1, breach / BREACH_TIME)
                : Math.max(0, 1 - clearTime / 3);
    }

    /**
     * @return abgeschossene Schmugglerdrohnen in diesem Tauchgang
     */
    public int smugglersCaught() {
        return smugglersCaught;
    }

    private void spawnWave() {
        var spawns = room.waves().get(wave);
        double strength = enemyHealth();
        for (int i = 0; i < spawns.size(); i++) {
            var spawn = spawns.get(i);
            double x = spawn.x(), y = spawn.y();
            if (wave > 0 && !spawn.kind().boss() && spawn.kind() != EnemyKind.TURRET) {
                int side = i % 2 == 0 ? 1 : -1;
                if (player.x + side * 400 > layout().width() - 150 || player.x + side * 400 < 150)
                    side = -side;
                x = clamp(player.x + side * (380 + 95 * (i / 2)), 150, layout().width() - 150);
                if (!spawn.kind().flying() && spawn.kind() != EnemyKind.EEL) y = FLOOR;
            }
            var enemy =
                    new Enemy(nextId++, spawn.kind(), spawn.affix(), x, y, strength, 1.1 + i * .35);
            enemies.add(enemy);
            emit(new GameEvent(GameEvent.Type.SPAWN, x, y, spawn.kind().ordinal(), ""));
            if (spawn.kind().boss())
                emit(
                        new GameEvent(
                                GameEvent.Type.BOSS_INTRO,
                                x,
                                y,
                                spawn.kind().ordinal(),
                                spawn.kind().title()));
        }
    }

    /**
     * Ruft einen Begleiter eines Gegners herbei.
     *
     * @param kind Art des Begleiters
     * @param x horizontale Position
     * @param y Fusshöhe
     * @param parent rufender Gegner
     * @return neuer Gegner
     */
    Enemy summon(EnemyKind kind, double x, double y, Enemy parent) {
        var enemy =
                new Enemy(
                        nextId++,
                        kind,
                        Affix.NONE,
                        clamp(x, 120, layout().width() - 120),
                        y,
                        enemyHealth() * .8,
                        .9);
        enemies.add(enemy);
        parent.children.add(enemy.id);
        emit(new GameEvent(GameEvent.Type.SPAWN, enemy.x, y, kind.ordinal(), ""));
        return enemy;
    }

    private void clearRoom() {
        projectiles.removeIf(q -> !q.friendly);
        hazards.removeIf(h -> h.life() != Double.POSITIVE_INFINITY);
        player.heal(8 * player.stacks(Item.REGEN));
        if (room.kind() == RoomPlan.Kind.BRIDGE) {
            phase = Phase.VICTORY;
            player.swing = null;
            player.dashTime = 0;
            player.shieldTime = 0;
            player.vx = 0;
            loot.collectAll();
            emit(GameEvent.at(GameEvent.Type.VICTORY, player.x, player.y));
            return;
        }
        phase = Phase.ROOM_CLEARED;
        clearTime = 0;
        clearedConditions.add(room.condition());
        if (room.salvageReward() > 0)
            dropScrap(layout().rewardX(), FLOOR - 60, room.salvageReward());
        rewards.open(room);
        if (room.hostile())
            emit(new GameEvent(GameEvent.Type.ROOM_CLEAR, player.x, player.y, 0, room.title()));
    }

    /**
     * Nimmt ein Angebot an. Belohnungsangebote schliessen die übrigen; gekaufte Händlerware
     * verschwindet einzeln.
     *
     * @param offer eines der aktuellen {@link #offers()}
     * @return {@code true}, wenn das Angebot angenommen und abgerechnet wurde
     */
    public boolean take(Offer offer) {
        boolean taken = rewards.take(offer);
        announceResonances();
        return taken;
    }

    /**
     * Nimmt einen Handel der Druckkapelle an. Pro Kapelle ist genau ein Handel möglich.
     *
     * @param deal einer der aktuellen {@link #deals()}
     * @return {@code true}, wenn Opfer und Belohnung verbucht wurden
     */
    public boolean acceptDeal(ShrineDeal deal) {
        boolean accepted = rewards.accept(deal);
        announceResonances();
        return accepted;
    }

    /** Meldet jede neu vervollständigte Resonanz genau einmal. */
    private void announceResonances() {
        for (var synergy : Synergy.activeIn(player.items))
            if (resonances.add(synergy))
                emit(new GameEvent(GameEvent.Type.SYNERGY, player.x, player.y, 0, synergy.title()));
    }

    /**
     * Setzt ein Reparaturset ein.
     *
     * @return {@code true}, wenn Integrität fehlte und ein Set verbraucht wurde
     */
    public boolean useRepairKit() {
        var p = player;
        if ((phase != Phase.RUNNING && phase != Phase.ROOM_CLEARED)
                || !p.alive()
                || p.repairKits <= 0
                || p.health >= p.maxHealth) return false;
        p.repairKits--;
        p.heal(35);
        emit(
                new GameEvent(
                        GameEvent.Type.SUPPLY, p.x, p.y - 65, 35, "Reparaturset · +35 Integrität"));
        return true;
    }

    /**
     * Kostenlose Werkstattreparatur, einmal pro Werkstatt.
     *
     * @return {@code true}, wenn repariert wurde
     */
    public boolean repair() {
        return rewards.repair();
    }

    /**
     * Was die Interaktionstaste an der aktuellen Position auslösen würde.
     *
     * @return Interaktion
     */
    public Interaction interaction() {
        if (phase != Phase.ROOM_CLEARED && phase != Phase.RUNNING) return Interaction.NONE;
        double x = player.x;
        if (phase == Phase.RUNNING) {
            var console = machinery.console();
            if (console != null && console.active()) return Interaction.CONSOLE;
        }
        if (x > layout().exitX() - 150)
            return phase == Phase.ROOM_CLEARED ? Interaction.EXIT : Interaction.LOCKED;
        if (phase != Phase.ROOM_CLEARED || Math.abs(x - layout().rewardX()) > 150)
            return Interaction.NONE;
        return switch (room.kind()) {
            case WORKSHOP -> Interaction.WORKSHOP;
            case MERCHANT -> Interaction.MERCHANT;
            case SHRINE -> rewards.dealTaken ? Interaction.NONE : Interaction.SHRINE;
            default -> rewards.available ? Interaction.REWARD : Interaction.NONE;
        };
    }

    /**
     * @return wählbare nächste Räume, leer auf der Brücke
     */
    public List<RoomPlan> nextRooms() {
        return room.depth() < RoomGenerator.ROOM_COUNT - 1
                ? generator.choices(room.depth() + 1)
                : List.of();
    }

    /**
     * Betritt den gewählten nächsten Raum. Nur nach dem Sichern des aktuellen Raums möglich.
     *
     * @param branch Index in {@link #nextRooms()}
     * @return {@code true}, wenn der Raum gewechselt wurde
     */
    public boolean chooseNextRoom(int branch) {
        if (phase != Phase.ROOM_CLEARED) return false;
        var choices = nextRooms();
        if (branch < 0 || branch >= choices.size()) return false;
        loot.collectAll();
        enterRoom(choices.get(branch));
        return true;
    }

    /**
     * Beginnt nach einem Sieg einen schwereren Zyklus mit dem aktuellen Build.
     *
     * @return {@code true}, wenn ein neuer Zyklus begann
     */
    public boolean nextCycle() {
        if (phase != Phase.VICTORY) return false;
        if (cycle < 99) cycle++;
        generator = new RoomGenerator(setup.seed(), cycle, setup.pressure());
        player.heal(player.maxHealth * .5);
        player.energy = player.maxEnergy();
        route.clear();
        enterRoom(generator.room(0, 0));
        return true;
    }

    // ---------------------------------------------------------------------------------------------
    // Hilfen für Kampf und Gegner (paketintern)
    // ---------------------------------------------------------------------------------------------

    Projectile shoot(
            Projectile.Kind kind,
            boolean friendly,
            double x,
            double y,
            double vx,
            double vy,
            double damage,
            double radius,
            double life) {
        var projectile =
                new Projectile(nextId++, kind, friendly, x, y, vx, vy, damage, radius, life);
        projectiles.add(projectile);
        return projectile;
    }

    void shockwaves(double x, double damage, double speed) {
        for (int side : new int[] {-1, 1})
            shoot(
                    Projectile.Kind.SHOCKWAVE,
                    false,
                    x + side * 70,
                    FLOOR - 18,
                    side * speed,
                    0,
                    damage,
                    22,
                    4);
    }

    void addHazard(Hazard hazard) {
        hazards.add(hazard);
    }

    void emit(GameEvent event) {
        events.add(event);
    }

    void countKill() {
        kills++;
    }

    void dropScrap(double x, double y, int value) {
        loot.dropScrap(x, y, value);
    }

    void drop(Pickup.Kind kind, double value, double x, double y) {
        loot.drop(kind, value, x, y);
    }

    void breakCrate(SupplyCrate crate) {
        loot.breakCrate(crate);
    }

    long nextId() {
        return nextId++;
    }

    double enemyHealth() {
        return (1 + room.sector() * .22 + (room.depth() % RoomGenerator.SECTOR_ROOMS) * .04)
                * Math.min(4, 1 + cycle * .3)
                * (1 + setup.pressure() * .12);
    }

    double enemyDamage() {
        return (1 + room.sector() * .15)
                * Math.min(3, 1 + cycle * .15)
                * (1 + setup.pressure() * .1)
                * (player.stacks(Item.GREED) > 0 ? 1.15 : 1)
                * (player.explorer ? .75 : 1);
    }

    static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // ---------------------------------------------------------------------------------------------
    // Lesender Zugriff für Anwendung und Darstellung
    // ---------------------------------------------------------------------------------------------

    /**
     * @return spielende Figur
     */
    public Player player() {
        return player;
    }

    /**
     * @return lebende und gerade besiegte Gegner (nur lesend)
     */
    public List<Enemy> enemies() {
        return Collections.unmodifiableList(enemies);
    }

    /**
     * @return aktive Geschosse (nur lesend)
     */
    public List<Projectile> projectiles() {
        return Collections.unmodifiableList(projectiles);
    }

    /**
     * @return Vorratskisten (nur lesend)
     */
    public List<SupplyCrate> crates() {
        return Collections.unmodifiableList(crates);
    }

    /**
     * @return Raumgefahren (nur lesend)
     */
    public List<Hazard> hazards() {
        return Collections.unmodifiableList(hazards);
    }

    /**
     * @return liegende Beute (nur lesend)
     */
    public List<Pickup> pickups() {
        return Collections.unmodifiableList(pickups);
    }

    /**
     * @return aktueller Raumplan
     */
    public RoomPlan room() {
        return room;
    }

    /**
     * @return Geometrie des aktuellen Raums
     */
    public RoomLayout layout() {
        return room.layout();
    }

    /**
     * @return Ablaufphase
     */
    public Phase phase() {
        return phase;
    }

    /**
     * @return Startbedingungen
     */
    public RunSetup setup() {
        return setup;
    }

    /**
     * @return Routen-Seed
     */
    public long seed() {
        return setup.seed();
    }

    /**
     * @return Zyklus ab 0
     */
    public int cycle() {
        return cycle;
    }

    /**
     * @return Druckstufe
     */
    public int pressure() {
        return setup.pressure();
    }

    /**
     * Bedient den Notschalter in Reichweite der Figur.
     *
     * @return {@code true}, wenn ein Effekt ausgelöst wurde
     */
    public boolean useConsole() {
        return machinery.use();
    }

    /**
     * @return eingebaute Raumtechnik des aktuellen Raums
     */
    public List<Fixture> fixtures() {
        return Collections.unmodifiableList(fixtures);
    }

    /**
     * @return Raumzustände, unter denen seit Beginn oder Fortsetzen ein Raum gesichert wurde
     */
    public Set<RoomCondition> clearedConditions() {
        return Collections.unmodifiableSet(clearedConditions);
    }

    /**
     * @return Abschüsse im Run
     */
    public int kills() {
        return kills;
    }

    /**
     * @return aktuelle Welle ab 0
     */
    public int wave() {
        return wave;
    }

    /**
     * @return verbleibende Zeit bis zur nächsten Welle oder -1
     */
    public double waveDelay() {
        return waveDelay;
    }

    /**
     * @return Tauchzeit in Sekunden
     */
    public double elapsed() {
        return elapsed;
    }

    /**
     * @return Zeit im aktuellen Raum
     */
    public double roomTime() {
        return roomTime;
    }

    /**
     * @return {@code true}, solange die Bergung dieses Raums offen ist
     */
    public boolean rewardAvailable() {
        return rewards.available;
    }

    /**
     * @return {@code true}, wenn die Werkstattreparatur verbraucht ist
     */
    public boolean repaired() {
        return rewards.repaired;
    }

    /**
     * @return {@code true}, wenn in dieser Kapelle bereits gehandelt wurde
     */
    public boolean dealTaken() {
        return rewards.dealTaken;
    }

    /**
     * @return aktuelle Angebote
     */
    public List<Offer> offers() {
        return rewards.offers;
    }

    /**
     * @return Handel der Druckkapelle
     */
    public List<ShrineDeal> deals() {
        return rewards.deals;
    }

    /**
     * @return Raum-Sicherung des aktuellen Raumeingangs
     */
    public RunCheckpoint checkpoint() {
        return checkpoint;
    }

    /**
     * @return gewählte Abzweige bis zum aktuellen Raum
     */
    public List<Integer> route() {
        return List.copyOf(route);
    }

    /**
     * @return lebender Boss des Raums oder {@code null}
     */
    public Enemy boss() {
        for (var enemy : enemies) if (enemy.kind.boss() && enemy.alive()) return enemy;
        return null;
    }

    /**
     * @return Menge der Module, die in Bergungen auftauchen können
     */
    public Set<Item> itemPool() {
        return setup.itemPool().isEmpty()
                ? EnumSet.noneOf(Item.class)
                : EnumSet.copyOf(setup.itemPool());
    }

    /**
     * Entnimmt alle seit dem letzten Aufruf entstandenen Ereignisse.
     *
     * @return Ereignisse in Entstehungsreihenfolge
     */
    public List<GameEvent> drainEvents() {
        var copy = List.copyOf(events);
        events.clear();
        return copy;
    }
}
