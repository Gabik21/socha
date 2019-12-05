package gameLauncher;


import sc.server.serverApplication;
import sc.server.gaming.GameRoom;

public class serverThread extends Thread {
	public serverApplication server = new serverApplication();
	//public GameoverListener listener = new GameoverListener("listener",server.server);
	private Thread t;
	private String threadName;

	serverThread( String name) {
		threadName = name;
		System.out.println("Creating " +  threadName );
	}

	public void run() {
		System.out.println("Running " +  threadName );
		String[] test= {""};
		//listener.start();
		server.main();
		System.out.println("Thread " +  threadName + " exiting.");
	}

}
