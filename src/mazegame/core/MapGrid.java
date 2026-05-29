package mazegame.core;

import mazegame.entity.*;
import mazegame.item.*;

import java.util.Random;

/**
 * 하나의 방(Room) 데이터를 2D {@code GameObject[][]} 그리드로 보관하고
 * 콘솔에 렌더링하는 클래스.
 *
 * 다형성 활용: 그리드에는 Hero, Monster 하위 클래스, Weapon 하위 클래스,
 * Potion 하위 클래스, Door, Key, null(빈 칸) 등 모든 GameObject가 저장되며,
 * 렌더링 시 공통 getSymbol() 메서드를 통해 접근한다.
 */
public class MapGrid {

    private GameObject[][] grid;
    private int rows;
    private int cols;

    // 현재 영웅 위치
    private int heroRow;
    private int heroCol;

    private final Random random = new Random();

    // ── 초기화 ────────────────────────────────────────────────────────────────

    /**
     * 로드된 2D 배열로 그리드를 초기화하고 명세서 규칙에 따라 영웅을 배치한다.
     *  1. CSV에 '@' 표시가 있으면 해당 위치에 배치.
     *  2. 없으면 (1,1) 시도.
     *  3. (1,1)이 막혀있으면 빈 칸 중 랜덤 배치.
     *
     * @param grid CSVHandler에서 파싱한 2D 배열 (null = 빈 칸)
     * @param hero 배치할 Hero 객체
     */
    public void init(GameObject[][] grid, Hero hero) {
        this.grid = grid;
        this.rows = grid.length;
        this.cols = (rows > 0) ? grid[0].length : 0;

        placeHero(hero);
    }

    /**
     * 특정 문을 통해 방에 재진입할 때 사용하는 오버로드.
     * 영웅은 진입한 문의 안쪽 칸에 배치된다.
     *
     * @param grid         방 그리드
     * @param hero         Hero 객체
     * @param entryDoorRow 진입한 문의 행
     * @param entryDoorCol 진입한 문의 열
     */
    public void init(GameObject[][] grid, Hero hero, int entryDoorRow, int entryDoorCol) {
        this.grid = grid;
        this.rows = grid.length;
        this.cols = (rows > 0) ? grid[0].length : 0;

        // 문에서 안쪽으로 한 칸 이동한 위치 계산
        int inwardRow = entryDoorRow;
        int inwardCol = entryDoorCol;

        if (entryDoorRow == 0)             inwardRow = 1;
        else if (entryDoorRow == rows - 1) inwardRow = rows - 2;
        if (entryDoorCol == 0)             inwardCol = 1;
        else if (entryDoorCol == cols - 1) inwardCol = cols - 2;

        // 안쪽 칸이 비어있으면 그 위치에 배치; 아니면 일반 배치 로직 사용
        if (isInBounds(inwardRow, inwardCol) && grid[inwardRow][inwardCol] == null) {
            heroRow = inwardRow;
            heroCol = inwardCol;
            grid[heroRow][heroCol] = hero;
        } else {
            placeHero(hero);
        }
    }

    /** 명세서 규칙에 따라 영웅을 그리드에 배치한다. */
    private void placeHero(Hero hero) {
        // 규칙 2: (1,1) 시도
        if (isInBounds(1, 1) && grid[1][1] == null) {
            heroRow = 1;
            heroCol = 1;
            grid[heroRow][heroCol] = hero;
            return;
        }

        // 규칙 3: 랜덤 빈 칸
        placeHeroRandomly(hero);
    }

    /**
     * CSV에서 '@' 위치를 미리 찾은 경우 사용하는 초기화 메서드.
     * init() 대신 이 메서드를 호출한다.
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
            // 빈 칸이 전혀 없는 극단적 경우 — (0,0)에 강제 배치
            heroRow = 0; heroCol = 0;
        } else {
            int[] pos = empty.get(random.nextInt(empty.size()));
            heroRow = pos[0]; heroCol = pos[1];
        }
        grid[heroRow][heroCol] = hero;
    }

    // ── 렌더링 ────────────────────────────────────────────────────────────────

    /**
     * ASCII 벽과 영웅 스탯 라인을 포함하여 방을 System.out에 출력한다.
     * 형식:
     *   AdventureGame
     *   HP: x/25 | Weapon: ... | Key: ...
     *   +----------+
     *   | 셀 내용... |
     *   +----------+
     *
     * @param hero 스탯 라인 출력에 사용
     */
    public void render(Hero hero) {
        System.out.println("AdventureGame");
        System.out.println(hero.getStatsLine());

        // 상단 벽
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

        // 하단 벽
        System.out.println(topBottom);
    }

    // ── 이동 ──────────────────────────────────────────────────────────────────

    /** (r, c) 셀의 내용을 반환한다. 범위 밖이면 null. */
    public GameObject getCell(int r, int c) {
        if (!isInBounds(r, c)) return null;
        return grid[r][c];
    }

    /** (r, c) 셀에 객체를 설정한다. */
    public void setCell(int r, int c, GameObject obj) {
        if (isInBounds(r, c)) grid[r][c] = obj;
    }

    /** 영웅을 (newRow, newCol)로 이동시키고 이전 칸을 비운다. */
    public void moveHero(int newRow, int newCol) {
        grid[heroRow][heroCol] = null; // 이전 칸 비우기
        heroRow = newRow;
        heroCol = newCol;
        // 새 칸에 영웅 객체를 넣는 것은 호출자가 담당
    }

    public boolean isInBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    // ── 인접 탐색 ─────────────────────────────────────────────────────────────

    /**
     * 영웅 주변 상하좌우 4칸 중 몬스터가 있는 칸의 좌표 목록을 반환한다.
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

    // ── Getter ────────────────────────────────────────────────────────────────

    public int getHeroRow() { return heroRow; }
    public int getHeroCol() { return heroCol; }
    public int getRows()    { return rows; }
    public int getCols()    { return cols; }
    public GameObject[][] getGrid() { return grid; }
}
