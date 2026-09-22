package ch.zhaw.abyss.domain;

import java.util.EnumMap;
import java.util.Map;

public final class Player extends Actor {
    double energy = 100;
    double attackCooldown, attackTime, dashCooldown, dashTime, invulnerableTime;
    double abilityCooldown, shieldTime;
    int salvage;
    final ActiveModule module;
    final EnumMap<Upgrade, Integer> upgrades = new EnumMap<>(Upgrade.class);
    final boolean explorer;

    Player(ActiveModule module, boolean explorer) {
        super(1, 170, GameRun.FLOOR, 48, 112, explorer ? 150 : 100);
        this.module = module;
        this.explorer = explorer;
    }

    public double energy() {
        return energy;
    }

    public double maxEnergy() {
        return 100 + stacks(Upgrade.CAPACITOR) * 20;
    }

    public double attackTime() {
        return attackTime;
    }

    public double dashTime() {
        return dashTime;
    }

    public double dashCooldown() {
        return dashCooldown;
    }

    public double abilityCooldown() {
        return abilityCooldown;
    }

    public double shieldTime() {
        return shieldTime;
    }

    public double invulnerableTime() {
        return invulnerableTime;
    }

    public int salvage() {
        return salvage;
    }

    public ActiveModule module() {
        return module;
    }

    public boolean explorer() {
        return explorer;
    }

    public int stacks(Upgrade upgrade) {
        return upgrades.getOrDefault(upgrade, 0);
    }

    public Map<Upgrade, Integer> upgrades() {
        return Map.copyOf(upgrades);
    }

    public double attackDamage() {
        return 20 * (1 + .18 * stacks(Upgrade.SERVO)) * (explorer ? 1.2 : 1);
    }

    public double abilityDamage() {
        return 34 * (1 + .15 * stacks(Upgrade.CAPACITOR));
    }

    public double cooldownMultiplier() {
        return 1 - .12 * stacks(Upgrade.COOLANT);
    }

    public boolean grounded() {
        return y >= GameRun.FLOOR - .1;
    }

    void heal(double amount) {
        health = Math.min(maxHealth, health + Math.max(0, amount));
    }

    void upgrade(Upgrade upgrade) {
        int current = stacks(upgrade);
        if (current >= Upgrade.MAX_STACKS)
            throw new IllegalStateException("Modul bereits auf Höchststufe");
        upgrades.put(upgrade, current + 1);
        if (upgrade == Upgrade.MEDICAL) {
            maxHealth += 20;
            heal(30);
        }
        if (upgrade == Upgrade.CAPACITOR) energy = Math.min(maxEnergy(), energy + 20);
    }
}
