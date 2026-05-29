package mazegame.item;

import mazegame.entity.GameObject;

/**
 * 영웅이 장착할 수 있는 모든 무기의 추상 부모 클래스.
 * 각 무기는 데미지 값과 표시 이름을 가진다.
 * 자식 클래스: Stick, WeakSword, StrongSword.
 */
public abstract class Weapon implements GameObject {

    protected int damage;

    public Weapon(int damage) {
        this.damage = damage;
    }

    public int getDamage() { return damage; }

    /** 스탯 라인에 표시되는 무기 이름. */
    public abstract String getName();
}
