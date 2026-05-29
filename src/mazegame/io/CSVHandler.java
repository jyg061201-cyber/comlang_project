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
 * 게임의 모든 CSV 파일 입출력을 담당한다.
 *  1. 게임 시작 시 실행별 저장 폴더를 생성하고 원본 CSV 파일을 복사한다.
 *  2. CSV 파일을 파싱하여 2D GameObject 배열로 변환한다.
 *  3. 방을 나갈 때 현재 맵 상태를 저장 폴더의 CSV에 덮어쓴다.
 *
 * 모든 경로는 상대 경로를 사용한다 — 절대 경로는 절대 사용하지 않는다.
 */
public class CSVHandler {

    /** 원본 CSV 방 파일이 있는 폴더 (작업 디렉터리 기준 상대 경로). */
    private static final String DATA_DIR = "data";

    /** 게임 시작 시 생성되는 실행별 저장 서브폴더. */
    private String saveDir;

    // ── 초기화 ────────────────────────────────────────────────────────────────

    /**
     * data/ 하위에 고유한 저장 폴더를 생성하고 data/ 안의 모든 CSV 파일을 복사한다.
     * 게임 시작 시 한 번만 호출해야 한다.
     *
     * @throws GameException data 디렉터리가 없거나 읽을 수 없는 경우
     */
    public void initSaveFolder() throws GameException {
        File dataFolder = new File(DATA_DIR);
        if (!dataFolder.exists() || !dataFolder.isDirectory()) {
            throw new GameException(
                "데이터 디렉터리 '" + DATA_DIR + "'를 찾을 수 없습니다. "
                + "프로그램 옆에 'data' 폴더를 만들고 CSV 파일을 넣어주세요.");
        }

        // 고유한 서브폴더 이름 생성: run_yyyyMMdd_HHmmss
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        saveDir = DATA_DIR + File.separator + "run_" + timestamp;

        File saveFolderFile = new File(saveDir);
        if (!saveFolderFile.mkdirs()) {
            throw new GameException("저장 폴더를 생성할 수 없습니다: " + saveDir);
        }

        // data/ 안의 모든 .csv 파일을 새 저장 폴더로 복사
        File[] csvFiles = dataFolder.listFiles(
                (dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            throw new GameException(
                "'" + DATA_DIR + "'에서 CSV 파일을 찾을 수 없습니다.");
        }
        for (File csv : csvFiles) {
            File dest = new File(saveDir + File.separator + csv.getName());
            try {
                Files.copy(csv.toPath(), dest.toPath(),
                           StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new GameException(
                    "'" + csv.getName() + "'을 저장 폴더로 복사하는 데 실패했습니다.", e);
            }
        }
    }

    /** 저장 폴더 경로(상대)를 반환한다. */
    public String getSaveDir() { return saveDir; }

    // ── CSV 파싱 ──────────────────────────────────────────────────────────────

    /**
     * 저장 폴더에서 CSV 파일을 읽어 2D GameObject 배열을 생성한다.
     * 첫 번째 줄: rows,cols (헤더)
     * 이후 줄: 쉼표로 구분된 셀 값
     *
     * 인식되는 셀 토큰:
     *   (빈칸)   → null (이동 가능한 빈 칸)
     *   @        → 영웅 초기 위치 표시 (GameEngine이 실제 배치)
     *   S        → Stick
     *   W        → WeakSword
     *   X        → StrongSword
     *   m        → MinorFlask
     *   B        → BigFlask
     *   *        → Key
     *   G 또는 G:n → Goblin (현재 HP 포함 가능)
     *   O 또는 O:n → Orc
     *   T 또는 T:n → Troll
     *   d:파일명  → 일반 문
     *   D        → 마스터 문
     *
     * @param filename CSV 파일명 (예: "start.csv"), 전체 경로 아님
     * @return [rows][cols] 크기의 GameObject 배열 (null = 빈 셀)
     * @throws GameException 파일 없음, 헤더 오류, 인식 불가 토큰 발생 시
     */
    public GameObject[][] loadRoom(String filename) throws GameException {
        String filePath = saveDir + File.separator + filename;
        File file = new File(filePath);

        if (!file.exists()) {
            throw new GameException(
                "방 파일을 찾을 수 없습니다: '" + filename + "' (" + saveDir + "에서 탐색)");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            // --- 헤더 라인 ---
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new GameException(
                    "'" + filename + "'이 비어있거나 헤더 라인이 없습니다.");
            }
            String[] dims = headerLine.trim().split(",");
            if (dims.length < 2) {
                throw new GameException(
                    "'" + filename + "' 헤더 라인 형식 오류: '" + headerLine + "'");
            }
            int rows, cols;
            try {
                rows = Integer.parseInt(dims[0].trim());
                cols = Integer.parseInt(dims[1].trim());
            } catch (NumberFormatException e) {
                throw new GameException(
                    "'" + filename + "' 헤더 라인에 정수가 아닌 값이 있습니다: '"
                    + headerLine + "'");
            }

            // --- 데이터 행 ---
            GameObject[][] grid = new GameObject[rows][cols];
            for (int r = 0; r < rows; r++) {
                String line = reader.readLine();
                if (line == null) {
                    // 행이 부족하면 빈 행으로 처리
                    break;
                }
                // 끝 쪽 빈 토큰을 유지하며 분할
                String[] tokens = splitCsvLine(line, cols);
                for (int c = 0; c < cols; c++) {
                    String token = (c < tokens.length) ? tokens[c].trim() : "";
                    grid[r][c] = parseToken(token, filename);
                }
            }
            return grid;

        } catch (IOException e) {
            throw new GameException(
                "'" + filename + "' 읽기 오류: " + e.getMessage(), e);
        }
    }

    /**
     * CSV 한 줄을 정확히 expectedCols개의 토큰으로 분할한다.
     * 줄이 짧으면 빈 문자열로 채운다.
     */
    private String[] splitCsvLine(String line, int expectedCols) {
        // -1 limit으로 끝 쪽 빈 필드도 유지
        String[] parts = line.split(",", -1);
        if (parts.length >= expectedCols) return parts;
        String[] padded = new String[expectedCols];
        for (int i = 0; i < expectedCols; i++) {
            padded[i] = (i < parts.length) ? parts[i] : "";
        }
        return padded;
    }

    /**
     * 단일 CSV 셀 토큰을 적절한 GameObject로 변환한다.
     * 빈 셀(이동 가능한 바닥)은 null을 반환한다.
     */
    private GameObject parseToken(String token, String filename) throws GameException {
        if (token.isEmpty()) return null;

        switch (token) {
            case "@": return null; // 영웅 초기 위치 표시; GameEngine이 실제 배치
            case "S": return new Stick();
            case "W": return new WeakSword();
            case "X": return new StrongSword();
            case "m": return new MinorFlask();
            case "B": return new BigFlask();
            case "*": return new Key();
            case "D": return new Door(); // 마스터 문
            default:
                // 선택적 HP 포함 몬스터: G, G:3, O:8, T:15 ...
                if (token.startsWith("G")) return parseMonster(token, "G", filename);
                if (token.startsWith("O")) return parseMonster(token, "O", filename);
                if (token.startsWith("T")) return parseMonster(token, "T", filename);
                // 일반 문: d:파일명.csv
                if (token.startsWith("d:")) {
                    String target = token.substring(2).trim();
                    if (target.isEmpty()) {
                        throw new GameException(
                            "'" + filename + "'의 문 셀에 목적지 파일명이 없습니다.");
                    }
                    return new Door(target);
                }
                // 알 수 없는 토큰 — 경고 출력 후 빈 칸으로 처리 (크래시 방지)
                System.out.println("[경고] '" + filename + "'에서 알 수 없는 셀 토큰 '"
                        + token + "' — 빈 칸으로 처리합니다.");
                return null;
        }
    }

    /** "G", "G:3", "O:8", "T:2" 형태의 몬스터 토큰을 파싱한다. */
    private GameObject parseMonster(String token, String type, String filename)
            throws GameException {
        int hp = -1; // -1이면 기본 최대 HP 사용
        if (token.contains(":")) {
            String[] parts = token.split(":");
            if (parts.length >= 2 && !parts[1].trim().isEmpty()) {
                try {
                    hp = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    throw new GameException(
                        "'" + filename + "'에서 몬스터 HP 파싱 오류: '" + token + "'");
                }
            }
        }
        switch (type) {
            case "G": return (hp < 0) ? new Goblin()  : new Goblin(hp);
            case "O": return (hp < 0) ? new Orc()     : new Orc(hp);
            case "T": return (hp < 0) ? new Troll()   : new Troll(hp);
            default:  return null;
        }
    }

    // ── 상태 저장 ─────────────────────────────────────────────────────────────

    /**
     * 현재 맵 그리드 상태를 저장 폴더의 CSV에 덮어쓴다.
     * 영웅이 일반 문을 통해 방을 나갈 때마다 호출된다.
     *
     * 영웅 셀은 빈 칸으로 저장한다 (방을 다시 로드할 때 위치를 재계산).
     *
     * @param filename 저장 폴더에서 덮어쓸 CSV 파일명
     * @param grid     현재 2D 그리드
     * @throws GameException 파일을 쓸 수 없는 경우
     */
    public void saveRoom(String filename, GameObject[][] grid) throws GameException {
        String filePath = saveDir + File.separator + filename;
        int rows = grid.length;
        int cols = (rows > 0) ? grid[0].length : 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // 헤더
            writer.println(rows + "," + cols);
            // 데이터 행
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
                "'" + filename + "'에 방 상태를 저장할 수 없습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 단일 그리드 셀을 CSV 토큰 문자열로 변환한다.
     * 영웅 셀은 빈 칸으로 저장한다.
     * 몬스터는 현재 HP 포함하여 저장해 피해가 방문 간 유지된다.
     * 일반 문은 "d:파일명.csv" 형태로 저장해 목적지를 보존한다.
     */
    private String cellToToken(GameObject obj) {
        if (obj == null) return "";

        // 영웅 셀 → 빈 칸으로 저장 (로드 시 위치 재계산)
        if (obj instanceof mazegame.entity.Hero) return "";

        // 몬스터: 현재 HP 포함 저장으로 피해 유지
        if (obj instanceof mazegame.entity.Monster) {
            mazegame.entity.Monster m = (mazegame.entity.Monster) obj;
            return m.getSymbol() + ":" + m.getHp();
        }

        // 일반 문은 반드시 "d:파일명.csv" 형태로 저장
        // getSymbol()은 "d"만 반환하여 목적지 파일명이 사라지므로 주의
        if (obj instanceof mazegame.entity.Door) {
            mazegame.entity.Door door = (mazegame.entity.Door) obj;
            if (!door.isMaster()) {
                return "d:" + door.getTargetFilename();
            }
            return "D"; // 마스터 문
        }

        // 나머지 (무기, 물약, 키)는 기호 그대로 저장
        return obj.getSymbol();
    }

    // ── 유틸리티 ──────────────────────────────────────────────────────────────

    /**
     * 저장 폴더에 있는 CSV 파일명 목록을 반환한다.
     * 디버깅용이며 게임플레이에는 필요 없다.
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
