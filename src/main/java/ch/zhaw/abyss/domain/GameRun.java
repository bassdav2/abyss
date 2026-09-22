package ch.zhaw.abyss.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Aggregat eines Tauchgangs. Alle Regeln laufen ohne JavaFX, Audio oder Dateizugriff. */
public final class GameRun {
    public static final double WIDTH = 1600, HEIGHT = 900, FLOOR = 620;

    public enum Phase {
        RUNNING,
        ROOM_CLEARED,
        DEFEAT,
        VICTORY
    }

    final Player player;
    final List<Enemy> enemies = new ArrayList<>();
    final List<Projectile> projectiles = new ArrayList<>();
    final List<Hazard> hazards = new ArrayList<>();
    final List<SupplyCrate> crates = new ArrayList<>();
    final List<GameEvent> events = new ArrayList<>();
    private final List<Integer> route = new ArrayList<>();
    private final long seed;
    private RoomGenerator generator;
    private RoomPlan room;
    private Phase phase;
    private int cycle, kills, combo;
    private long nextId = 10;
    private double elapsed, roomTime;
    private boolean rewardAvailable, repaired;
    private List<Upgrade> rewardOffers = List.of();
    private RunCheckpoint checkpoint;
    private int wave;
    private double waveDelay = -1;

    public GameRun(long seed, ActiveModule module, boolean explorer) {
        this.seed = seed;
        player = new Player(module, explorer);
        generator = new RoomGenerator(seed, 0);
        enterRoom(generator.room(0, 0));
    }

    public static GameRun restore(RunCheckpoint saved) {
        if (saved == null
                || saved.module() == null
                || saved.cycle() < 0
                || saved.depth() < 0
                || saved.depth() >= RoomGenerator.ROOM_COUNT
                || saved.branch() < 0
                || saved.branch() > 1
                || saved.kills() < 0
                || !Double.isFinite(saved.health())
                || !Double.isFinite(saved.energy())
                || !Double.isFinite(saved.elapsed())
                || saved.elapsed() < 0
                || saved.salvage() < 0
                || saved.salvage() > 9999
                || saved.repairKits() < 0
                || saved.repairKits() > 3)
            throw new IllegalArgumentException("Ungültiger Spielstand");
        var run = new GameRun(saved.seed(), saved.module(), saved.explorer());
        for (var entry : saved.upgrades().entrySet()) {
            if (entry.getValue() < 0 || entry.getValue() > Upgrade.MAX_STACKS)
                throw new IllegalArgumentException("Ungültige Modulstufe");
            for (int i = 0; i < entry.getValue(); i++) run.player.upgrade(entry.getKey());
        }
        if (saved.health() <= 0
                || saved.health() > run.player.maxHealth()
                || saved.energy() < 0
                || saved.energy() > run.player.maxEnergy())
            throw new IllegalArgumentException("Ungültige Ressourcen");
        run.cycle = saved.cycle();
        run.generator = new RoomGenerator(saved.seed(), saved.cycle());
        if (saved.branch() >= run.generator.choices(saved.depth()).size())
            throw new IllegalArgumentException("Nicht erreichbarer Raum");
        run.player.health = saved.health();
        run.player.energy = saved.energy();
        run.player.salvage = saved.salvage();
        run.player.repairKits = saved.repairKits();
        run.kills = saved.kills();
        run.elapsed = saved.elapsed();
        run.route.clear();
        if (saved.route().size() != saved.depth() + 1
                || saved.route().stream().anyMatch(b -> b < 0 || b > 1)
                || saved.route().getLast() != saved.branch())
            throw new IllegalArgumentException("Ungültige Route");
        for (int depth = 0; depth < saved.route().size(); depth++)
            run.generator.room(depth, saved.route().get(depth));
        run.route.addAll(saved.route().subList(0, saved.depth()));
        run.enterRoom(run.generator.room(saved.depth(), saved.branch()));
        run.events.clear();
        return run;
    }

