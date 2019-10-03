package sc.player2020.logic.heuristic;

import sc.plugin2020.GameState;
import sc.shared.PlayerColor;

public class SimpleHeuristic implements IHeuristic {
  @Override
  public int ratePosition(PlayerColor playerColor, GameState state) {
    return state.getPointsForPlayer(playerColor) - state.getPointsForPlayer(playerColor.opponent());
  }
}
