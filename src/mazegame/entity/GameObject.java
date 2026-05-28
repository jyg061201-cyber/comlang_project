package mazegame.entity;

/**
 * Interface implemented by every object that can occupy a cell in the map grid.
 * This is the key interface enabling polymorphism: Hero, Monster subclasses,
 * Weapon subclasses, Potion subclasses, Door, and Key all implement this,
 * allowing MapGrid to store them all in a single GameObject[][] array.
 */
public interface GameObject {

    /**
     * Returns the single character symbol displayed in the map for this object.
     * @return the display symbol as a String
     */
    String getSymbol();
}
