package mazegame.item;

/** Big Flask: symbol B, restores 12 HP. */
public class BigFlask extends Potion {
    public BigFlask() { super(12); }

    @Override public String getSymbol() { return "B"; }
}
