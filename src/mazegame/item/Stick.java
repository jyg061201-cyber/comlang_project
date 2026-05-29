package mazegame.item;

/** 막대기: 기호 S, 데미지 1. */
public class Stick extends Weapon {
    public Stick() { super(1); }

    @Override public String getSymbol() { return "S"; }
    @Override public String getName()   { return "Stick"; }
}
