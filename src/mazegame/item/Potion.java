package mazegame.item;

import mazegame.entity.GameObject;

/**
 * Abstract base class for healing potions.
 * Potions are consumed automatically when the hero walks onto them,
 * unless the hero is already at full HP.
 * Concrete subclasses: MinorFlask, BigFlask.
 */
public abstract class Potion implements GameObject {

    protected int healAmount;

    public Potion(int healAmount) {
        this.healAmount = healAmount;
    }

    public int getHealAmount() { return healAmount; }
}
