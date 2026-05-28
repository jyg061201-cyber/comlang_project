package mazegame.io;

/**
 * Custom exception for recoverable game I/O errors:
 * missing CSV file, malformed header line, bad door references, etc.
 * Using a dedicated exception type lets GameEngine and CSVHandler
 * catch game-specific problems separately from unexpected runtime errors.
 */
public class GameException extends Exception {

    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
