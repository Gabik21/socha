package sc.player2020.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sc.api.plugins.IMove;
import sc.framework.plugins.Player;
import sc.player2020.Starter;
import sc.plugin2020.GameState;
import sc.plugin2020.IGameHandler;
import sc.shared.GameResult;
import sc.shared.PlayerColor;

public abstract class Logic implements IGameHandler {
  protected static final Logger log = LoggerFactory.getLogger(Logic.class);

  protected Starter client;
  protected GameState gameState;
  protected Player currentPlayer;

  public Logic(Starter client) {
    this.client = client;
  }

  /**
   * {@inheritDoc}
   */
  public void gameEnded(GameResult data, PlayerColor color, String errorMessage) {
    log.info("Das Spiel ist beendet.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onRequestAction() {
    log.info("Es wurde ein Zug angefordert.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onUpdate(Player player, Player otherPlayer) {
    currentPlayer = player;
    log.info("Spielerwechsel: " + player.getColor());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onUpdate(GameState gameState) {
    this.gameState = gameState;
    currentPlayer = gameState.getCurrentPlayer();
    log.info("Zug: {} Spieler: {}", gameState.getTurn(), currentPlayer.getColor());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void sendAction(IMove move) {
    client.sendMove(move);
  }

}
