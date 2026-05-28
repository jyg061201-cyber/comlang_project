package mazegame.item;

/** Strong Sword: symbol X, damage 3. */
public class StrongSword extends Weapon {
    public StrongSword() { super(3); }

    @Override public String getSymbol() { return "X"; }
    @Override public String getName()   { return "Strong Sword"; }
}