    public void update(double seconds, InputFrame input) {
        if (seconds <= 0 || !Double.isFinite(seconds)) return;
        if (seconds > 1.0 / 30 + 1e-8)
            throw new IllegalArgumentException("Simulationsschritt zu gross");
        if (phase == Phase.DEFEAT || phase == Phase.VICTORY) return;
        elapsed += seconds;
        roomTime += seconds;
        updatePlayer(seconds, input);
        if (phase == Phase.RUNNING) {
            for (Hazard hazard : hazards) {
                hazard.update(seconds);
                if (hazard.active() && hazard.bounds().intersects(player.bounds()))
                    damagePlayer(6 * enemyDamage(), hazard.x());
            }
            for (Enemy enemy : enemies) if (enemy.alive()) EnemyAi.update(enemy, this, seconds);
            updateProjectiles(seconds);
            enemies.removeIf(e -> !e.alive());
            if (player.alive() && enemies.isEmpty()) {
                if (wave + 1 < room.waveCount()) {
                    if (waveDelay < 0) {
                        waveDelay = 2.2;
                        events.add(
                                new GameEvent(
                                        GameEvent.Type.REINFORCEMENTS,
                                        800,
                                        FLOOR,
                                        0,
                                        "Weitere Patrouille nähert sich"));
                    } else {
                        waveDelay -= seconds;
                        if (waveDelay <= 0) {
                            wave++;
                            spawnWave();
                            waveDelay = -1;
                        }
                    }
                } else clearRoom();
            }
        }
        if (!player.alive() && phase != Phase.DEFEAT) {
            phase = Phase.DEFEAT;
            events.add(GameEvent.at(GameEvent.Type.DEFEAT, player.x, player.y));
        }
    }

    private void updatePlayer(double dt, InputFrame input) {
        if (input.heal()) useRepairKit();
        player.attackCooldown = Math.max(0, player.attackCooldown - dt);
        player.attackTime = Math.max(0, player.attackTime - dt);
        player.dashCooldown = Math.max(0, player.dashCooldown - dt);
        player.dashTime = Math.max(0, player.dashTime - dt);
        player.invulnerableTime = Math.max(0, player.invulnerableTime - dt);
        player.abilityCooldown = Math.max(0, player.abilityCooldown - dt);
        player.shieldTime = Math.max(0, player.shieldTime - dt);
        player.hurtTime = Math.max(0, player.hurtTime - dt);
        player.energy =
                Math.min(
                        player.maxEnergy(),
                        player.energy + dt * (5.5 + player.stacks(Upgrade.SIPHON)));
        int direction = (input.right() ? 1 : 0) - (input.left() ? 1 : 0);
        if (direction != 0 && player.dashTime <= 0) player.facing = direction;
        if (input.aimDirection() != 0
                && player.dashTime <= 0
                && (input.attack() || input.ability())) player.facing = input.aimDirection();
        if (input.jump() && player.grounded()) {
            player.vy = -650;
            events.add(GameEvent.at(GameEvent.Type.JUMP, player.x, player.y));
        }
        if (input.dash() && player.dashCooldown <= 0) {
            player.dashTime = .20;
            player.dashCooldown = .95 * player.cooldownMultiplier();
            player.invulnerableTime = Math.max(.24, player.invulnerableTime);
            events.add(GameEvent.at(GameEvent.Type.DASH, player.x, player.y));
        }
        player.vx =
                player.dashTime > 0
                        ? player.facing * 790 * player.movementMultiplier()
                        : direction
                                * (player.attackTime > .1 ? 215 : 300)
                                * player.movementMultiplier();
        player.x = clamp(player.x + player.vx * dt, 72, WIDTH - 72);
        player.vy += 1850 * dt;
        player.y = Math.min(FLOOR, player.y + player.vy * dt);
        if (player.grounded()) player.vy = 0;
        player.animationTime += dt * (Math.abs(player.vx) > 1 ? 1 : .6);
        if (input.attack() && player.attackCooldown <= 0 && player.dashTime <= 0) attack();
        if (input.ability()
                && phase == Phase.RUNNING
                && player.abilityCooldown <= 0
                && player.energy >= player.module.cost()) useAbility();
    }

