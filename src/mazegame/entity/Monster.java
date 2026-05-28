package mazegame.entity;

/**
 * Abstract base class for all monsters.
 * Defines common state (hp, maxHp, damage, symbol) and behaviour
 * (takeDamage, isDead). Concrete subclasses set their own stats.
 */
public abstract class Monster implements GameObject {

    protected int hp;
    protected int maxHp;
    protected int damage;
    protected String symbol;

    /**
     * @param hp     starting and maximum hit points
     * @param damage attack value dealt to the hero each exchange
     * @param symbol single-character map symbol
     */
    public Monster(int hp, int damage, String symbol) {
        this.hp = hp;
        this.maxHp = hp;
        this.damage = damage;
        this.symbol = symbol;
    }

    @Override
    public String getSymbol() { return symbol; }

    public int getHp()     { return hp; }
    public int getMaxHp()  { return maxHp; }
    public int getDamage() { return damage; }

    /**
     * Reduces HP by amount, clamped to 0.
     */
    public void takeDamage(int amount) {
        hp = Math.max(0, hp - amount);
    }

    /** Returns true once HP has reached zero. */
    public boolean isDead() { return hp <= 0; }

    /** Human-readable name used in the combat menu. */
    public abstract String getName();

    /**
     * Called when this monster is killed.
     * Override in subclasses that drop items (e.g. Troll drops the Key).
     * @return true if this monster drops the key on death
     */
    public boolean dropsKey() { return false; }
}
