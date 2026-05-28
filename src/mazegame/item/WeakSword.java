package mazegame.item;

/** Weak Sword: symbol W, damage 2. */
public class WeakSword extends Weapon {
    public WeakSword() { super(2); }

    @Override public String getSymbol() { return "W"; }
    @Override public String getName()   { return "Weak Sword"; }
}
