package mazegame.item;

/** 소형 물약: 기호 m, HP 6 회복. */
public class MinorFlask extends Potion {
    public MinorFlask() { super(6); }

    @Override public String getSymbol() { return "m"; }
}