    private void attack() {
        combo = (combo + 1) % 3;
        player.attackCooldown = (combo == 0 ? .48 : .36) * player.attackSpeedMultiplier();
        player.attackTime = .29;
        var reach =
                new Bounds(
                        player.facing > 0 ? player.x + 8 : player.x - player.attackReach() - 8,
                        player.y - 136,
                        player.attackReach(),
                        140);
        events.add(
                GameEvent.at(GameEvent.Type.SWING, player.x + player.facing * 50, player.y - 60));
        Enemy firstHit = null;
        for (SupplyCrate crate : crates)
            if (crate.intact()
                    && reach.intersects(crate.bounds())
                    && crate.hit(player.attackDamage())) collectCrate(crate);
        for (Enemy enemy : enemies) {
            if (enemy.alive() && reach.intersects(enemy.bounds())) {
                if (firstHit == null) firstHit = enemy;
                damageEnemy(enemy, player.attackDamage() * (combo == 0 ? 1.25 : 1));
                enemy.x =
                        clamp(
                                enemy.x + player.facing * (enemy.kind.boss() ? 8 : 24),
                                70,
                                WIDTH - 70);
                if (!enemy.kind.boss()
                        && (enemy.state == Enemy.State.APPROACH
                                || enemy.state == Enemy.State.RECOVER)) {
                    enemy.state = Enemy.State.STUNNED;
                    enemy.stateTime = .18;
                }
            }
        }
        if (combo == 0 && firstHit != null && player.stacks(Upgrade.ARC_COIL) > 0) {
            Enemy origin = firstHit;
            enemies.stream()
                    .filter(e -> e != origin && e.alive() && Math.abs(e.x - origin.x) < 280)
                    .min(java.util.Comparator.comparingDouble(e -> Math.abs(e.x - origin.x)))
                    .ifPresent(
                            e -> {
                                damageEnemy(e, 15 * player.stacks(Upgrade.ARC_COIL));
                                events.add(new GameEvent(GameEvent.Type.ARC, e.x, e.y - 65, 0, ""));
                            });
        }
    }

    private void collectCrate(SupplyCrate crate) {
        String text;
        switch (crate.kind()) {
            case REPAIR -> {
                player.heal(18);
                text = "Reparaturgel · +18 Integrität";
            }
            case ENERGY -> {
                player.energy = Math.min(player.maxEnergy(), player.energy + 30);
                text = "Energiezelle · +30 Energie";
            }
            case SALVAGE -> {
                player.salvage = Math.min(9999, player.salvage + 8);
                text = "Ersatzteile · +8 Schrott";
            }
            default -> throw new IllegalStateException();
        }
        events.add(new GameEvent(GameEvent.Type.SUPPLY, crate.x(), FLOOR - 40, 0, text));
    }

    private void useAbility() {
        player.energy -= player.module.cost();
        player.abilityCooldown = player.module.cooldown() * player.cooldownMultiplier();
        switch (player.module) {
            case PULSE -> {
                events.add(new GameEvent(GameEvent.Type.PULSE, player.x, player.y - 60, 280, ""));
                for (Enemy enemy : enemies)
                    if (enemy.alive() && Math.abs(enemy.x - player.x) < 290) {
                        damageEnemy(enemy, player.abilityDamage());
                        if (!enemy.kind.boss()) {
                            enemy.state = Enemy.State.STUNNED;
                            enemy.stateTime = 1.1;
                        }
                        enemy.x =
                                clamp(
                                        enemy.x + Math.signum(enemy.x - player.x) * 45,
                                        70,
                                        WIDTH - 70);
                    }
            }
            case ARC -> {
                addProjectile(
                        Projectile.Kind.ARC,
                        true,
                        player.x + player.facing * 45,
                        player.y - 70,
                        player.facing * 960,
                        0,
                        player.abilityDamage() * 1.15,
                        20,
                        2);
                events.add(GameEvent.at(GameEvent.Type.ARC, player.x, player.y - 70));
            }
            case AEGIS -> {
                player.shieldTime = 4;
                events.add(GameEvent.at(GameEvent.Type.SHIELD, player.x, player.y - 60));
            }
        }
    }

