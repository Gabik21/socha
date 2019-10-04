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
    // Reset timeout
    timeout = false;
    // Roll back to initial depth
    currentDepth = initialDepth;
    startTime = System.currentTimeMillis();

    IMove chosenMove = null;
    int lastFinishedDepth = 0;
    int bestRating = Integer.MIN_VALUE;

    // We do have MAX_TIME amount of time to "think" on our move.
    // If the last tree search was short enough we can go deeper and make the most of our time.
    while (!timeout) {

      // Reset working variables
      bestMoves.clear();

      // Do actual rating
      int rating = minimax(currentDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);

      if (!timeout) {
        lastFinishedDepth = currentDepth;
        // With simple heuristics we will get a huge amount of equally rated moves.
        // This will choose a random one, to make our client unpredictable.
        // + Special case first turn. We want the place always in the middle
        if (!bestMoves.isEmpty()) {
          chosenMove = bestMoves.get(gameState.getTurn() == 0 ? 0 : RANDOM.nextInt(bestMoves.size()));
          bestRating = rating;
        }
      }
      currentDepth++;
    }

    log.info("Rating: {} Depth: {} Move: {}", bestRating, lastFinishedDepth, chosenMove);

    if (chosenMove != null) {
      sendAction(chosenMove);
    } else {
      // It is possible to run into a position where no move is possible.
      // At this point we can just send miss moves and hope that we win by magic
      sendAction(new MissMove());
    }

  }

  private List<IMove> bestMoves = new ArrayList<>();

  /**
   * Recursive minimax combined with alpha beta pruning
   */
  private int minimax(int depth, int alpha, int beta) {

    // We are getting close to lose by time! Exit now.
    if (System.currentTimeMillis() - startTime > MAX_TIME) {
      timeout = true;
      return 0;
    }

    if (depth == 0 || gameState.getRound() == 60) {
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
          }
          bestMoves.add(move);
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
