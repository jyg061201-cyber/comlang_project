package mazegame.item;

import mazegame.entity.GameObject;

/**
 * 트롤 처치 시 드롭되는 마스터 키(*).
 * 게임 전체에 딱 하나만 존재한다.
 * 영웅이 키 칸을 밟으면 인벤토리에 추가되고 맵에서 제거된다.
 */
public class Key implements GameObject {

    @Override
    public String getSymbol() { return "*"; }
}
