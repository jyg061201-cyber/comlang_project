package mazegame.io;

/**
 * 게임 I/O 관련 복구 가능한 오류를 위한 커스텀 예외 클래스.
 * CSV 파일 없음, 헤더 형식 오류, 잘못된 문 참조 등의 상황에 사용된다.
 * GameEngine과 CSVHandler가 일반 런타임 오류와 게임 전용 오류를
 * 구분하여 처리할 수 있도록 한다.
 */
public class GameException extends Exception {

    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
