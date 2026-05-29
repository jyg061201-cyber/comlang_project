package mazegame.core;

import mazegame.entity.*;
import mazegame.item.*;
import mazegame.io.*;

import java.util.List;
import java.util.Scanner;

/**
 * 게임의 핵심 루프 클래스.
 *
 * 담당 역할:
 *  - CSVHandler 초기화 및 시작 방 로드.
 *  - 플레이어 입력 처리 (w/a/s/d 이동, q 종료).
 *  - 이동 규칙 적용: 아이템 획득, 물약 소비, 무기 교체, 문 이동, 전투.
 *  - 방 전환 관리 (현재 방 저장 → 새 방 로드).
 *  - 승리(마스터 문 + 키) 및 패배(영웅 HP = 0) 조건 감지.
 */
public class GameEngine {

    private final CSVHandler csvHandler;
    private final Scanner    scanner;
    private Hero             hero;
    private Potion           pendingPotion; // HP가 꽉 찼을 때 영웅 아래에 깔린 물약
    private Weapon           pendingWeapon; // 무기 교체를 거절했을 때 영웅 아래에 깔린 무기
    private MapGrid          mapGrid;
    private String           currentFilename;

    public GameEngine() {
        csvHandler = new CSVHandler();
        scanner    = new Scanner(System.in);
        hero       = new Hero();
        mapGrid    = new MapGrid();
    }

    // ── 시작 ──────────────────────────────────────────────────────────────────

    public void start() {
        // 실행별 저장 폴더 생성 및 CSV 파일 복사
        try {
            csvHandler.initSaveFolder();
        } catch (GameException e) {
            System.out.println("[Error] " + e.getMessage());
            System.out.println("Please check that the 'data' folder exists and contains CSV files.");
            return;
        }

        // 시작 방 로드
        try {
            loadRoom("start.csv", -1, -1);
        } catch (GameException e) {
            System.out.println("[Error] Could not load start.csv: " + e.getMessage());
            return;
        }

        // 메인 게임 루프 실행
        gameLoop();

        scanner.close();
    }

    // ── 방 로드 ───────────────────────────────────────────────────────────────

    /**
     * 방 파일을 로드하고 MapGrid를 (재)초기화한다.
     *
     * @param filename      로드할 CSV 파일명
     * @param entryDoorRow  진입한 문의 행 (게임 시작 시 -1)
     * @param entryDoorCol  진입한 문의 열 (게임 시작 시 -1)
     */
    private void loadRoom(String filename, int entryDoorRow, int entryDoorCol)
            throws GameException {
        currentFilename = filename;
        GameObject[][] rawGrid = csvHandler.loadRoom(filename);

        // CSV 파일에서 '@' 위치를 직접 스캔하여 영웅 초기 위치 파악
        int atRow = findHeroPosition(filename)[0];
        int atCol = findHeroPosition(filename)[1];

        if (entryDoorRow >= 0) {
            // 문을 통해 진입: 문 안쪽 칸에 배치
            mapGrid.init(rawGrid, hero, entryDoorRow, entryDoorCol);
        } else if (atRow >= 0) {
            // CSV에 '@' 표시가 있으면 해당 위치에 배치
            mapGrid.initWithHeroAt(rawGrid, hero, atRow, atCol);
        } else {
            // '@' 없음: (1,1) 또는 랜덤 빈 칸에 배치
            mapGrid.init(rawGrid, hero);
        }
    }

    /**
     * 저장 폴더의 CSV 파일에서 '@' 토큰을 찾아 영웅의 지정 시작 위치를 반환한다.
     * '@'가 없으면 {-1, -1}을 반환한다.
     */
    private int[] findHeroPosition(String filename) {
        String path = csvHandler.getSaveDir()
                      + java.io.File.separator + filename;
        try (java.io.BufferedReader br =
                new java.io.BufferedReader(new java.io.FileReader(path))) {
            br.readLine(); // 헤더 스킵
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
            // 찾지 못함 — fall through
        }
        return new int[]{-1, -1};
    }

