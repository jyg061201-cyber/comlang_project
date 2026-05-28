/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package mazegame.entity;
/**
 *
 * @author wjddu
 */
/**
 * 맵(2D 배열)에 배치될 수 있는 모든 게임 객체의 공통 규격.
 * MapGrid의 GameObject[][] grid 배열에 담기 위해 모든 객체가 구현해야 한다.
 */
public interface GameObject {
    /**
     * 콘솔 화면에 출력될 기호를 반환한다.
     * 예: Hero → "@", Goblin → "G", Stick → "S"
     * @return 
     */
    char getSymbol();
}
 