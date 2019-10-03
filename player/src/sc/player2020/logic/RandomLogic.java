package sc.player2020.logic;

import sc.api.plugins.IMove;
import sc.player2020.Starter;
import sc.plugin2020.util.GameRuleLogic;

import java.util.List;

public class RandomLogic extends Logic {
  /**
   * Erzeugt ein neues Strategieobjekt, das zufaellige Zuege taetigt.
   *
   * @param client Der zugrundeliegende Client, der mit dem Spielserver kommuniziert.
   */
  public RandomLogic(Starter client) {
    super(client);
  }

  @Override
  public void onRequestAction() {
    super.onRequestAction();
    List<IMove> possibleMoves = GameRuleLogic.getPossibleMoves(gameState);
    sendAction(possibleMoves.get((int) (Math.random() * possibleMoves.size())));
  }
}