    // ── 메인 루프 ─────────────────────────────────────────────────────────────

    private void gameLoop() {
        while (true) {
            mapGrid.render(hero);

            // 인접 몬스터 확인: 있으면 자동으로 전투 메뉴 표시
            List<int[]> adjMonsters = mapGrid.getAdjacentMonsters();
            if (!adjMonsters.isEmpty()) {
                boolean attacked = handleCombatTurn(adjMonsters);
                // 전투 후 영웅 사망 체크
                if (!hero.isAlive()) {
                    mapGrid.render(hero);
                    System.out.println("You have been defeated. Game over.");
                    break;
                }
                if (attacked) {
                    continue; // 공격 후 다시 렌더링
                }
                // 스킵 — 이번 턴에 이동 입력으로 넘어감
                System.out.println("You skipped combat. You may move.");
            }

            // 이동 입력 처리
            System.out.print("Enter command (w/a/s/d to move, q to quit): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("q")) {
                System.out.println("Thanks for playing. Goodbye!");
                break;
            }

            switch (input) {
                case "w": tryMove(-1,  0); break; // 위
                case "s": tryMove( 1,  0); break; // 아래
                case "a": tryMove( 0, -1); break; // 왼쪽
                case "d": tryMove( 0,  1); break; // 오른쪽
                default:
                    System.out.println("Invalid command. Use w/a/s/d to move, q to quit.");
                    break;
            }

            // 이동 중 사망 체크 (일반적으로 전투 없이는 발생하지 않지만 안전을 위해)
            if (!hero.isAlive()) {
                mapGrid.render(hero);
                System.out.println("You have been defeated. Game over.");
                break;
            }
        }
    }

    /**
     * 전투 턴: 영웅이 몬스터와 인접할 때 자동으로 호출된다.
     * 인접한 몬스터 목록을 받아 액션 메뉴(공격/스킵)를 표시한다.
     * @return 공격을 수행했으면 true, 스킵했으면 false
     */
    private boolean handleCombatTurn(List<int[]> adjMonsters) {
        // 인접 몬스터 목록과 HP 표시
        System.out.println("-- Nearby monsters --");
        for (int[] pos : adjMonsters) {
            Monster m = (Monster) mapGrid.getCell(pos[0], pos[1]);
            System.out.println("  " + m.getName()
                    + " [HP: " + m.getHp() + "/" + m.getMaxHp()
                    + " | Dmg: " + m.getDamage() + "]");
        }

        // 인접 몬스터가 여러 마리면 대상 선택
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

        // f 또는 e가 입력될 때까지 반복
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

        // 무기가 없으면 공격 불가
        if (!hero.isArmed()) {
            System.out.println("You have no weapon! You can't attack.");
            return false;
        }

        // 동시 데미지 교환
        int heroAttack    = hero.getWeaponDamage();
        int monsterAttack = monster.getDamage();
        monster.takeDamage(heroAttack);
        hero.takeDamage(monsterAttack);

        System.out.println("You dealt " + heroAttack + " damage to " + monster.getName()
                + ". (" + monster.getName() + " HP: " + monster.getHp() + ")");
        System.out.println(monster.getName() + " dealt " + monsterAttack
                + " damage to you. (Your HP: " + hero.getHp() + ")");

        // 몬스터 처치 확인
        if (monster.isDead()) {
            System.out.println(monster.getName() + " is defeated!");
            if (monster.dropsKey()) {
                mapGrid.setCell(target[0], target[1], new Key());
                System.out.println("The Troll dropped the Master Key (*)!");
            } else {
                mapGrid.setCell(target[0], target[1], null); // 몬스터 제거
            }
        }
        return true; // 공격 수행됨
    }

    // ── 이동 ──────────────────────────────────────────────────────────────────

