package mazegame.entity;

/**
 * Orc: HP 8, Damage 3, symbol O.
 */
public class Orc extends Monster {

    public Orc() {
        super(8, 3, "O");
    }

    /**
     * Constructor for restoring from a saved CSV state.
     */
    public Orc(int hp) {
        super(8, 3, "O");
        this.hp = hp;
    }

    @Override
    public String getName() { return "Orc"; }
}
