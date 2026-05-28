package mazegame.core;

import mazegame.entity.*;
import mazegame.item.*;
import mazegame.io.*;

import java.util.List;
import java.util.Scanner;

/**
 * The central game loop.
 *
 * Responsibilities:
 *  - Initialise CSVHandler and load the starting room.
 *  - Accept player input (u/d/r/l for movement, a for attack, q to quit).
 *  - Apply movement rules: item pickup, potion consumption, weapon switching,
 *    door traversal, and combat adjacency.
 *  - Manage room transitions (save current room, load new room).
 *  - Detect win (master door + key) and lose (hero HP = 0) conditions.
 */
public class GameEngine {

    private final CSVHandler csvHandler;
    private final Scanner    scanner;
    private Hero             hero;
    private Potion           pendingPotion; // potion under the hero when HP was full
    private Weapon           pendingWeapon; // weapon under the hero when player declined to switch
    private MapGrid          mapGrid;
    private String           currentFilename;

    public GameEngine() {
        csvHandler = new CSVHandler();
        scanner    = new Scanner(System.in);
        hero       = new Hero();
        mapGrid    = new MapGrid();
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public void start() {
        // 1. Create per-run save folder and copy CSV files
        try {
            csvHandler.initSaveFolder();
        } catch (GameException e) {
            System.out.println("[Error] " + e.getMessage());
            System.out.println("Please check that the 'data' folder exists and contains CSV files.");
            return;
        }

        // 2. Load the starting room
        try {
            loadRoom("start.csv", -1, -1);
        } catch (GameException e) {
            System.out.println("[Error] Could not load start.csv: " + e.getMessage());
            return;
        }

        // 3. Main game loop
        gameLoop();

        scanner.close();
    }

    // ── Room loading ──────────────────────────────────────────────────────────

    /**
     * Loads a room file and (re)initialises the MapGrid.
     *
     * @param filename      the CSV file to load
     * @param entryDoorRow  row of the door used to enter (-1 if start of game)
     * @param entryDoorCol  col of the door used to enter (-1 if start of game)
     */
    private void loadRoom(String filename, int entryDoorRow, int entryDoorCol)
            throws GameException {
        currentFilename = filename;
        GameObject[][] rawGrid = csvHandler.loadRoom(filename);

        // Scan for '@' placeholder and note its position
        int atRow = -1, atCol = -1;
        for (int r = 0; r < rawGrid.length; r++) {
            // CSVHandler returns null for '@' cells; we need to record the position
            // from the raw CSV. Since CSVHandler already set those cells to null,
            // we can only use it if parsing was split. Instead, we rely on the
            // initWithHeroAt / init logic.
            // (The '@' token is intentionally swallowed by CSVHandler into null.)
        }
        // Re-scan the file directly to find '@', if needed
        atRow = findHeroPosition(filename)[0];
        atCol = findHeroPosition(filename)[1];

        if (entryDoorRow >= 0) {
            mapGrid.init(rawGrid, hero, entryDoorRow, entryDoorCol);
        } else if (atRow >= 0) {
            mapGrid.initWithHeroAt(rawGrid, hero, atRow, atCol);
        } else {
            mapGrid.init(rawGrid, hero);
        }
    }

    /**
     * Scans the save-folder CSV for the '@' token to find the hero's
     * designated start position.
     * Returns {-1, -1} if no '@' is present.
     */
    private int[] findHeroPosition(String filename) {
        String path = csvHandler.getSaveDir()
                      + java.io.File.separator + filename;
        try (java.io.BufferedReader br =
                new java.io.BufferedReader(new java.io.FileReader(path))) {
            br.readLine(); // skip header
            int row = 0;
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",", -1);
                for (int col = 0; col < tokens.length; col++) {
                    if (tokens[col].trim().equals("@")) {
                        return new int[]{row, col};
                    }
                }
                row++;
            }
        } catch (java.io.IOException e) {
            // fall through – return not-found
        }
        return new int[]{-1, -1};
    }

    // ── Main loop ─────────────────────────────────────────────────────────────

    private void gameLoop() {
        while (true) {
            mapGrid.render(hero);

            // Check adjacency: if monsters are adjacent, show combat menu automatically
            List<int[]> adjMonsters = mapGrid.getAdjacentMonsters();
            if (!adjMonsters.isEmpty()) {
                boolean attacked = handleCombatTurn(adjMonsters);
                // Check hero death after combat
                if (!hero.isAlive()) {
                    mapGrid.render(hero);
                    System.out.println("You have been defeated. Game over.");
                    break;
                }
                if (attacked) {
                    continue; // re-render after attacking
                }
                // Skipped — fall through to movement input this turn
                System.out.println("You skipped combat. You may move.");
            }

            // Movement turn
            System.out.print("Enter command (w/a/s/d to move, q to quit): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("q")) {
                System.out.println("Thanks for playing. Goodbye!");
                break;
            }

            switch (input) {
                case "w": tryMove(-1,  0); break; // up
                case "s": tryMove( 1,  0); break; // down
                case "a": tryMove( 0, -1); break; // left
                case "d": tryMove( 0,  1); break; // right
                default:
                    System.out.println("Invalid command. Use w/a/s/d to move, q to quit.");
                    break;
            }

            // Check hero death (shouldn't happen without combat, but just in case)
            if (!hero.isAlive()) {
                mapGrid.render(hero);
                System.out.println("You have been defeated. Game over.");
                break;
            }
        }
    }

    /**
     * Combat turn: automatically triggered when hero is adjacent to one or more monsters.
     * Shows action menu (attack/skip) for each adjacent monster.
     */
    private boolean handleCombatTurn(List<int[]> adjMonsters) {
        // Show all adjacent monsters and their HP
        System.out.println("-- Nearby monsters --");
        for (int[] pos : adjMonsters) {
            Monster m = (Monster) mapGrid.getCell(pos[0], pos[1]);
            System.out.println("  " + m.getName()
                    + " [HP: " + m.getHp() + "/" + m.getMaxHp()
                    + " | Dmg: " + m.getDamage() + "]");
        }

        // If multiple monsters adjacent, ask which to engage
        int[] target;
        if (adjMonsters.size() == 1) {
            target = adjMonsters.get(0);
        } else {
            System.out.println("Choose a monster:");
            for (int i = 0; i < adjMonsters.size(); i++) {
                int[] pos = adjMonsters.get(i);
                Monster m = (Monster) mapGrid.getCell(pos[0], pos[1]);
                System.out.println("  " + (i + 1) + ". " + m.getName()
                        + " [HP: " + m.getHp() + "]");
            }
            System.out.print("Choice (number): ");
            String choice = scanner.nextLine().trim();
            int idx;
            try {
                idx = Integer.parseInt(choice) - 1;
            } catch (NumberFormatException e) {
                idx = -1;
            }
            if (idx < 0 || idx >= adjMonsters.size()) {
                System.out.println("Invalid choice. Turn skipped.");
                return false;
            }
            target = adjMonsters.get(idx);
        }

        Monster monster = (Monster) mapGrid.getCell(target[0], target[1]);
        System.out.println("-- " + monster.getName()
                + " [HP: " + monster.getHp() + "/" + monster.getMaxHp() + "] --");

        String action;
        while (true) {
            System.out.print("Action (f = attack, e = skip): ");
            action = scanner.nextLine().trim().toLowerCase();
            if (action.equals("f") || action.equals("e")) break;
            System.out.println("Invalid input. Please enter f to attack or e to skip.");
        }

        if (action.equals("e")) {
            System.out.println("You skipped.");
            return false;
        }

        // Hero must be armed to attack
        if (!hero.isArmed()) {
            System.out.println("You have no weapon! You can't attack.");
            return false;
        }

        // Simultaneous damage exchange
        int heroAttack    = hero.getWeaponDamage();
        int monsterAttack = monster.getDamage();
        monster.takeDamage(heroAttack);
        hero.takeDamage(monsterAttack);

        System.out.println("You dealt " + heroAttack + " damage to " + monster.getName()
                + ". (" + monster.getName() + " HP: " + monster.getHp() + ")");
        System.out.println(monster.getName() + " dealt " + monsterAttack
                + " damage to you. (Your HP: " + hero.getHp() + ")");

        // Monster defeated?
        if (monster.isDead()) {
            System.out.println(monster.getName() + " is defeated!");
            if (monster.dropsKey()) {
                mapGrid.setCell(target[0], target[1], new Key());
                System.out.println("The Troll dropped the Master Key (*)!");
            } else {
                mapGrid.setCell(target[0], target[1], null);
            }
        }
        return true; // attack was performed
    }

    // ── Movement ──────────────────────────────────────────────────────────────

    /**
     * Attempts to move the hero by (dr, dc).
     * Handles: boundary, monsters blocking, item pickup, door traversal.
     * @return true if the action was processed (even if movement failed)
     */
    private boolean tryMove(int dr, int dc) {
        int newRow = mapGrid.getHeroRow() + dr;
        int newCol = mapGrid.getHeroCol() + dc;

        if (!mapGrid.isInBounds(newRow, newCol)) {
            System.out.println("You can't move there – it's a wall.");
            return false;
        }

        GameObject target = mapGrid.getCell(newRow, newCol);

        // --- Blocked by monster ---
        if (target instanceof Monster) {
            System.out.println("A " + ((Monster) target).getName()
                    + " is blocking the way. Use 'a' to attack.");
            return false;
        }

        // --- Door ---
        if (target instanceof Door) {
            return handleDoor((Door) target, newRow, newCol);
        }

        // --- Move hero ---
        int prevRow = mapGrid.getHeroRow();
        int prevCol = mapGrid.getHeroCol();
        mapGrid.moveHero(newRow, newCol);
        mapGrid.setCell(newRow, newCol, hero);
        // Restore any item that was under the hero when they step away
        if (pendingPotion != null) {
            mapGrid.setCell(prevRow, prevCol, pendingPotion);
            pendingPotion = null;
        }
        if (pendingWeapon != null) {
            mapGrid.setCell(prevRow, prevCol, pendingWeapon);
            pendingWeapon = null;
        }

        // --- Interact with what was on the cell ---
        if (target instanceof Weapon) {
            handleWeaponPickup((Weapon) target, prevRow, prevCol);
        } else if (target instanceof Potion) {
            handlePotionPickup((Potion) target);
        } else if (target instanceof Key) {
            hero.pickUpKey();
            System.out.println("You picked up the Master Key!");
        }

        return true;
    }

    // ── Door logic ────────────────────────────────────────────────────────────

    private boolean handleDoor(Door door, int doorRow, int doorCol) {
        if (door.isMaster()) {
            if (hero.hasKey()) {
                System.out.println("You use the Master Key to open the door and escape the maze!");
                System.out.println("*** YOU WIN! ***");
                // Save state before exit
                trySaveRoom();
                System.exit(0);
            } else {
                System.out.println("The Master Door is locked. You need the Key (*).");
                return false;
            }
        }

        // Regular door – save current room then load the next one
        trySaveRoom();
        String targetFile = door.getTargetFilename();
        System.out.println("Moving to " + targetFile + "...");

        try {
            loadRoom(targetFile, doorRow, doorCol);
        } catch (GameException e) {
            System.out.println("[Error] Could not load room '" + targetFile + "': "
                    + e.getMessage());
            // Reload current room so the game can continue
            try {
                loadRoom(currentFilename, -1, -1);
            } catch (GameException ex) {
                System.out.println("[Fatal] Cannot reload current room. Exiting.");
                System.exit(1);
            }
            return false;
        }
        return true;
    }

    private void trySaveRoom() {
        try {
            csvHandler.saveRoom(currentFilename, mapGrid.getGrid());
        } catch (GameException e) {
            System.out.println("[Warning] Could not save room state: " + e.getMessage());
        }
    }

    // ── Item interactions ─────────────────────────────────────────────────────

    /**
     * Handles stepping onto a weapon cell.
     * The hero already occupies (heroRow, heroCol); the cell the hero came
     * FROM is (prevRow, prevCol) and is now empty — that is where we drop
     * any weapon that should remain on the floor.
     */
    private void handleWeaponPickup(Weapon newWeapon, int prevRow, int prevCol) {
        if (!hero.isArmed()) {
            // Simply equip – hero cell already holds the hero object, nothing to change
            hero.equipWeapon(newWeapon);
            System.out.println("You picked up a " + newWeapon.getName() + ".");
        } else {
            // Already armed – prompt to switch
            System.out.println("You already have a " + hero.getCurrentWeapon().getName()
                    + ". Switch to " + newWeapon.getName() + "? (y/n): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if (choice.equals("y")) {
                Weapon old = hero.getCurrentWeapon();
                hero.equipWeapon(newWeapon);
                // Drop old weapon onto the cell the hero just vacated
                mapGrid.setCell(prevRow, prevCol, old);
                System.out.println("Switched to " + newWeapon.getName()
                        + ". Dropped " + old.getName() + ".");
            } else {
                // Keep current weapon; store new weapon so it reappears when hero leaves
                pendingWeapon = newWeapon;
                System.out.println("Kept " + hero.getCurrentWeapon().getName() + ".");
            }
        }
    }

    private void handlePotionPickup(Potion potion) {
        if (hero.isFullHp()) {
            // Hero is on this cell; store the potion so we can put it back when hero leaves
            pendingPotion = potion;
            System.out.println("You are already at full HP. The potion stays under you.");
        } else {
            pendingPotion = null;
            hero.heal(potion.getHealAmount());
            System.out.println("You drank a potion and recovered "
                    + potion.getHealAmount() + " HP. HP: "
                    + hero.getHp() + "/" + hero.getMaxHp());
        }
    }

    // ── Combat ────────────────────────────────────────────────────────────────


    /**
     * Handles the 'a' command.
     * If only one monster is adjacent, attacks it immediately.
     * If multiple are adjacent, prompts the player to choose which to attack.
     */

    /**
     * Single-monster attack sequence:
     *  - Shows action menu (attack / skip)
     *  - On attack: simultaneous damage exchange
     *  - Removes monster if dead; drops key if Troll
     */
}
