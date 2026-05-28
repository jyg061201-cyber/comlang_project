package mazegame.entity;

/**
 * Goblin: HP 3, Damage 1, symbol G.
 */
public class Goblin extends Monster {

    public Goblin() {
        super(3, 1, "G");
    }

    /**
     * Constructor that allows restoring a goblin with a specific HP value
     * (used when loading a saved room state from CSV).
     * @param hp current hit points read from the CSV cell
     */
    public Goblin(int hp) {
        super(3, 1, "G");
        this.hp = hp;
    }

    @Override
    public String getName() { return "Goblin"; }
}
