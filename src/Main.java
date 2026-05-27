/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author wjddu
 */

// Main.java
public class Main {
    public static void main(String[] args) {
        // 1. Room 객체를 생성하고 start.csv 파일을 로드합니다. [cite: 108, 135]
        Room testRoom = new Room("start.csv");
        testRoom.loadFromCSV("start.csv");

        int rows = testRoom.getRows();
        int cols = testRoom.getCols();

        System.out.println("=== 맵 파싱 및 로드 테스트 (start.csv) ===");
        System.out.println("방 크기: " + rows + "x" + cols);
        System.out.println("-------------------------------------");

        // 2. 2D 배열을 순회하며 콘솔에 기호를 출력합니다.
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                GridEntity entity = testRoom.getEntity(i, j);
                if (entity != null) {
                    System.out.print("[" + entity.getSymbol() + "]");
                } else {
                    System.out.print("[?]");
                }
            }
            System.out.println(); // 줄바꿈
        }
        System.out.println("-------------------------------------");
    }
}