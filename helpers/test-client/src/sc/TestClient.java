package sc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.thoughtworks.xstream.XStream;
import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.Option;
import org.slf4j.LoggerFactory;
import sc.framework.plugins.Player;
import sc.networking.INetworkInterface;
import sc.networking.TcpNetwork;
import sc.networking.clients.XStreamClient;
import sc.plugin2020.util.Constants;
import sc.protocol.LobbyProtocol;
import sc.protocol.requests.*;
import sc.protocol.responses.*;
import sc.server.Configuration;
import sc.shared.*;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.IntToDoubleFunction;

import static java.lang.Math.pow;
import static sc.Util.factorial;

/**
 * A simple CLI to test clients. Enables TestMode on startup.
 * <p>
 * Defaults:
 * <ul>
 * <li>starts on localhost 13051</li>
 * <li>displayNames: player1, player2</li>
 * <li>client location: ./defaultplayer.jar</li>
 * <li>canTimeout: true</li>
 * </ul>
 */
public class TestClient extends XStreamClient {
  private static final Logger logger = (Logger) LoggerFactory.getLogger(TestClient.class);

  private static final String gameType = "swc_2019_piranhas";
  private static final ClientPlayer[] players = {new ClientPlayer(), new ClientPlayer()};
  private static final File logDir = new File("logs").getAbsoluteFile();

  private static TestClient testclient;
  private static Double significance;
  private static int minTests;