    void damageEnemy(Enemy enemy, double amount) {
        if (!enemy.alive()) return;
        if (enemy.armored())
            amount *=
                    enemy.kind == EnemyKind.WARDEN
                            ? .5
                            : enemy.kind == EnemyKind.REACTOR ? .38 : .22;
        enemy.health = Math.max(0, enemy.health - amount);
        enemy.hurtTime = .12;
        events.add(
                new GameEvent(
                        GameEvent.Type.HIT, enemy.x, enemy.y - enemy.height * .55, amount, ""));
        if (!enemy.alive()) {
            kills++;
            player.salvage =
                    Math.min(9999, player.salvage + (enemy.kind == EnemyKind.SENTINEL ? 5 : 3));
            player.energy =
                    Math.min(
                            player.maxEnergy(),
                            player.energy + 8 + 4 * player.stacks(Upgrade.SIPHON));
            player.heal(player.stacks(Upgrade.RECOVERY) * 3);
            events.add(
                    GameEvent.at(GameEvent.Type.ENEMY_DOWN, enemy.x, enemy.y - enemy.height * .5));
        }
    }

    void damagePlayer(double amount, double sourceX) {
        if (!player.alive() || player.invulnerableTime > 0) return;
        double actual =
                amount
                        * (1 - .12 * player.stacks(Upgrade.PLATING))
                        * (player.shieldTime > 0 ? .25 : 1);
        player.health = Math.max(0, player.health - actual);
        player.hurtTime = .2;
        player.invulnerableTime = .72;
        player.x = clamp(player.x + Math.signum(player.x - sourceX) * 20, 72, WIDTH - 72);
        events.add(new GameEvent(GameEvent.Type.PLAYER_HIT, player.x, player.y - 65, actual, ""));
    }

    void addProjectile(
            Projectile.Kind kind,
            boolean friendly,
            double x,
            double y,
            double vx,
            double vy,
            double damage,
            double radius,
            double life) {
        projectiles.add(
                new Projectile(nextId++, kind, friendly, x, y, vx, vy, damage, radius, life));
    }

    private void updateProjectiles(double dt) {
        for (Projectile projectile : projectiles) {
            projectile.x += projectile.vx * dt;
            projectile.y += projectile.vy * dt;
            projectile.life -= dt;
            if (projectile.friendly) {
                for (Enemy enemy : enemies) {
                    if (enemy.alive()
                            && !projectile.hitActors.contains(enemy.id)
                            && projectile.bounds().intersects(enemy.bounds())) {
                        projectile.hitActors.add(enemy.id);
                        damageEnemy(enemy, projectile.damage);
                    }
                }
            } else if (projectile.bounds().intersects(player.bounds())) {
                damagePlayer(projectile.damage, projectile.x);
                projectile.life = 0;
            }
        }
        projectiles.removeIf(
                p -> p.life <= 0 || p.x < -80 || p.x > WIDTH + 80 || p.y < 100 || p.y > FLOOR + 50);
    }

    private void clearRoom() {
        projectiles.clear();
        player.heal(7 * player.stacks(Upgrade.REGEN));
        if (room.kind() == RoomPlan.Kind.BRIDGE) {
            phase = Phase.VICTORY;
            player.attackTime = 0;
            player.dashTime = 0;
            player.shieldTime = 0;
            player.vx = 0;
            events.add(GameEvent.at(GameEvent.Type.VICTORY, player.x, player.y));
        } else {
            phase = Phase.ROOM_CLEARED;
            rewardAvailable = true;
            player.salvage = Math.min(9999, player.salvage + room.salvageReward());
            rewardOffers = room.kind() == RoomPlan.Kind.CACHE ? List.of() : makeOffers();
            events.add(
                    new GameEvent(GameEvent.Type.ROOM_CLEAR, player.x, player.y, 0, room.title()));
        }
    }

