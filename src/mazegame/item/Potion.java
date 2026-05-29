package mazegame.item;

import mazegame.entity.GameObject;

/**
 * 모든 회복 물약의 추상 부모 클래스.
 * 영웅이 물약 칸으로 이동하면 자동으로 소비된다.
 * HP가 꽉 찬 경우에는 물약이 그 자리에 그대로 남는다.
 * 자식 클래스: MinorFlask, BigFlask.
 */
public abstract class Potion implements GameObject {

    protected int healAmount;

    public Potion(int healAmount) {
        this.healAmount = healAmount;
    }

    public int getHealAmount() { return healAmount; }
}
