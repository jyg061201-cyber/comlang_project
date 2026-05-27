/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author wjddu
 */
// Potion.java
public class Potion implements GridEntity {
    private String type;

    public Potion(String data) {
        this.type = data; // "m" 또는 "B" 저장 [cite: 36-38]
    }

    @Override
    public char getSymbol() {
        return type.charAt(0);
    }
}