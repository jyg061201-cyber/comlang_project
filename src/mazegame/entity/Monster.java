package mazegame.entity;

/**
 * 모든 몬스터의 추상 부모 클래스.
 * 공통 상태(hp, maxHp, damage, symbol)와 행동(takeDamage, isDead)을 정의한다.
 * 구체적인 스탯은 각 자식 클래스에서 설정한다.
 */
public abstract class Monster implements GameObject {

    protected int hp;
    protected int maxHp;
    protected int damage;
    protected String symbol;

    /**
     * @param hp     초기 및 최대 체력
     * @param damage 영웅에게 주는 공격력
     * @param symbol 맵에 표시되는 단일 문자
     */
    public Monster(int hp, int damage, String symbol) {
        this.hp = hp;
        this.maxHp = hp;
        this.damage = damage;
        this.symbol = symbol;
    }

    @Override
    public String getSymbol() { return symbol; }

    public int getHp()     { return hp; }
    public int getMaxHp()  { return maxHp; }
    public int getDamage() { return damage; }

    /**
     * 주어진 양만큼 HP를 감소시킨다. 최솟값은 0.
     */
    public void takeDamage(int amount) {
        hp = Math.max(0, hp - amount);
    }

    /** HP가 0이 되면 true를 반환한다. */
    public boolean isDead() { return hp <= 0; }

    /** 전투 메뉴에 표시되는 몬스터 이름. */
    public abstract String getName();

    /**
     * 몬스터가 처치될 때 호출된다.
     * 아이템을 드롭하는 자식 클래스에서 오버라이드한다 (예: Troll → Key 드롭).
     * @return 처치 시 키를 드롭하면 true
     */
    public boolean dropsKey() { return false; }
}
