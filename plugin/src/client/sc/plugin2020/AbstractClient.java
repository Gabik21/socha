package sc.plugin2020;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sc.api.plugins.IMove;
import sc.framework.plugins.Player;
import sc.framework.plugins.protocol.MoveRequest;
import sc.networking.clients.IControllableGame;
import sc.networking.clients.ILobbyClientListener;
import sc.networking.clients.LobbyClient;
import sc.plugin2020.util.Configuration;
import sc.protocol.responses.PrepareGameProtocolMessage;
import sc.protocol.responses.ProtocolErrorMessage;
import sc.shared.GameResult;
import sc.shared.PlayerColor;
import sc.shared.WelcomeMessage;

import java.io.IOException;
import java.net.ConnectException;

/**
 * Abstrakter Client nach Vorschrift des SDK.
 * Beinhaltet einen LobbyClient, der den tatsächlichen Client darstellt.
 */
public abstract class AbstractClient implements ILobbyClientListener, Cloneable{
  private static final Logger logger = LoggerFactory.getLogger(AbstractClient.class);

  /** The handler reacts to messages from the server received by the lobby client */
  protected IGameHandler handler;

  /** The lobby client, that connects to the room */
  private LobbyClient client;

  private String gameType;

  /** If the client made an error (rule violation), store reason here */
  private String error;

  /** current id to identify the client instance internal */
  private PlayerType id;
  /** the current room in which the player is */
  private String roomId;
  /** the current host */
  private String host;
  /** the current port */
  private int port;
  /** current figurecolor to identify which client belongs to which player */
  private PlayerColor color;


  public AbstractClient(String host, int port, PlayerType id) throws IOException {
    this.gameType = GamePlugin.PLUGIN_UUID;
    try {
      this.client = new LobbyClient(Configuration.getXStream(), Configuration.getClassesToRegister(), host, port);
    } catch(ConnectException e) {
      logger.error("Could not connect to Server: " + e.getMessage());
      System.exit(1);
    }
    this.client.addListener(this);
    this.client.start();
    this.id = id;
    this.port = port;
    this.host = host;
    this.error = null;
  }

  /** wenn es nur einen client gibt */
  public AbstractClient(String host, int port) throws IOException {
    this(host, port, PlayerType.PLAYER_ONE);
  }

  public void setHandler(IGameHandler handler) {
    this.handler = handler;
  }

  /**
   * Tell this client to observe the game given by the preparation handler
   *
   * @return controllable game
   */
  public IControllableGame observeGame(PrepareGameProtocolMessage handle) {
    return this.client.observe(handle);
  }

  /**
   * Called when a new message is sent to the room, e.g. move requests
   */
  @Override
  public void onRoomMessage(String roomId, Object data) {
    if(data instanceof MoveRequest) {
      this.handler.onRequestAction();
    } else if(data instanceof WelcomeMessage) {
      this.color = ((WelcomeMessage) data).getPlayerColor();
    }
    this.roomId = roomId;
  }

  /**
   * sends the <code>move</code> to the server
   *
   * @param move the move you want to do
   */
  public void sendMove(IMove move) {
    this.client.sendMessageToRoom(this.roomId, move);
  }

  /** Called when an error is sent to the room */
  @Override
  public void onError(String roomId, ProtocolErrorMessage response) {
    logger.debug("onError: Client {} received error {}", this, response.getMessage());
    this.error = response.getMessage();
  }

  /**
   * Called when game state has been received.
   * Happens after a client made a move.
   */
  @Override
  public void onNewState(String roomId, Object state) {
    sc.plugin2020.GameState gameState = (GameState) state;
    logger.debug("{} got new state {}", this, gameState);

    if(this.id != PlayerType.OBSERVER) {
      this.handler.onUpdate(gameState);

      if(gameState.getCurrentPlayer().getColor() == this.color) {
        // active player is own
        this.handler.onUpdate(gameState.getCurrentPlayer(), gameState.getOtherPlayer());
      } else {
        // active player is the enemy
        this.handler.onUpdate(gameState.getOtherPlayer(), gameState.getCurrentPlayer());
      }
    }
  }

  public void joinAnyGame() {
    this.client.joinRoomRequest(this.gameType);
  }

  @Override
  public void onGameJoined(String roomId) {
  }

  @Override
  public void onGamePrepared(PrepareGameProtocolMessage response) {
  }

  public void onGamePaused(String roomId, Player nextPlayer) {
  }

  @Override
  public void onGameObserved(String roomId) {
  }

  @Override
  public void onGameLeft(String roomId) {
    logger.info("{} got game left {}", this, roomId);
    this.client.stop();
  }

  @Override
  public void onGameOver(String roomId, GameResult data) {
    logger.debug("{} onGameOver got game result {}", this, data);
    if(this.handler != null) {
      this.handler.gameEnded(data, this.color, this.error);
    }
  }

  public void joinPreparedGame(String reservation) {
    this.client.joinPreparedGame(reservation);
  }

  public String getHost() {
    return this.host;
  }

  public int getPort() {
    return this.port;
  }

  public String getError() {
    return this.error;
  }

  public PlayerColor getColor() {
    return this.color;
  }

}
