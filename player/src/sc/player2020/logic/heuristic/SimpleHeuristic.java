package sc.player2020.logic.heuristic;

import sc.plugin2020.GameState;

public class SimpleHeuristic implements IHeuristic {

  @Override
  public int ratePosition(GameState state) {
    return state.getPointsForPlayer(state.getCurrentPlayerColor()) - state.getPointsForPlayer(state.getOtherPlayerColor());
  }

}
