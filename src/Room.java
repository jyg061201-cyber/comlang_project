/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author wjddu
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Room {
    private int rows;
    private int cols;
    private GridEntity[][] grid;
    private String fileName;

    // 생성자
    public Room(String fileName) {
        this.fileName = fileName;
    }

    // CSV 파일을 읽어서 2D 배열을 초기화하는 메서드
    public void loadFromCSV(String filePath) {
        // try-with-resources 구문을 사용해 파일을 안전하게 열고 닫습니다. 
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            
            // 1. 첫 번째 줄 읽기 (행과 열 크기) [cite: 18]
            String header = br.readLine();
            if (header != null) {
                String[] dimensions = header.split(",");
                this.rows = Integer.parseInt(dimensions[0].trim());
                this.cols = Integer.parseInt(dimensions[1].trim());
                this.grid = new GridEntity[rows][cols]; // 2D 배열 생성
            }

            // 2. 두 번째 줄부터 맵 데이터 읽기
            String line;
            int currentRow = 0;
            
            while ((line = br.readLine()) != null && currentRow < rows) {
                // limit을 -1로 주어 끝에 있는 빈칸(,)도 무시하지 않고 배열에 포함시킵니다.
                String[] cells = line.split(",", -1);
                
                for (int currentCol = 0; currentCol < cols; currentCol++) {
                    String cellData = "";
                    // ArrayIndexOutOfBounds 방지
                    if (currentCol < cells.length) {
                        cellData = cells[currentCol].trim();
                    }
                    
                    // 3. 읽어온 문자열을 기반으로 객체 생성 후 그리드에 배치
                    grid[currentRow][currentCol] = createEntityFromString(cellData);
                }
                currentRow++;
            }
            
        } catch (IOException | NumberFormatException e) {
            // 파일이 없거나 잘못된 형식일 때 프로그램이 튕기지 않도록 예외 처리 
            System.err.println("Error loading room file: " + filePath + " - " + e.getMessage());
        }
    }

    // 문자열을 분석해서 알맞은 객체(무기, 몬스터, 문 등)로 변환하는 팩토리 메서드
    private GridEntity createEntityFromString(String data) {
        if (data.isEmpty()) {
            return new EmptySpace(); // 빈 공간
        } else if (data.equals("@")) {
            return new HeroEntity(); // 영웅 시작 위치 [cite: 22]
        } else if (data.startsWith("d:")) {
            return new Door(data); // 일반 문 (예: "d:room2.csv") [cite: 40]
        } else if (data.equals("D")) {
            return new MasterDoor(); // 마스터 도어 [cite: 43]
        } else if (data.equals("S") || data.equals("W") || data.equals("X")) {
            return new Weapon(data); // 무기 [cite: 23-28]
        } else if (data.startsWith("G:") || data.startsWith("O:") || data.startsWith("T:")) {
            return new Monster(data); // 몬스터 (예: "G:3") [cite: 30-33]
        } else if (data.equals("m") || data.equals("B")) {
            return new Potion(data); // 포션 [cite: 36-38]
        } else {
            return new EmptySpace(); // 알 수 없는 값은 빈 공간으로 처리
        }
    }
    
    // (테스트용) 방의 크기와 내용을 출력해보는 메서드
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    // Room.java 내부 맨 아래에 추가
    public GridEntity getEntity(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return grid[row][col];
        }
        return null;
    }
}
