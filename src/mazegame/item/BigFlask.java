package mazegame.item;

/** 대형 물약: 기호 B, HP 12 회복. */
public class BigFlask extends Potion {
    public BigFlask() { super(12); }

    @Override public String getSymbol() { return "B"; }
}
