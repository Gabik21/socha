package gameLauncher;

import sc.api.plugins.IMove;
import sc.plugin2020.GameState;

public class GameStateMove implements Cloneable{
	/****************************************************************************************
	 * Class for storing a GameState and the following move in a pair						* 
	 * @author Vincent Helbig																*
	 ****************************************************************************************/
	public GameState gameState=null;
	public IMove move=null;
	
	public GameStateMove(GameState gameState, IMove move) {
		this.gameState=gameState;
		this.move= move;
	}
	public void println() {
		System.out.println(gameState);
		System.out.println(move);
	}
	public String toString() {
		return(gameState.toString()+"\n"+move.toString());
	}
	
}