  public static void main(String[] args) {
    System.setProperty("file.encoding", "UTF-8");

    // define commandline options
    CmdLineParser parser = new CmdLineParser();
    Option loglevelOption = parser.addStringOption("loglevel");
    Option serverOption = parser.addBooleanOption("start-server");
    Option hostOption = parser.addStringOption('h', "host");
    Option portOption = parser.addIntegerOption('p', "port");

    Option numberOfTestsOption = parser.addIntegerOption('t', "tests");
    Option minTestsOption = parser.addIntegerOption("min-tests");
    Option significanceOption = parser.addDoubleOption("significance");

    Option noTimeoutOption = parser.addBooleanOption("no-timeout");
    Option[] execOptions = {parser.addStringOption("player1"), parser.addStringOption("player2")};
    Option[] nameOptions = {parser.addStringOption("name1"), parser.addStringOption("name2")};
    Option[] noTimeoutOptions = {parser.addBooleanOption("no-timeout1"), parser.addBooleanOption("no-timeout2")};

    try {
      parser.parse(args);
    } catch (CmdLineParser.OptionException e) {
      logger.error(e.toString());
      e.printStackTrace();
      exit(2);
    }

    Configuration.loadServerProperties();

    // read commandline options
    String loglevel = (String) parser.getOptionValue(loglevelOption, null);
    if (loglevel != null) {
      Level level = Level.toLevel(loglevel, null);
      if (level == null)
        logger.warn(loglevel + " is not a valid LogLevel!");
      else
        logger.setLevel(level);
    }

    boolean startServer = (boolean) parser.getOptionValue(serverOption, false);
    String host = (String) parser.getOptionValue(hostOption, "localhost");
    int port = (int) parser.getOptionValue(portOption, SharedConfiguration.DEFAULT_TESTSERVER_PORT);

    int numberOfTests = (int) parser.getOptionValue(numberOfTestsOption, 100);
    significance = (Double) parser.getOptionValue(significanceOption);
    if (significance != null) {
      minTests = (int) parser.getOptionValue(minTestsOption, 20);
      if (numberOfTests > 170) {
        logger.error("With significance testing the number of tests must not exceed 170!");
        exit(2);
      }
    }

    boolean noTimeout = (boolean) parser.getOptionValue(noTimeoutOption, false);
    for (int i = 0; i < 2; i++) {
      players[i].canTimeout = !(noTimeout || (boolean) parser.getOptionValue(noTimeoutOptions[i], false));
      players[i].name = (String) parser.getOptionValue(nameOptions[i], "player" + (i + 1));
      players[i].executable = (String) parser.getOptionValue(execOptions[i], "./defaultplayer.jar");
      players[i].isJar = Util.isJar(players[i].executable);
    }
    if (players[0].name.equals(players[1].name)) {
      logger.warn("Both players have the same name, adding suffixes!");
      players[0].name = players[0].name + "-1";
      players[1].name = players[1].name + "-2";
    }
    logger.info("Player1: " + players[0]);
    logger.info("Player2: " + players[1]);

    try {
      if (startServer) {
        logger.info("Starting server...");
        ProcessBuilder builder = new ProcessBuilder("java", "-Dfile.encoding=UTF-8", "-jar", "server.jar", "--port", String.valueOf(port));
        logDir.mkdirs();
        builder.redirectOutput(new File(logDir, "server_port" + port + ".log"));
        builder.redirectError(new File(logDir, "server_port" + port + ".err"));
        Process server = builder.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::destroyForcibly));
        Thread.sleep(1000);
      }
      testclient = new TestClient(Configuration.getXStream(), sc.plugin2020.util.Configuration.getClassesToRegister(), host, port, numberOfTests);
      Runtime.getRuntime().addShutdownHook(new Thread(testclient::printScores));
    } catch (Exception e) {
      logger.error("Error while initializing: " + e.toString());
      e.printStackTrace();
      exit(2);
    }
  }

  private String host;
  private int port;

  /** number of tests that have already been run */
  private int finishedTests;
  /** total number of tests that should be executed */
  private int totalTests;

  private boolean terminateWhenPossible = false;
  private int playerScores = 0;
  private int irregularGames = 0;

  private ExecutorService waiter = Executors.newSingleThreadExecutor();

  public TestClient(XStream xstream, Collection<Class<?>> protocolClasses,
                    String host, int port, int totalTests) throws IOException {
    super(xstream, createTcpNetwork(host, port));
    LobbyProtocol.registerMessages(xstream);
    LobbyProtocol.registerAdditionalMessages(xstream, protocolClasses);
    this.host = host;
    this.port = port;
    this.totalTests = totalTests;
    start();
    logger.debug("Authenticating as administrator, enabling TestMode");
    send(new AuthenticateRequest(Configuration.getAdministrativePassword()));
    send(new TestModeRequest(true));
    logger.info("Waiting for server...");
  }

  private boolean gameProgressing = false;

  @Override
  protected void onObject(ProtocolMessage message) {
    if (message == null) {
      logger.warn("Received null message");
      return;
    }

    logger.trace("Received {}", message);
    if (message instanceof TestModeMessage) {
      boolean testMode = (((TestModeMessage) message).testMode);
      logger.debug("TestMode was set to {} - starting clients", testMode);
      prepareNewClients();
    } else if (message instanceof RoomPacket) {
      RoomPacket packet = (RoomPacket) message;
      if (packet.getData() instanceof GameResult) {
        if (gameProgressing) {
          gameProgressing = false;
          System.out.println();
        }
        GameResult result = (GameResult) packet.getData();
        if (!result.isRegular())
          irregularGames++;
        StringBuilder log = new StringBuilder("Game {} ended " +
                (result.isRegular() ? "regularly -" : "abnormally!") + " Winner: ");
        for (Player winner : result.getWinners())
          log.append(winner.getDisplayName()).append(", ");
        logger.warn(log.substring(0, log.length() - 2), finishedTests);

        finishedTests++;
        for (ClientPlayer player : players)
          send(new GetScoreForPlayerRequest(player.name));

        try {
          for (ClientPlayer player : players)
            player.proc.waitFor(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
        for (ClientPlayer player : players)
          if (player.proc.isAlive()) {
            logger.warn("ClientPlayer {} is not responding anymore. Killing...", player.name);
            player.proc.destroyForcibly();
          }

        if (finishedTests == totalTests)
          terminateWhenPossible = true;
        else
          prepareNewClients();
      } else {
        if (logger.isInfoEnabled() && !logger.isTraceEnabled()) {
          if (!gameProgressing) {
            System.out.print("Game progress: ");
            gameProgressing = true;
          }
          System.out.print("#");
        }
      }
    } else if (message instanceof PlayerScorePacket) {
      playerScores++;
      Score score = ((PlayerScorePacket) message).getScore();

      for (ClientPlayer player : players) {
        if (player.name.equals(score.getDisplayName())) {
          player.score = score;
          break;
        }
      }

      List<ScoreValue> values = score.getScoreValues();
      logger.info(String.format("New score for %s: Siegpunkte %s, \u2205Wert 1 %5.2f after %s of %s tests",
              score.getDisplayName(), values.get(0).getValue(), values.get(1).getValue(), finishedTests, totalTests));

      if (playerScores == 2 && (isSignificant() || terminateWhenPossible)) {
        printScores();
        exit(0);
      }

    } else if (message instanceof PrepareGameProtocolMessage) {
      logger.debug("Received PrepareGame - starting clients");
      playerScores = 0;
      PrepareGameProtocolMessage pgm = (PrepareGameProtocolMessage) message;
      send(new ObservationRequest(pgm.getRoomId()));
      try {
        for (int i = 0; i < 2; i++)
          startPlayer(i, pgm.getReservations().get((finishedTests + i) % 2));

        waiter.execute(() -> {
          int tests = finishedTests;
          int slept = 0;
          while (tests == finishedTests) {
            // Detect failed clients
            for (ClientPlayer player : players)
              if (!player.proc.isAlive()) {
                logger.error("{} crashed, look into {}", player.name, logDir);
                exit(2);
              }
            // Max game length: Roundlimit * 2 * 2 seconds, one second buffer per round
            if (slept > Constants.ROUND_LIMIT * 5) {
              logger.error("The game seems to hang, exiting!");
              exit(2);
            }
            try {
              Thread.sleep(1000);
              slept++;
            } catch (InterruptedException ignored) {
              break;
            }
          }
        });

      } catch (IOException e) {
        e.printStackTrace();
      }
    } else if (message instanceof ObservationProtocolMessage) {
      logger.debug("Successfully joined GameRoom as Observer");
    } else {
      logger.debug("Received uninteresting " + message.getClass().getSimpleName());
    }
  }

  private void startPlayer(int id, String reservation) throws IOException {
    ClientPlayer player = players[id];
    ProcessBuilder builder;
    if (player.isJar) {
      logger.debug("Invoking client {} with Java", player.name);
      builder = new ProcessBuilder("java", "-jar", "-mx1500m", player.executable, "-r", reservation, "-h", host, "-p", Integer.toString(port));
    } else {
      logger.debug("Invoking client {}", player.name);
      builder = new ProcessBuilder(player.executable, "--reservation", reservation, "--host", host, "--port", Integer.toString(port));
    }

    logDir.mkdirs();
    builder.redirectOutput(new File(logDir, players[id].name + "_Test" + finishedTests + ".log"));
    builder.redirectError(new File(logDir, players[id].name + "_Test" + finishedTests + ".err"));
    players[id].proc = builder.start();
    try {
      Thread.sleep(100);
    } catch (InterruptedException ignored) {
    }
  }

  /** prepares slots for new clients (if {@link #finishedTests} is even player1 starts, otherwise player2) */
  private void prepareNewClients() {
    SlotDescriptor[] slots = new SlotDescriptor[2];
    for (int i = 0; i < 2; i++)
      slots[(finishedTests + i) % 2] = new SlotDescriptor(players[i].name, players[i].canTimeout, false);
    logger.debug("Prepared client slots: " + Arrays.toString(slots));
    send(new PrepareGameRequest(gameType, slots[0], slots[1]));
  }

  private static void exit(int status) {
    if (testclient != null) {
      testclient.send(new CloseConnection());
      testclient.waiter.shutdownNow();
    }

    for (ClientPlayer player : players)
      if (player.proc != null)
        player.proc.destroyForcibly();

    if (status != 0)
      logger.warn("Terminating with exit code " + status);
    System.exit(status);
  }

  private static INetworkInterface createTcpNetwork(String host, int port) throws IOException {
    logger.info("Creating TCP Network for {}:{}", host, port);
    return new TcpNetwork(new Socket(host, port));
  }

  private boolean scoresPrinted = false;

  private void printScores() {
    if (scoresPrinted) return;
    try {
      logger.warn(String.format("\n" +
                      "=============== SCORES ================\n" +
                      "%s: %.0f\n" +
                      "%s: %.0f\n" +
                      "=======================================\n" +
                      "{} of {} games ended abnormally!",
              players[0].name, players[0].score.getScoreValues().get(0).getValue(),
              players[1].name, players[1].score.getScoreValues().get(0).getValue()),
              irregularGames, finishedTests);
      scoresPrinted = true;
    } catch (Exception ignored) {
    }
  }

  private boolean isSignificant() {
    if (significance == null || finishedTests < minTests)
      return false;
    int n = finishedTests;
    IntToDoubleFunction binominalPD = (int k) -> pow(0.5, k) * pow(0.5, n - k) * (factorial(n, k) / factorial(n - k));
    players:
    for (int i = 0; i < 2; i++) {
      double binominalCD = 0.0;
      for (int k = 0; k <= players[i].score.getScoreValues().get(0).getValue().intValue() / Constants.WIN_SCORE; k++) {
        binominalCD += binominalPD.applyAsDouble(k);
        if (binominalCD > significance)
          continue players;
      }
      logger.warn(String.format("%s is significantly better! Uncertainty: %.2f%%", players[(i + 1) % 2].name, binominalCD * 100));
      return true;
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format("TestClient {Port: %d, Tests: %d/%d, Players: %s}", port, finishedTests, totalTests, Arrays.toString(players));
  }
}

class ClientPlayer {
  String name;
  boolean canTimeout;

  String executable;
  boolean isJar;

  @Override
  public String toString() {
    return String.format("ClientPlayer{name='%s', executable='%s', isJar=%s, canTimeout=%s}", name, executable, isJar, canTimeout);
  }

  Process proc;
  Score score;
}

class Util {

  static boolean isJar(String f) {
    return f.endsWith("jar") && new File(f).exists();
  }

  static double factorial(int n) {
    return n <= 1 ? 1 : factorial(n - 1) * n;
  }

  static double factorial(int n, int downTo) {
    return n <= downTo ? 1 : factorial(n - 1, downTo) * n;
  }

}
