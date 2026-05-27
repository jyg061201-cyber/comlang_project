/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author wjddu
 */
// Weapon.java
public class Weapon implements GridEntity {
    private String type;

    public Weapon(String data) {
        this.type = data; // "S", "W", "X" 중 하나 저장 [cite: 23-28]
    }

    @Override
    public char getSymbol() {
        return type.charAt(0);
    }
}
