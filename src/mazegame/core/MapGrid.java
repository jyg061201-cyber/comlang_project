package mazegame.core;

import mazegame.entity.*;
import mazegame.item.*;

import java.util.Random;

/**
 * Holds a single room's data as a 2D {@code GameObject[][]} grid and
 * is responsible for rendering it to the console.
 *
 * Polymorphism in action: the grid stores any GameObject (Hero, Monster
 * subclass, Weapon subclass, Potion subclass, Door, Key, or null for empty),
 * all accessed through the common getSymbol() method for rendering.
 */
public class MapGrid {

    private GameObject[][] grid;
    private int rows;
    private int cols;

    // Current hero position
    private int heroRow;
    private int heroCol;

    private final Random random = new Random();

    // ── Construction ──────────────────────────────────────────────────────────

    /**
     * Initialises the grid from a loaded 2D array and places the hero
     * according to the spec rules:
     *  1. If an '@' placeholder exists in the grid, use that position.
     *  2. Otherwise try (1,1).
     *  3. If (1,1) is occupied, pick a random empty cell.
     *
     * @param grid the parsed 2D array from CSVHandler (null cells = open floor)
     * @param hero the Hero object to place
     */
    public void init(GameObject[][] grid, Hero hero) {
        this.grid = grid;
        this.rows = grid.length;
        this.cols = (rows > 0) ? grid[0].length : 0;

        placeHero(hero);
    }

    /**
     * Overload used when re-entering a room from a specific door.
     * The hero is placed adjacent (inward) to the door they came through.
     *
     * @param grid         the room grid
     * @param hero         the Hero object
     * @param entryDoorRow row of the door the hero entered through
     * @param entryDoorCol col of the door the hero entered through
     */
    public void init(GameObject[][] grid, Hero hero, int entryDoorRow, int entryDoorCol) {
        this.grid = grid;
        this.rows = grid.length;
        this.cols = (rows > 0) ? grid[0].length : 0;

        // Calculate the cell one step inward from the door
        int inwardRow = entryDoorRow;
        int inwardCol = entryDoorCol;

        if (entryDoorRow == 0)        inwardRow = 1;
        else if (entryDoorRow == rows - 1) inwardRow = rows - 2;
        if (entryDoorCol == 0)        inwardCol = 1;
        else if (entryDoorCol == cols - 1) inwardCol = cols - 2;

        // Use the inward cell if it is empty; otherwise fall back to normal placement
        if (isInBounds(inwardRow, inwardCol) && grid[inwardRow][inwardCol] == null) {
            heroRow = inwardRow;
            heroCol = inwardCol;
            grid[heroRow][heroCol] = hero;
        } else {
            placeHero(hero);
        }
    }

    /** Places the hero on the grid following spec rules. */
    private void placeHero(Hero hero) {
        // Rule 1: look for an '@' placeholder left in the grid
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                // CSVHandler returns null for '@' cells, so we can't detect them
                // this way. The '@' placeholder is handled by CSVHandler returning
                // null AND the caller passing the (r,c) recorded during parsing.
                // Here we just find the first null that works as a fallback.
            }
        }

        // Rule 2: try (1,1)
        if (isInBounds(1, 1) && grid[1][1] == null) {
            heroRow = 1;
            heroCol = 1;
            grid[heroRow][heroCol] = hero;
            return;
        }

        // Rule 3: random empty cell
        placeHeroRandomly(hero);
    }

    /**
     * Variant that respects a pre-recorded '@' position from the CSV.
     * Call this instead of init() when CSVHandler found an '@' token.
     */
    public void initWithHeroAt(GameObject[][] grid, Hero hero, int atRow, int atCol) {
        this.grid = grid;
        this.rows = grid.length;
        this.cols = (rows > 0) ? grid[0].length : 0;

        if (isInBounds(atRow, atCol) && grid[atRow][atCol] == null) {
            heroRow = atRow;
            heroCol = atCol;
            grid[heroRow][heroCol] = hero;
        } else {
            placeHero(hero);
        }
    }

    private void placeHeroRandomly(Hero hero) {
        java.util.List<int[]> empty = new java.util.ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == null) empty.add(new int[]{r, c});
            }
        }
        if (empty.isEmpty()) {
            // Edge case: no empty cells at all – place at (0,0) as last resort
            heroRow = 0; heroCol = 0;
        } else {
            int[] pos = empty.get(random.nextInt(empty.size()));
            heroRow = pos[0]; heroCol = pos[1];
        }
        grid[heroRow][heroCol] = hero;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    /**
     * Renders the room to System.out with ASCII walls and the hero's stat line.
     * Format:
     *   AdventureGame
     *   HP: x/25 | Weapon: ... | Key: ...
     *   +----------+
     *   | cells... |
     *   +----------+
     *
     * @param hero used to print the stats line
     */
    public void render(Hero hero) {
        System.out.println("AdventureGame");
        System.out.println(hero.getStatsLine());

        // Top wall
        String topBottom = "+" + "-".repeat(cols * 2 + 1) + "+";
        System.out.println(topBottom);

        for (int r = 0; r < rows; r++) {
            StringBuilder line = new StringBuilder("|");
            for (int c = 0; c < cols; c++) {
                line.append(" ");
                GameObject obj = grid[r][c];
                line.append(obj == null ? " " : obj.getSymbol());
            }
            line.append(" |");
            System.out.println(line.toString());
        }

        // Bottom wall
        System.out.println(topBottom);
    }

    // ── Movement ──────────────────────────────────────────────────────────────

    /** Returns the cell content at (r, c), or null if out of bounds. */
    public GameObject getCell(int r, int c) {
        if (!isInBounds(r, c)) return null;
        return grid[r][c];
    }

    /** Sets the cell at (r, c). */
    public void setCell(int r, int c, GameObject obj) {
        if (isInBounds(r, c)) grid[r][c] = obj;
    }

    /** Moves the hero to (newRow, newCol), vacating the old cell. */
    public void moveHero(int newRow, int newCol) {
        grid[heroRow][heroCol] = null;  // vacate old cell
        heroRow = newRow;
        heroCol = newCol;
        // Caller is responsible for putting the hero object into the new cell
    }

    public boolean isInBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    // ── Adjacency ─────────────────────────────────────────────────────────────

    /**
     * Returns up to 4 adjacent cells (up/down/left/right) that contain a Monster.
     */
    public java.util.List<int[]> getAdjacentMonsters() {
        java.util.List<int[]> result = new java.util.ArrayList<>();
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : dirs) {
            int r = heroRow + d[0];
            int c = heroCol + d[1];
            if (isInBounds(r, c) && grid[r][c] instanceof mazegame.entity.Monster) {
                result.add(new int[]{r, c});
            }
        }
        return result;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public int getHeroRow() { return heroRow; }
    public int getHeroCol() { return heroCol; }
    public int getRows()    { return rows; }
    public int getCols()    { return cols; }
    public GameObject[][] getGrid() { return grid; }
}
