/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mazegame.entity;

/**
 *
 * @author wjddu
 */
    
import mazegame.item.Weapon;

/**
 * 플레이어가 조종하는 영웅(@).
 * GameObject 인터페이스를 구현하여 MapGrid의 grid 배열에 담긴다.
 */
public class Hero implements GameObject {

    private int hp;
    private final int maxHp = 25;
    private Weapon currentWeapon;  // null이면 비무장 상태
    private boolean hasKey;

    public Hero() {
        this.hp = maxHp;
        this.currentWeapon = null;
        this.hasKey = false;
    }

    // ── 체력 ──────────────────────────────────────

    /**
     * 데미지를 입는다. HP가 0 아래로 내려가지 않도록 보정한다.
     */
    public void takeDamage(int amount) {
        hp = hp - amount;
        if (hp < 0) hp = 0;
    }

    /**
     * 체력을 회복한다. maxHp를 초과하지 않도록 보정한다.
     * @return 실제로 회복된 양 (가득 찼으면 0)
     */
    public int heal(int amount) {
        if (hp == maxHp) return 0;
        int before = hp;
        hp = hp + amount;
        if (hp > maxHp) hp = maxHp;
        return hp - before;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    // ── 무기 ──────────────────────────────────────

    /**
     * 무기를 장착한다.
     */
    public void equipWeapon(Weapon weapon) {
        this.currentWeapon = weapon;
    }

    /**
     * 무기를 해제한다 (무기를 바닥에 내려놓을 때 사용).
     */
    public void unequipWeapon() {
        this.currentWeapon = null;
    }

    public boolean isArmed() {
        return currentWeapon != null;
    }

    // ── 키 ───────────────────────────────────────

    public void pickUpKey() {
        this.hasKey = true;
    }

    // ── GameObject 인터페이스 ──────────────────────

    @Override
    public char getSymbol() {
        return '@';
    }

    // ── Getters ───────────────────────────────────

    public int getHp()            { return hp; }
    public int getMaxHp()         { return maxHp; }
    public Weapon getCurrentWeapon() { return currentWeapon; }
    public boolean hasKey()       { return hasKey; }
}