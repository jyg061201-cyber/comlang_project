package mazegame.entity;

import mazegame.item.Weapon;

/**
 * Represents the player-controlled hero (@).
 * Tracks HP, current weapon, and key possession.
 */
public class Hero implements GameObject {

    private int hp;
    private final int maxHp;
    private Weapon currentWeapon; // null if unarmed
    private boolean hasKey;

    public Hero() {
        this.hp = 25;
        this.maxHp = 25;
        this.currentWeapon = null;
        this.hasKey = false;
    }

    @Override
    public String getSymbol() {
        return "@";
    }

    // ── HP ────────────────────────────────────────────────────────────────────

    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }

    /**
     * Reduces HP by the given amount, clamped to 0.
     * @param amount damage received
     */
    public void takeDamage(int amount) {
        hp = Math.max(0, hp - amount);
    }

    /**
     * Restores HP by the given amount, clamped to maxHp.
     * @param amount HP to restore
     * @return true if the hero was actually healed (not already full)
     */
    public boolean heal(int amount) {
        if (hp >= maxHp) return false;
        hp = Math.min(maxHp, hp + amount);
        return true;
    }

    public boolean isAlive() { return hp > 0; }
    public boolean isFullHp() { return hp >= maxHp; }

    // ── Weapon ────────────────────────────────────────────────────────────────

    public Weapon getCurrentWeapon() { return currentWeapon; }
    public boolean isArmed() { return currentWeapon != null; }

    /**
     * Equips the given weapon. The caller is responsible for handling
     * the old weapon (dropping it back onto the map).
     */
    public void equipWeapon(Weapon weapon) {
        this.currentWeapon = weapon;
    }

    /** Returns current weapon damage, or 0 if unarmed. */
    public int getWeaponDamage() {
        return (currentWeapon != null) ? currentWeapon.getDamage() : 0;
    }

    // ── Key ───────────────────────────────────────────────────────────────────

    public boolean hasKey() { return hasKey; }
    public void pickUpKey() { this.hasKey = true; }

    // ── Display ───────────────────────────────────────────────────────────────

    /** Returns the stats line shown above the map every turn. */
    public String getStatsLine() {
        String weaponName = (currentWeapon != null)
                ? currentWeapon.getName() + " (" + currentWeapon.getDamage() + ")"
                : "None";
        String keyStatus = hasKey ? "Yes" : "No";
        return "HP: " + hp + "/" + maxHp + " | Weapon: " + weaponName + " | Key: " + keyStatus;
    }
}