    private List<Upgrade> makeOffers() {
        var pool = new ArrayList<Upgrade>();
        for (Upgrade upgrade : Upgrade.values())
            if (player.stacks(upgrade) < Upgrade.MAX_STACKS) pool.add(upgrade);
        Collections.shuffle(
                pool,
                new Random(seed ^ room.depth() * 7349L ^ room.branch() * 193L ^ cycle * 911L));
        return List.copyOf(pool.subList(0, Math.min(3, pool.size())));
    }

    public boolean claimReward(Upgrade upgrade) {
        if (phase != Phase.ROOM_CLEARED || !rewardAvailable || !rewardOffers.contains(upgrade))
            return false;
        if (room.kind() == RoomPlan.Kind.WORKSHOP && player.salvage < 15) return false;
        if (room.kind() == RoomPlan.Kind.WORKSHOP) player.salvage -= 15;
        int ranks = Math.min(room.rewardRanks(), Upgrade.MAX_STACKS - player.stacks(upgrade));
        for (int i = 0; i < ranks; i++) player.upgrade(upgrade);
        rewardAvailable = false;
        events.add(new GameEvent(GameEvent.Type.UPGRADE, player.x, player.y, 0, upgrade.title()));
        return true;
    }

    public boolean claimSupplies() {
        if (phase != Phase.ROOM_CLEARED || !rewardAvailable || !rewardOffers.isEmpty())
            return false;
        player.heal(25);
        player.energy = Math.min(player.maxEnergy(), player.energy + 35);
        player.salvage = Math.min(9999, player.salvage + 10);
        rewardAvailable = false;
        events.add(GameEvent.at(GameEvent.Type.HEAL, player.x, player.y));
        return true;
    }

    public boolean useRepairKit() {
        if ((phase != Phase.RUNNING && phase != Phase.ROOM_CLEARED)
                || !player.alive()
                || player.repairKits <= 0
                || player.health >= player.maxHealth()) return false;
        player.repairKits--;
        player.heal(35);
        events.add(
                new GameEvent(
                        GameEvent.Type.SUPPLY,
                        player.x,
                        player.y - 65,
                        0,
                        "Reparaturset · +35 Integrität"));
        return true;
    }

    public boolean buyRepairKit() {
        if (room.kind() != RoomPlan.Kind.WORKSHOP
                || phase != Phase.ROOM_CLEARED
                || player.salvage < 20
                || player.repairKits >= 3) return false;
        player.salvage -= 20;
        player.repairKits++;
        events.add(
                new GameEvent(
                        GameEvent.Type.SUPPLY,
                        player.x,
                        player.y - 65,
                        0,
                        "Reparaturset verstaut · Q zum Benutzen"));
        return true;
    }

    public boolean repair() {
        if (room.kind() != RoomPlan.Kind.WORKSHOP || phase != Phase.ROOM_CLEARED || repaired)
            return false;
        player.heal(40);
        player.energy = player.maxEnergy();
        repaired = true;
        events.add(GameEvent.at(GameEvent.Type.HEAL, player.x, player.y));
        return true;
    }

    public List<RoomPlan> nextRooms() {
        return room.depth() < RoomGenerator.ROOM_COUNT - 1
                ? generator.choices(room.depth() + 1)
                : List.of();
    }

    public boolean chooseNextRoom(int branch) {
        if (phase != Phase.ROOM_CLEARED) return false;
        var choices = nextRooms();
        if (branch < 0 || branch >= choices.size()) return false;
        enterRoom(choices.get(branch));
        return true;
    }

    public boolean nextCycle() {
        if (phase != Phase.VICTORY) return false;
        if (cycle < Integer.MAX_VALUE) cycle++;
        generator = new RoomGenerator(seed, cycle);
        player.heal(50);
        player.energy = player.maxEnergy();
        route.clear();
        enterRoom(generator.room(0, 0));
        return true;
    }

