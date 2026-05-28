package mazegame.entity;

/**
 * Troll: HP 15, Damage 4, symbol T.
 * The Troll is the only monster that drops the Key on death.
 * There is exactly one Troll in the entire game.
 */
public class Troll extends Monster {

    public Troll() {
        super(15, 4, "T");
    }

    /**
     * Constructor for restoring from a saved CSV state.
     */
    public Troll(int hp) {
        super(15, 4, "T");
        this.hp = hp;
    }

    @Override
    public String getName() { return "Troll"; }

    /** The Troll always drops the key when defeated. */
    @Override
    public boolean dropsKey() { return true; }
}
