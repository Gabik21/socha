package sc.player2020.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sc.api.plugins.IMove;
import sc.framework.plugins.Player;
import sc.player2020.Starter;
import sc.plugin2020.*;
import sc.plugin2020.util.GameRuleLogic;
import sc.shared.GameResult;
import sc.shared.PlayerColor;

import java.util.Comparator;
import java.util.List;

public abstract class Logic implements IGameHandler {
  protected static final Logger log = LoggerFactory.getLogger(Logic.class);
  public static final long MAX_TIME = 1_800;

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

  public void applyMove(GameState gameState, IMove move) {
    GameRuleLogic.performMove(gameState, move);
  }

  public void revokeMove(GameState gameState, IMove move) {

    if (move instanceof SetMove) {
      gameState.getUndeployedPieces(((SetMove) move).getPiece().getOwner()).add(((SetMove) move).getPiece());
      gameState.getBoard().getField(((SetMove) move).getDestination()).getPieces().remove(((SetMove) move).getPiece());
    } else if (move instanceof DragMove) {
      Board board = gameState.getBoard();
      Piece pieceToIMove = board.getField(((DragMove) move).getDestination()).getPieces().pop();
      board.getField(((DragMove) move).getStart()).getPieces().push(pieceToIMove);
    }

    gameState.setCurrentPlayerColor(gameState.getOtherPlayerColor());
    gameState.setTurn(gameState.getTurn() - 1);

  }

  public GameState cloneState(GameState gameState) {
    return gameState.copy(gameState.getRed(), gameState.getBlue(), gameState.getBoard().clone(), gameState.getTurn(),
            gameState.getUndeployedPieces(PlayerColor.RED), gameState.getUndeployedPieces(PlayerColor.BLUE));
  }

  public List<IMove> getPossibleMoves(GameState gameState) {
    List<IMove> moves = GameRuleLogic.getPossibleDragMoves(gameState);
    List<SetMove> possibleSetMoves = GameRuleLogic.getPossibleSetMoves(gameState);

    possibleSetMoves.sort(Comparator.comparingInt(s -> s.getDestination().getX() * s.getDestination().getX()
            + s.getDestination().getY() * s.getDestination().getY()
            + s.getDestination().getZ() * s.getDestination().getZ()));

    moves.addAll(possibleSetMoves);
    return moves;
  }

}
