package sc.player2020.logic;

import sc.api.plugins.IMove;
import sc.player2020.Starter;
import sc.player2020.logic.heuristic.IHeuristic;
import sc.plugin2020.util.GameRuleLogic;
import sc.shared.PlayerColor;

import java.util.List;

public class MinimaxLogic extends Logic {

  private final int initialDepth;
  private final IHeuristic heuristic;

  private PlayerColor favoredPlayer;
  private IMove bestMove;

  public MinimaxLogic(Starter client, int initialDepth, IHeuristic heuristic) {
    super(client);
    this.initialDepth = initialDepth;
    this.heuristic = heuristic;
  }

  @Override
  public void onRequestAction() {
    super.onRequestAction();
    favoredPlayer = currentPlayer.getColor();

    int rating = miniMax(initialDepth);

    log.info("rating: {}", rating);
    sendAction(bestMove);
  }

  public int miniMax(int depth) {

    if (depth == 0 || gameState.getRound() == 61) {
      return heuristic.ratePosition( gameState);
    }

    List<IMove> possibleMoves = GameRuleLogic.getPossibleMoves(gameState);

    if (possibleMoves.isEmpty()) {
      return heuristic.ratePosition(gameState);
    }

    int maxRating = Integer.MIN_VALUE;

    for (IMove move : possibleMoves) {

      applyMove(gameState, move);
      int rating = -miniMax(depth - 1);
      revokeMove(gameState, move);

      if (rating > maxRating) {
        maxRating = rating;
        if (depth == initialDepth) {
          bestMove = move;
        }
      }
    }

    return maxRating;
  }

}
