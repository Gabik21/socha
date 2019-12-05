package gameLauncher;


import sc.server.Lobby;
import sc.server.gaming.GameRoom;

public class GameoverListener extends Thread {
	public boolean gameover=false;
	
	private Lobby server;
	private Thread t;
	private String threadName;
	private int gamesCount;

	GameoverListener( String name,Lobby server) {
		this.server=server;
		threadName = name;
		System.out.println("Creating " +  threadName );
	}

	public void run() {
		System.out.println("Running " +  threadName );
		while(server.getGameManager().getGames().size()<1) {}
		GameRoom[] games =  server.getGameManager().getGames().toArray(new GameRoom[0]);
		while(true) {
			System.out.println(games[games.length-1].getStatus()==GameRoom.GameStatus.OVER);
			if(games[games.length-1].getStatus()==GameRoom.GameStatus.OVER) {
				gameover=true;
				gamesCount=games.length;
			}
			if(gameover==true) {
				while(server.getGameManager().getGames().size()<=gamesCount) {}
				games = (GameRoom[]) server.getGameManager().getGames().toArray();
				//gameover=false;
			}
		}
		//System.out.println("Thread " +  threadName + " exiting.");
	}

}
