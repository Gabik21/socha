package gameLauncher;

import sc.server.Lobby;
import sc.server.gaming.GameRoom;
import sc.plugin2020.*;

import java.util.ArrayList;
import java.util.List;

import sc.api.plugins.IMove;

//import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class GameLauncher {
	/****************************************************************************************
	 * Class for starting Matches and doing calculations with their data					*
	 * The aim is to do about 200 Matches to get a small Dataset of 12000 GameStates and 	*
	 * Moves																				* 
	 * @author Vincent Helbig																*
	 ****************************************************************************************/
	static Lobby lobby=null;
	static serverThread server=new serverThread("server");
	/****************************************************************************************
	 * Method to start server and everything else											*
	 * @param nothing																	   	*
	 * @return nothing																	    *
	 ****************************************************************************************/
	public static void init() {
		server.start();
		lobby = server.server.server;
	}
	/****************************************************************************************
	 * Main method																			*
	 * @param nothing																	   	*
	 * @return nothing																	    *
	 ****************************************************************************************/
	public static void main(String[] args) {
		Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		root.detachAndStopAllAppenders(); //disable logging from server and players etc.
		
		init();
		makeMatch();
		stopServer();
		
	}
	/****************************************************************************************
	 * Method to start a Game and rethrieve it's gameStates and moves						*
	 * @param nothing(for now)															   	*
	 * @return List of a GameStates and Moves pair as a GameStateMove object 			    *
	 ****************************************************************************************/
	public static List<GameStateMove> makeMatch(){
		int gamesSize=lobby.getGameManager().getGames().size();
		
		clientThread client1 = new clientThread("client1");
		client1.start();	//start first client
		clientThread client2 = new clientThread("client2");
		client2.start(); 	//start second client
		
		while(lobby.getGameManager().getGames().size()<=gamesSize) {} // wait for both clients to connect and game to start
		
		GameRoom[] games =  lobby.getGameManager().getGames().toArray(new GameRoom[0]); // get started game
		while(games[games.length-1].getStatus()!=GameRoom.GameStatus.OVER) { //while the game isn't over
			System.out.println(client1.player.logic.gameStates.size()+"%"); //Progress Message(optinal)
		}
		List<GameState> gameStates = client1.player.logic.gameStates;	 // List for gameStates
		List<IMove> moves1 = client1.player.logic.moves;				 // List for client 1's moves
		List<IMove> moves2 = client2.player.logic.moves;				 // List for client 2's moves
		if(moves1.size() != moves2.size() || moves1.size()*2 != (gameStates.size()-1)) { // if this is true, something went wrong
			System.out.print("Error!!!");
			return null;
		}
		if(gameStates.get(0).getCurrentPlayerColor() != client1.player.getColor()) { // switch List to remain chronological order
			List<IMove> tmp = moves1;
			moves1=moves2;
			moves2 = tmp;
		}
		List<GameStateMove> list = new ArrayList<GameStateMove>(); // this is our return value
		for(int i=0;i< gameStates.size()-1;i++) {  //process because every client stored only it's own moves
			if(i%2==0) {
				list.add(new GameStateMove(gameStates.get(i),moves1.get(i/2)));
			}else {
				list.add(new GameStateMove(gameStates.get(i),moves2.get(i/2)));
			}
		}
		System.out.println("Game finished");
		return list;
	}
	/****************************************************************************************
	 * Method to stop server and everything else, we should not do many calculations after	*
	 *  calling this method																	*
	 * @param nothing																	   	*
	 * @return nothing																	    *
	 ****************************************************************************************/
	public static void stopServer() {
		server.stop(); //since we're not gonna do anything after that, we're using this method
	}
	
	

}
