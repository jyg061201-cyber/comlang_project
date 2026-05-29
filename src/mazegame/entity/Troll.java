package mazegame.entity;

/**
 * 트롤: HP 15, 데미지 4, 기호 T.
 * 게임에 딱 한 마리만 존재하며, 처치 시 키를 드롭하는 유일한 몬스터다.
 */
public class Troll extends Monster {

    public Troll() {
        super(15, 4, "T");
    }

    /**
     * CSV에서 저장된 HP 값으로 트롤을 복원할 때 사용하는 생성자.
     */
    public Troll(int hp) {
        super(15, 4, "T");
        this.hp = hp;
    }

    @Override
    public String getName() { return "Troll"; }

    /** 트롤은 처치 시 항상 키를 드롭한다. */
    @Override
    public boolean dropsKey() { return true; }
}
