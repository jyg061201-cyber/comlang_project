package mazegame.item;

/** 강한 검: 기호 X, 데미지 3. */
public class StrongSword extends Weapon {
    public StrongSword() { super(3); }

    @Override public String getSymbol() { return "X"; }
    @Override public String getName()   { return "Strong Sword"; }
}
