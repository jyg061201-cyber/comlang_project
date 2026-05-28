package mazegame.io;

import mazegame.entity.*;
import mazegame.item.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all CSV file operations for the game:
 *  1. Creating a per-run save folder and copying original CSV files into it.
 *  2. Parsing a CSV file into a 2D GameObject array.
 *  3. Saving the current map state back to the per-run CSV copy on room exit.
 *
 * All paths used here are RELATIVE – no absolute paths are ever constructed.
 */
public class CSVHandler {

    /** Folder that holds the original CSV room files (relative to working dir). */
    private static final String DATA_DIR = "data";

    /** The per-run save subfolder created at game start. */
    private String saveDir;

    // ── Initialisation ────────────────────────────────────────────────────────

    /**
     * Creates a uniquely-named save folder under data/ and copies every CSV
     * file found in data/ into it.  Must be called once at game start.
     *
     * @throws GameException if the data directory is missing or cannot be read
     */
    public void initSaveFolder() throws GameException {
        File dataFolder = new File(DATA_DIR);
        if (!dataFolder.exists() || !dataFolder.isDirectory()) {
            throw new GameException(
                "Data directory '" + DATA_DIR + "' not found. "
                + "Please place your CSV room files in the 'data' folder "
                + "next to the program.");
        }

        // Build a unique sub-folder name: run_yyyyMMdd_HHmmss
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        saveDir = DATA_DIR + File.separator + "run_" + timestamp;

        File saveFolderFile = new File(saveDir);
        if (!saveFolderFile.mkdirs()) {
            throw new GameException("Could not create save folder: " + saveDir);
        }

        // Copy every .csv in data/ into the new save folder
        File[] csvFiles = dataFolder.listFiles(
                (dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            throw new GameException(
                "No CSV files found in '" + DATA_DIR + "'.");
        }
        for (File csv : csvFiles) {
            File dest = new File(saveDir + File.separator + csv.getName());
            try {
                Files.copy(csv.toPath(), dest.toPath(),
                           StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new GameException(
                    "Failed to copy '" + csv.getName() + "' to save folder.", e);
            }
        }
    }

    /** Returns the path to the save folder (relative). */
    public String getSaveDir() { return saveDir; }

    // ── CSV Parsing ───────────────────────────────────────────────────────────

    /**
     * Reads a CSV file from the save folder and builds a 2D GameObject array.
     * The first line of the file is: rows,cols
     * Subsequent lines are comma-separated cell values.
     *
     * Recognised cell tokens:
     *   (empty)  → null (open floor)
     *   @        → Hero placeholder – caller places Hero object
     *   S        → Stick
     *   W        → WeakSword
     *   X        → StrongSword
     *   m        → MinorFlask
     *   B        → BigFlask
     *   *        → Key
     *   G or G:n → Goblin (optionally with current HP)
     *   O or O:n → Orc
     *   T or T:n → Troll
     *   d:file   → Regular Door
     *   D        → Master Door
     *
     * @param filename the CSV file name (e.g. "start.csv"), NOT a full path
     * @return a 2D array [rows][cols] of GameObjects (null = empty cell)
     * @throws GameException on missing file, bad header, or unrecognised token
     */
    public GameObject[][] loadRoom(String filename) throws GameException {
        String filePath = saveDir + File.separator + filename;
        File file = new File(filePath);

        if (!file.exists()) {
            throw new GameException(
                "Room file not found: '" + filename + "' (looked in " + saveDir + ")");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            // --- Header line ---
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new GameException(
                    "'" + filename + "' is empty or has no header line.");
            }
            String[] dims = headerLine.trim().split(",");
            if (dims.length < 2) {
                throw new GameException(
                    "'" + filename + "' header line is malformed: '" + headerLine + "'");
            }
            int rows, cols;
            try {
                rows = Integer.parseInt(dims[0].trim());
                cols = Integer.parseInt(dims[1].trim());
            } catch (NumberFormatException e) {
                throw new GameException(
                    "'" + filename + "' header line has non-integer dimensions: '"
                    + headerLine + "'");
            }

            // --- Data rows ---
            GameObject[][] grid = new GameObject[rows][cols];
            for (int r = 0; r < rows; r++) {
                String line = reader.readLine();
                if (line == null) {
                    // Treat missing rows as all-empty
                    break;
                }
                // Split preserving trailing empty tokens
                String[] tokens = splitCsvLine(line, cols);
                for (int c = 0; c < cols; c++) {
                    String token = (c < tokens.length) ? tokens[c].trim() : "";
                    grid[r][c] = parseToken(token, filename);
                }
            }
            return grid;

        } catch (IOException e) {
            throw new GameException(
                "Error reading '" + filename + "': " + e.getMessage(), e);
        }
    }

    /**
     * Splits a CSV line into exactly {@code expectedCols} tokens,
     * padding with empty strings if the line is short.
     */
    private String[] splitCsvLine(String line, int expectedCols) {
        // Use -1 limit to keep trailing empty fields
        String[] parts = line.split(",", -1);
        if (parts.length >= expectedCols) return parts;
        String[] padded = new String[expectedCols];
        for (int i = 0; i < expectedCols; i++) {
            padded[i] = (i < parts.length) ? parts[i] : "";
        }
        return padded;
    }

    /**
     * Converts a single CSV cell token to the appropriate GameObject.
     * Returns null for an empty cell (open floor).
     */
    private GameObject parseToken(String token, String filename) throws GameException {
        if (token.isEmpty()) return null;

        switch (token) {
            case "@": return null; // Hero placeholder; Hero is placed by GameEngine
            case "S": return new Stick();
            case "W": return new WeakSword();
            case "X": return new StrongSword();
            case "m": return new MinorFlask();
            case "B": return new BigFlask();
            case "*": return new Key();
            case "D": return new Door();        // Master door
            default:
                // Monster with optional HP: G, G:3, O:8, T:15 …
                if (token.startsWith("G")) return parseMonster(token, "G", filename);
                if (token.startsWith("O")) return parseMonster(token, "O", filename);
                if (token.startsWith("T")) return parseMonster(token, "T", filename);
                // Regular door: d:filename.csv
                if (token.startsWith("d:")) {
                    String target = token.substring(2).trim();
                    if (target.isEmpty()) {
                        throw new GameException(
                            "Door cell in '" + filename + "' has no target filename.");
                    }
                    return new Door(target);
                }
                // Unknown token – warn but don't crash
                System.out.println("[Warning] Unknown cell token '" + token
                        + "' in '" + filename + "' – treated as empty.");
                return null;
        }
    }

    /** Parses a monster token like "G", "G:3", "O:8", "T:2". */
    private GameObject parseMonster(String token, String type, String filename)
            throws GameException {
        int hp = -1; // -1 means "use default max HP"
        if (token.contains(":")) {
            String[] parts = token.split(":");
            if (parts.length >= 2 && !parts[1].trim().isEmpty()) {
                try {
                    hp = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    throw new GameException(
                        "Bad monster HP in '" + filename + "': '" + token + "'");
                }
            }
        }
        switch (type) {
            case "G": return (hp < 0) ? new Goblin()    : new Goblin(hp);
            case "O": return (hp < 0) ? new Orc()       : new Orc(hp);
            case "T": return (hp < 0) ? new Troll()     : new Troll(hp);
            default:  return null;
        }
    }

    // ── State Saving ──────────────────────────────────────────────────────────

    /**
     * Serialises the current state of the map grid back to the save-folder CSV.
     * Called whenever the hero leaves a room through a regular door.
     *
     * The hero's own cell is saved as empty (the hero is not written to the file;
     * each room always re-derives the hero's start position on load).
     *
     * @param filename the CSV file name to overwrite in the save folder
     * @param grid     the current 2D grid (hero cell may be non-null)
     * @throws GameException if the file cannot be written
     */
    public void saveRoom(String filename, GameObject[][] grid) throws GameException {
        String filePath = saveDir + File.separator + filename;
        int rows = grid.length;
        int cols = (rows > 0) ? grid[0].length : 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Header
            writer.println(rows + "," + cols);
            // Data rows
            for (int r = 0; r < rows; r++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < cols; c++) {
                    if (c > 0) sb.append(",");
                    sb.append(cellToToken(grid[r][c]));
                }
                writer.println(sb.toString());
            }
        } catch (IOException e) {
            throw new GameException(
                "Could not save room state to '" + filename + "': " + e.getMessage(), e);
        }
    }

    /**
     * Converts a single grid cell back to its CSV token.
     * Hero cells are saved as empty (open floor).
     * Monsters are saved with their current HP so partial damage persists.
     * Regular doors are saved as "d:filename.csv" to preserve the target.
     */
    private String cellToToken(GameObject obj) {
        if (obj == null) return "";

        // Hero cell → empty on save (hero position is re-derived on room load)
        if (obj instanceof mazegame.entity.Hero) return "";

        // Monsters: save current HP so partial damage persists across visits
        if (obj instanceof mazegame.entity.Monster) {
            mazegame.entity.Monster m = (mazegame.entity.Monster) obj;
            return m.getSymbol() + ":" + m.getHp();
        }

        // Regular doors MUST keep their "d:filename.csv" format —
        // getSymbol() returns just "d" which loses the target filename!
        if (obj instanceof mazegame.entity.Door) {
            mazegame.entity.Door door = (mazegame.entity.Door) obj;
            if (!door.isMaster()) {
                return "d:" + door.getTargetFilename();
            }
            return "D"; // master door
        }

        // Everything else (Weapon, Potion, Key) just uses its symbol
        return obj.getSymbol();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /**
     * Returns the list of CSV file names present in the save folder.
     * Useful for debugging, not required for gameplay.
     */
    public List<String> listSavedFiles() {
        List<String> names = new ArrayList<>();
        File dir = new File(saveDir);
        File[] files = dir.listFiles(
                (d, name) -> name.toLowerCase().endsWith(".csv"));
        if (files != null) {
            for (File f : files) names.add(f.getName());
        }
        return names;
    }
}
