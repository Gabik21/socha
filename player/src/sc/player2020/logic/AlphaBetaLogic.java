package sc.player2020.logic;

import sc.player2020.Starter;
import sc.player2020.logic.heuristic.IHeuristic;
import sc.plugin2020.GameState;
import sc.shared.PlayerColor;

public class AlphaBetaLogic extends Logic {

  private final PlayerColor favoredPlayer;
  private final int initialDepth;
  private final IHeuristic heuristic;

  public AlphaBetaLogic(Starter client, PlayerColor favoredPlayer, int initialDepth, IHeuristic heuristic) {
    super(client);
    this.favoredPlayer = favoredPlayer;
    this.initialDepth = initialDepth;
    this.heuristic = heuristic;
  }

  @Override
  public void onRequestAction() {
    super.onRequestAction();

  }

  private int maximizer(int depth, int alpha, int beta, GameState gameState) {
    if (depth == 0) {
      return heuristic.ratePosition(favoredPlayer, gameState);
    }

    return alpha;
  }

  private int minimizer(int depth, int alpha, int beta, GameState gameState) {
    return 0;
  }

}
