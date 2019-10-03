package sc.player2020.logic.heuristic;

import sc.plugin2020.GameState;
import sc.shared.PlayerColor;

public interface IHeuristic {

  int ratePosition(PlayerColor playerColor, GameState state);

}
