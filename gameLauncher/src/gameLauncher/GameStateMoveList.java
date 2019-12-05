package gameLauncher;

import java.util.ArrayList;
import java.util.List;

import sc.api.plugins.IMove;
import sc.plugin2020.GameState;

public class GameStateMoveList implements Cloneable{
	public List<GameState> gameStates=null;
	public List<IMove> moves=null;
	public int length;
	
	public GameStateMoveList(List<GameState> gameStates) {
		this.gameStates=gameStates;
		length=gameStates.size()-1;
		this.moves= new ArrayList<IMove>();
		for(int i=1;i<gameStates.size();i++) {
			this.moves.add(gameStates.get(i).getLastMove());
		}
	}
	public void println(int i) {
		System.out.println(gameStates.get(i));
		System.out.println(moves.get(i));
	}
	
}