    /**
     * 영웅을 (dr, dc) 방향으로 이동 시도한다.
     * 경계 초과, 몬스터 차단, 아이템 획득, 문 이동 등을 처리한다.
     * @return 이동이 처리되었으면 true (실패한 경우도 포함)
     */
    private boolean tryMove(int dr, int dc) {
        int newRow = mapGrid.getHeroRow() + dr;
        int newCol = mapGrid.getHeroCol() + dc;

        if (!mapGrid.isInBounds(newRow, newCol)) {
            System.out.println("You can't move there - it's a wall.");
            return false;
        }

        GameObject target = mapGrid.getCell(newRow, newCol);

        // 몬스터가 길을 막고 있는 경우
        if (target instanceof Monster) {
            System.out.println("A " + ((Monster) target).getName()
                    + " is blocking the way.");
            return false;
        }

        // 문 처리
        if (target instanceof Door) {
            return handleDoor((Door) target, newRow, newCol);
        }

        // 영웅 이동
        int prevRow = mapGrid.getHeroRow();
        int prevCol = mapGrid.getHeroCol();
        mapGrid.moveHero(newRow, newCol);
        mapGrid.setCell(newRow, newCol, hero);

        // 영웅이 떠난 칸에 임시 저장된 아이템 복원
        if (pendingPotion != null) {
            mapGrid.setCell(prevRow, prevCol, pendingPotion);
            pendingPotion = null;
        }
        if (pendingWeapon != null) {
            mapGrid.setCell(prevRow, prevCol, pendingWeapon);
            pendingWeapon = null;
        }

        // 이동한 칸의 아이템 상호작용
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

    // ── 문 처리 ───────────────────────────────────────────────────────────────

    private boolean handleDoor(Door door, int doorRow, int doorCol) {
        if (door.isMaster()) {
            if (hero.hasKey()) {
                System.out.println("You use the Master Key to open the door and escape the maze!");
                System.out.println("*** YOU WIN! ***");
                trySaveRoom();
                System.exit(0);
            } else {
                System.out.println("The Master Door is locked. You need the Key (*).");
                return false;
            }
        }

        // 일반 문 — 현재 방 저장 후 다음 방 로드
        trySaveRoom();
        String targetFile = door.getTargetFilename();
        System.out.println("Moving to " + targetFile + "...");

        try {
            loadRoom(targetFile, doorRow, doorCol);
        } catch (GameException e) {
            System.out.println("[Error] Could not load room '" + targetFile + "': "
                    + e.getMessage());
            // 현재 방을 다시 로드하여 게임 계속 진행
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

    // ── 아이템 상호작용 ───────────────────────────────────────────────────────

    /**
     * 무기 칸으로 이동했을 때 처리한다.
     * 영웅은 이미 현재 칸(heroRow, heroCol)에 있다.
     * 바닥에 남겨야 할 무기는 이전 칸(prevRow, prevCol)에 pendingWeapon으로 저장한다.
     */
    private void handleWeaponPickup(Weapon newWeapon, int prevRow, int prevCol) {
        if (!hero.isArmed()) {
            // 무기 없음 → 자동 장착
            hero.equipWeapon(newWeapon);
            System.out.println("You picked up a " + newWeapon.getName() + ".");
        } else {
            // 이미 무장 → 교체 여부 확인
            System.out.println("You already have a " + hero.getCurrentWeapon().getName()
                    + ". Switch to " + newWeapon.getName() + "? (y/n): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if (choice.equals("y")) {
                Weapon old = hero.getCurrentWeapon();
                hero.equipWeapon(newWeapon);
                // 기존 무기를 영웅이 떠난 칸에 드롭
                mapGrid.setCell(prevRow, prevCol, old);
                System.out.println("Switched to " + newWeapon.getName()
                        + ". Dropped " + old.getName() + ".");
            } else {
                // 교체 거절 → 새 무기를 pendingWeapon에 저장 (영웅이 떠나면 복원)
                pendingWeapon = newWeapon;
                System.out.println("Kept " + hero.getCurrentWeapon().getName() + ".");
            }
        }
    }

    private void handlePotionPickup(Potion potion) {
        if (hero.isFullHp()) {
            // HP가 꽉 찬 경우 → pendingPotion에 저장 (영웅이 떠나면 복원)
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
}
