package mazegame.item;

/** Minor Flask: symbol m, restores 6 HP. */
public class MinorFlask extends Potion {
    public MinorFlask() { super(6); }

    @Override public String getSymbol() { return "m"; }
}
