/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author wjddu
 */
// Monster.java
public class Monster implements GridEntity {
    private String data;

    public Monster(String data) {
        this.data = data; // "G:3", "O:8", "T:15" 형태 저장 [cite: 31-33]
    }

    @Override
    public char getSymbol() {
        return data.charAt(0); // 첫 글자인 G, O, T를 기호로 사용 [cite: 31-33]
    }
}