    private void enterRoom(RoomPlan nextRoom) {
        room = nextRoom;
        nextId = 10;
        roomTime = 0;
        enemies.clear();
        projectiles.clear();
        hazards.clear();
        crates.clear();
        wave = 0;
        waveDelay = -1;
        rewardAvailable = false;
        repaired = false;
        rewardOffers = List.of();
        player.x = 170;
        player.y = FLOOR;
        player.vx = 0;
        player.vy = 0;
        player.facing = 1;
        player.attackTime = 0;
        player.dashTime = 0;
        player.invulnerableTime = 1;
        player.shieldTime = 0;
        player.attackCooldown = 0;
        player.dashCooldown = 0;
        player.abilityCooldown = 0;
        player.hurtTime = 0;
        player.animationTime = 0;
        combo = 0;
        route.add(nextRoom.branch());
        spawnWave();
        if (room.kind() != RoomPlan.Kind.BRIDGE
                && room.kind() != RoomPlan.Kind.BOSS
                && room.kind() != RoomPlan.Kind.WORKSHOP) {
            var lootRandom =
                    new Random(seed ^ room.depth() * 2917L ^ room.branch() * 333L ^ cycle * 717L);
            crates.add(
                    new SupplyCrate(
                            room.variant() % 2 == 0 ? 355 : 1245,
                            SupplyCrate.Kind.values()[lootRandom.nextInt(3)]));
            if (room.kind() == RoomPlan.Kind.CACHE)
                crates.add(new SupplyCrate(450, SupplyCrate.Kind.REPAIR));
        }
        if (room.sector() > 0
                && (room.kind() == RoomPlan.Kind.COMBAT || room.kind() == RoomPlan.Kind.ELITE))
            hazards.add(
                    new Hazard(
                            room.variant() % 2 == 0 ? 650 : 1020,
                            room.variant() % 2 == 0 ? Hazard.Kind.STEAM : Hazard.Kind.ELECTRIC));
        phase = Phase.RUNNING;
        // Snapshot vor Raum-Belohnungen: Beim Fortsetzen wird genau dieser Einstieg rekonstruiert.
        checkpoint =
                new RunCheckpoint(
                        seed,
                        cycle,
                        room.depth(),
                        room.branch(),
                        player.module,
                        player.explorer,
                        player.health,
                        player.energy,
                        player.salvage,
                        player.upgrades,
                        kills,
                        elapsed,
                        route,
                        player.repairKits);
        if (enemies.isEmpty()) clearRoom();
    }

    private void spawnWave() {
        double strength = (1 + room.sector() * .12 + wave * .05) * Math.min(3.5, 1 + cycle * .23);
        boolean fromLeft = wave > 0 && player.x > WIDTH / 2;
        for (int i = 0; i < room.enemies().size(); i++) {
            var kind = room.enemies().get(i);
            double x = kind.boss() ? 1220 : fromLeft ? 180 + i * 108 : 940 + i * 118;
            enemies.add(new Enemy(nextId++, kind, x, strength, 1.4 + i * .35));
        }
    }

    public Player player() {
        return player;
    }

    public List<Enemy> enemies() {
        return Collections.unmodifiableList(enemies);
    }

    public List<Projectile> projectiles() {
        return Collections.unmodifiableList(projectiles);
    }

    public List<SupplyCrate> crates() {
        return Collections.unmodifiableList(crates);
    }

    public List<Hazard> hazards() {
        return Collections.unmodifiableList(hazards);
    }

    public RoomPlan room() {
        return room;
    }

    public Phase phase() {
        return phase;
    }

    public long seed() {
        return seed;
    }

    public int cycle() {
        return cycle;
    }

    public int kills() {
        return kills;
    }

    public int wave() {
        return wave;
    }

    public double waveDelay() {
        return waveDelay;
    }

    public double elapsed() {
        return elapsed;
    }

    public double roomTime() {
        return roomTime;
    }

    public boolean rewardAvailable() {
        return rewardAvailable;
    }

    public boolean repaired() {
        return repaired;
    }

    public List<Upgrade> rewardOffers() {
        return rewardOffers;
    }

    public RunCheckpoint checkpoint() {
        return checkpoint;
    }

    public List<Integer> route() {
        return List.copyOf(route);
    }

    public List<GameEvent> drainEvents() {
        var copy = List.copyOf(events);
        events.clear();
        return copy;
    }

    double enemyDamage() {
        return (1 + room.sector() * .12) * Math.min(2.8, 1 + cycle * .12);
    }

    static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
