package mazegame.item;

import mazegame.entity.GameObject;

/**
 * The master key (*), dropped by the Troll when defeated.
 * There is exactly one Key in the game.
 * Once the hero steps onto the Key cell it is added to the hero's inventory
 * and removed from the map.
 */
public class Key implements GameObject {

    @Override
    public String getSymbol() { return "*"; }
}
