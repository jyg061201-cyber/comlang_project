package mazegame;

import mazegame.core.GameEngine;

/**
 * 솔로 어드벤처 미로 게임의 진입점.
 * GameEngine 객체를 생성하고 게임을 시작한다.
 */
public class Main {
    public static void main(String[] args) {
        GameEngine engine = new GameEngine();
        engine.start();
    }
}
