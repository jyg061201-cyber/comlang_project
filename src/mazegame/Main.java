package mazegame;

import mazegame.core.GameEngine;

/**
 * Main entry point for the Solo Adventure Maze game.
 * Creates a GameEngine and starts the game.
 */
public class Main {
    public static void main(String[] args) {
        GameEngine engine = new GameEngine();
        engine.start();
    }
}
