package sc.player2020.logic;

import sc.api.plugins.IMove;
import sc.player2020.Starter;
import sc.player2020.logic.heuristic.IHeuristic;
import sc.plugin2020.MissMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AlphaBetaLogic extends Logic {

  private static final Random RANDOM = new Random();

  private final int initialDepth;
  private final IHeuristic heuristic;

  private int currentDepth;
  private int lastFinishedDepth;
  private long startTime;
  private boolean timeout;

  public AlphaBetaLogic(Starter client, int initialDepth, IHeuristic heuristic) {
    super(client);
    this.initialDepth = initialDepth;
    this.heuristic = heuristic;
  }

  @Override
  public void onRequestAction() {
    super.onRequestAction();

    IMove bestMove = null;
    bestMoves.clear();
    timeout = false;
    currentDepth = initialDepth;
    startTime = System.currentTimeMillis();

    int bestRating = Integer.MIN_VALUE;

    while (!timeout) {
      log.info("new catch");
      int rating = minimax(currentDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
      if (!timeout) {
        lastFinishedDepth = currentDepth;
        // Special case first turn. We want the place always in the middle
        bestMove = bestMoves.get(gameState.getTurn() == 0 ? 0 : RANDOM.nextInt(bestMoves.size()));
        bestRating = rating;
      }
      currentDepth += 2;
    }

    log.info("Time to move: {}ms", System.currentTimeMillis() - startTime);
    log.info("Rating: {} Depth: {} Move: {}", bestRating, lastFinishedDepth, bestMove);
    if (bestMove != null) {
      sendAction(bestMove);
    } else {
      sendAction(new MissMove());
    }

  }

  private List<IMove> bestMoves = new ArrayList<>();

  /**
   * Recursive minimax combined with alpha beta pruning
   */
  public int minimax(int depth, int alpha, int beta) {

    if (System.currentTimeMillis() - startTime > MAX_TIME) {
      timeout = true;
      return 0;
    }

    if (depth == 0 || gameState.getRound() == 61) {
      return heuristic.ratePosition(gameState);
    }

    List<IMove> possibleMoves = getPossibleMoves(gameState);

    for (IMove move : possibleMoves) {

      applyMove(gameState, move);
      int rating = -minimax(depth - 1, -beta, -alpha);
      revokeMove(gameState, move);

      if (rating >= alpha) {

        if (depth == currentDepth) {
          if (rating > alpha) {
            bestMoves.clear();
            log.info("clear");
          }
          bestMoves.add(move);
          log.info("Add equally rated at {}", rating);
        }
        alpha = rating;

        if (alpha >= beta) {
          break;
        }

      }

    }

    return alpha;
  }

}
