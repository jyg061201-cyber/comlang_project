package mazegame.item;

/** Stick: symbol S, damage 1. */
public class Stick extends Weapon {
    public Stick() { super(1); }

    @Override public String getSymbol() { return "S"; }
    @Override public String getName()   { return "Stick"; }
}
