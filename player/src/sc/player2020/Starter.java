package sc.player2020;

import jargs.gnu.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sc.player2020.logic.RandomLogic;
import sc.plugin2020.AbstractClient;
import sc.plugin2020.GameState;
import sc.plugin2020.IGameHandler;
import sc.shared.SharedConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Hauptklasse des Clients, die ueber Konsolenargumente gesteuert werden kann.
 * Sie veranlasst eine Verbindung zum Spielserver.
 */
public class Starter extends AbstractClient implements Cloneable{
  private static final Logger logger = LoggerFactory.getLogger(Starter.class);
  public RandomLogic logic  = new RandomLogic(this);
  
  public Starter(String host, int port, String reservation) throws Exception {
    // client starten
    super(host, port);
    logic = new RandomLogic(this);
    // Strategie zuweisen
    setHandler(logic);

    // einem Spiel beitreten
    if (reservation == null || reservation.isEmpty()) {
      joinAnyGame();
    } else {
      joinPreparedGame(reservation);
    }
  }

  public static void main(String[] args) {
    System.setProperty("file.encoding", "UTF-8");

    // parameter definieren
    CmdLineParser parser = new CmdLineParser();
    CmdLineParser.Option hostOption = parser.addStringOption('h', "host");
    CmdLineParser.Option portOption = parser.addIntegerOption('p', "port");
    CmdLineParser.Option reservationOption = parser.addStringOption('r', "reservation");

    try {
      // parameter auslesen
      parser.parse(args);
    } catch (CmdLineParser.OptionException e) {
      // bei Fehler die Hilfe anzeigen
      showHelp(e.getMessage());
      System.exit(2);
    }

    // parameter laden
    String host = (String) parser.getOptionValue(hostOption, "localhost");
    int port = (Integer) parser.getOptionValue(portOption, SharedConfiguration.DEFAULT_PORT);
    String reservation = (String) parser.getOptionValue(reservationOption, "");

    // einen neuen client erzeugen
    try {
      new Starter(host, port, reservation);
    } catch (Exception e) {
      logger.error("Beim Starten den Clients ist ein Fehler aufgetreten:", e);
      e.printStackTrace();
    }

  }

  private static void showHelp(String errorMsg) {
    String jarName = new File(Starter.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getName();
    System.out.println("\n" + errorMsg);
    System.out.println("\nBitte das Programm mit folgenden Parametern (optional) aufrufen: \n"
            + "java -jar " + jarName + " [{-h,--host} hostname]\n"
            + "                               [{-p,--port} port]\n"
            + "                               [{-r,--reservation} reservierung]");
    System.out.println("\nBeispiel: \n"
            + "java -jar " + jarName + " --host 127.0.0.1 --port 10500 --reservation 1234\n");
  }

}
