package mazegame.entity;

import mazegame.item.Weapon;

/**
 * 플레이어가 조종하는 영웅(@)을 나타낸다.
 * HP, 현재 장착 무기, 키 보유 여부를 관리한다.
 */
public class Hero implements GameObject {

    private int hp;
    private final int maxHp;
    private Weapon currentWeapon; // 무기 없으면 null
    private boolean hasKey;

    public Hero() {
        this.hp = 25;
        this.maxHp = 25;
        this.currentWeapon = null;
        this.hasKey = false;
    }

    @Override
    public String getSymbol() {
        return "@";
    }

    // ── HP ────────────────────────────────────────────────────────────────────

    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }

    /**
     * 주어진 양만큼 HP를 감소시킨다. 최솟값은 0.
     * @param amount 받은 데미지
     */
    public void takeDamage(int amount) {
        hp = Math.max(0, hp - amount);
    }

    /**
     * 주어진 양만큼 HP를 회복한다. 최댓값은 maxHp.
     * @param amount 회복량
     * @return 실제로 회복되었으면 true (이미 꽉 찬 경우 false)
     */
    public boolean heal(int amount) {
        if (hp >= maxHp) return false;
        hp = Math.min(maxHp, hp + amount);
        return true;
    }

    public boolean isAlive() { return hp > 0; }
    public boolean isFullHp() { return hp >= maxHp; }

    // ── 무기 ──────────────────────────────────────────────────────────────────

    public Weapon getCurrentWeapon() { return currentWeapon; }
    public boolean isArmed() { return currentWeapon != null; }

    /**
     * 주어진 무기를 장착한다.
     * 기존 무기를 맵에 내려놓는 처리는 호출자가 담당한다.
     */
    public void equipWeapon(Weapon weapon) {
        this.currentWeapon = weapon;
    }

    /** 현재 무기 데미지를 반환한다. 무기 없으면 0. */
    public int getWeaponDamage() {
        return (currentWeapon != null) ? currentWeapon.getDamage() : 0;
    }

    // ── 키 ────────────────────────────────────────────────────────────────────

    public boolean hasKey() { return hasKey; }
    public void pickUpKey() { this.hasKey = true; }

    // ── 화면 출력 ─────────────────────────────────────────────────────────────

    /** 매 턴 맵 위에 표시되는 스탯 라인을 반환한다. */
    public String getStatsLine() {
        String weaponName = (currentWeapon != null)
                ? currentWeapon.getName() + " (" + currentWeapon.getDamage() + ")"
                : "None";
        String keyStatus = hasKey ? "Yes" : "No";
        return "HP: " + hp + "/" + maxHp + " | Weapon: " + weaponName + " | Key: " + keyStatus;
    }
}
