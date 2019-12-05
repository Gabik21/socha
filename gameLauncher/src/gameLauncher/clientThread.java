package gameLauncher;


import sc.player2020.Starter;
import sc.shared.SharedConfiguration;

public class clientThread extends Thread {
	public Starter player;
	//public GameoverListener listener = new GameoverListener("listener",server.server);
	private Thread t;
	private String threadName;

	clientThread( String name) {
		threadName = name;
		System.out.println("Creating " +  threadName );
	}

	public void run() {
		System.out.println("Running " +  threadName );
		try {
			player = new Starter("localhost",SharedConfiguration.DEFAULT_PORT,"");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		System.out.println("Thread " +  threadName + " exiting.");
	}

}
