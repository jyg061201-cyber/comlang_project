package mazegame.entity;

/**
 * 고블린: HP 3, 데미지 1, 기호 G.
 */
public class Goblin extends Monster {

    public Goblin() {
        super(3, 1, "G");
    }

    /**
     * CSV에서 저장된 HP 값으로 고블린을 복원할 때 사용하는 생성자.
     * @param hp CSV 셀에서 읽어온 현재 체력
     */
    public Goblin(int hp) {
        super(3, 1, "G");
        this.hp = hp;
    }

    @Override
    public String getName() { return "Goblin"; }
}
