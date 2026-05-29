package mazegame.entity;

/**
 * 맵 그리드의 셀을 차지할 수 있는 모든 객체가 구현하는 인터페이스.
 * 다형성의 핵심: Hero, Monster 하위 클래스, Weapon 하위 클래스,
 * Potion 하위 클래스, Door, Key 모두 이 인터페이스를 구현하여
 * MapGrid가 단일 GameObject[][] 배열에 모두 저장할 수 있게 한다.
 */
public interface GameObject {

    /**
     * 맵에서 이 객체를 나타내는 단일 문자 기호를 반환한다.
     * @return 표시 기호 문자열
     */
    String getSymbol();
}
