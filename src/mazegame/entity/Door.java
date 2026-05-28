package mazegame.entity;

/**
 * Represents a door cell on the map border.
 *
 * Regular door (d): links to another room's CSV file via targetFilename.
 * Master door (D):  isMaster == true; requires the hero to have the Key.
 */
public class Door implements GameObject {

    private final String targetFilename; // e.g. "room2.csv", null for master door
    private final boolean isMaster;

    /**
     * Creates a regular door pointing to another room file.
     * @param targetFilename the CSV filename read from the cell at runtime
     */
    public Door(String targetFilename) {
        this.targetFilename = targetFilename;
        this.isMaster = false;
    }

    /**
     * Creates a master door (no target file needed).
     */
    public Door() {
        this.targetFilename = null;
        this.isMaster = true;
    }

    @Override
    public String getSymbol() {
        return isMaster ? "D" : "d";
    }

    public String getTargetFilename() { return targetFilename; }
    public boolean isMaster()         { return isMaster; }
}
