package gameLauncher;

import sc.server.Lobby;
import sc.server.serverApplication;
import sc.shared.SharedConfiguration;
import sc.server.gaming.GameRoom;
import sc.shared.SharedConfiguration;

import sc.plugin2020.*;

import java.sql.ClientInfoStatus;
import java.util.ArrayList;
import java.util.List;

import sc.api.plugins.IMove;

//import org.slf4j.Logger;

import sc.player2020.Starter;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class GameLauncher {
	public static void main(String[] args) {
		Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		root.detachAndStopAllAppenders();
		// TODO Auto-generated method stub
		serverThread server = new serverThread("server");
		server.start();
		Lobby lobby = server.server.server;
		int gamesSize=lobby.getGameManager().getGames().size();
		clientThread client1 = new clientThread("client1");
		client1.start();
		//Starter client2s = new Starter("test2",2,"none");
		clientThread client2 = new clientThread("client2");
		client2.start();
		while(lobby.getGameManager().getGames().size()<=gamesSize) {}
		GameRoom[] games =  lobby.getGameManager().getGames().toArray(new GameRoom[0]);
		while(games[games.length-1].getStatus()!=GameRoom.GameStatus.OVER) {
			//System.out.println(games[games.length-1].getStatus());
			//System.out.println(client1.player);
			System.out.println(client1.player.logic.gameStates.size()+"%");
		}
		/*while(true) {
			System.out.println(server.listener.gameover);
		}*/
		System.out.println("test");
		System.out.println(client1.player.logic.gameStates.size());
		System.out.println(client1.player.logic.moves.size());
		System.out.println(client2.player.logic.moves.size());
		System.out.println(client1.player.logic.gameStates.get(4));
		System.out.println(client2.player.logic.gameStates.get(4));
		List<GameState> gameStates = client1.player.logic.gameStates;
		List<IMove> moves1 = client1.player.logic.moves;
		List<IMove> moves2 = client2.player.logic.moves;
		System.out.println(gameStates.size());
		System.out.println(moves1.size());
		System.out.println(moves2.size());
		if(moves1.size() != moves2.size() || moves1.size()*2 != (gameStates.size()-1)) {
			System.out.print("Error!!!");
			return;
		}
		if(gameStates.get(0).getCurrentPlayerColor() != client1.player.getColor()) {
			List<IMove> tmp = moves1;
			moves1=moves2;
			moves2 = tmp;
		}
		List<GameStateMove> list = new ArrayList<GameStateMove>();
		for(int i=0;i< gameStates.size()-1;i++) {
			if(i%2==0) {
				list.add(new GameStateMove(gameStates.get(i),moves1.get(i/2)));
			}else {
				System.out.print(i);
				list.add(new GameStateMove(gameStates.get(i),moves2.get(i/2)));
			}
		}
		System.out.println(list.get(0));
		//list.get(0).println();
		System.out.println("finished");
		
	}

}
