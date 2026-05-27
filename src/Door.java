/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author wjddu
 */
// Door.java
public class Door implements GridEntity {
    private String targetFileName;

    public Door(String data) {
        // "d:room2.csv" 형태에서 파일명만 분리하여 저장 [cite: 40]
        if (data.startsWith("d:")) {
            String[] parts = data.split(":");
            if (parts.length > 1) {
                this.targetFileName = parts[1];
            }
        }
    }

    @Override
    public char getSymbol() {
        return 'd'; // 일반 문 기호 [cite: 42]
    }

    public String getTargetFileName() {
        return targetFileName;
    }
}