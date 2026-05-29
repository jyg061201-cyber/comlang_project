package mazegame.entity;

/**
 * 오크: HP 8, 데미지 3, 기호 O.
 */
public class Orc extends Monster {

    public Orc() {
        super(8, 3, "O");
    }

    /**
     * CSV에서 저장된 HP 값으로 오크를 복원할 때 사용하는 생성자.
     */
    public Orc(int hp) {
        super(8, 3, "O");
        this.hp = hp;
    }

    @Override
    public String getName() { return "Orc"; }
}
