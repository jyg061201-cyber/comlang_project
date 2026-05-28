package mazegame.item;

import mazegame.entity.GameObject;

/**
 * Abstract base class for all weapons the hero can equip.
 * Each weapon has a damage value and a display name.
 * Concrete subclasses: Stick, WeakSword, StrongSword.
 */
public abstract class Weapon implements GameObject {

    protected int damage;

    public Weapon(int damage) {
        this.damage = damage;
    }

    public int getDamage() { return damage; }

    /** Human-readable name shown in the stats line. */
    public abstract String getName();
}
