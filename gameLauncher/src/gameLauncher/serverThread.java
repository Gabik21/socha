package gameLauncher;


import sc.server.serverApplication;

public class serverThread extends Thread {
	/****************************************************************************************
	 * Class for starting and running Server in Background									* 
	 * @author Vincent Helbig																*
	 ****************************************************************************************/
	public serverApplication server = new serverApplication();
	//public GameoverListener listener = new GameoverListener("listener",server.server);
	private String threadName;

	serverThread( String name) {
		threadName = name;
		System.out.println("Creating " +  threadName );
	}

	public void run() {
		System.out.println("Running " +  threadName );
		//listener.start();
		server.main();
		System.out.println("Thread " +  threadName + " exiting.");
	}

}
