package mazegame.item;

/** 약한 검: 기호 W, 데미지 2. */
public class WeakSword extends Weapon {
    public WeakSword() { super(2); }

    @Override public String getSymbol() { return "W"; }
    @Override public String getName()   { return "Weak Sword"; }
}
