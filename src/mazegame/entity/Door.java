package mazegame.entity;

/**
 * 맵 경계에 있는 문 셀을 나타낸다.
 *
 * 일반 문(d): targetFilename으로 다른 방 CSV 파일과 연결된다.
 * 마스터 문(D): isMaster == true이며, 영웅이 키를 보유해야 통과 가능하다.
 */
public class Door implements GameObject {

    private final String targetFilename; // 예: "room2.csv", 마스터 문이면 null
    private final boolean isMaster;

    /**
     * 다른 방 파일로 연결되는 일반 문을 생성한다.
     * @param targetFilename 런타임에 셀에서 읽어온 CSV 파일명
     */
    public Door(String targetFilename) {
        this.targetFilename = targetFilename;
        this.isMaster = false;
    }

    /**
     * 마스터 문을 생성한다 (목적지 파일 없음).
     */
    public Door() {
        this.targetFilename = null;
        this.isMaster = true;
    }

    @Override
    public String getSymbol() {
        return isMaster ? "D" : "d";
    }

    public String getTargetFilename() { return targetFilename; }
    public boolean isMaster()         { return isMaster; }
}
