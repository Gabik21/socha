package sc.plugin2017;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import sc.plugin2017.util.InvalidMoveException;

@XStreamAlias(value = "step")
public class Step extends Action {

  /**
   * Zeigt an welche Nummer die Aktion hat
   */
  @XStreamAsAttribute
  public int order;
  /**
   * Anzahl der Felder, die zurückgelegt werden.
   */
  @XStreamAsAttribute
  public int distance;
  /**
   * Zeigt an, um wie viele Punkte die Geschwindigkeit am Ende des Zuges verringert werden soll
   */
  @XStreamOmitField
  protected int reduceSpeed;
  /**
   * Das fahren auf eine Sandbank beendet den Zug
   */
  @XStreamOmitField
  protected boolean endsTurn;
  
  public Step(int distance) {
    this.distance = distance;
    reduceSpeed = 0;
    endsTurn = false;
  }
  
  public Step(int distance, int order) {
    this.distance = distance;
    this.order = order;
    reduceSpeed = 0;
    endsTurn = false;
  }
  
  @Override
  public int perform(GameState state, Player player) throws InvalidMoveException {
    int neededSpeed = 0;
    Field start = player.getField(state.getBoard());
    List<Field> nextFields = new ArrayList<Field>();
    int direction = player.getDirection();
    if(distance == 0 || distance > 6 || distance < -1) {
      throw new InvalidMoveException("Zurückgelegte Distanz ist ungültig.");
    }
    if(distance == -1) { // Fall rückwärts von Sandbank
      if(start.getType() != FieldType.SANDBAR) {
        throw new InvalidMoveException("Rückwärtszug ist nur von Sandbank aus möglich.");
      }
      Field next = start.getFieldInDirection(GameState.getOppositeDirection(direction), state.getBoard());
      if(next == null || next.getType() == FieldType.LOG || !next.isPassable()) {
        throw new InvalidMoveException("Der Weg ist versperrt");
      }
      state.put(next.getX(), next.getY(), player);
      return 1;
    } else {
      nextFields.add(start);
      // Kontrolliere für die Zurückgelegte Distanz, wie viele Bewegunsgpunkte verbraucht werden und ob es möglich ist, soweit zu ziehen
      for(int i = 0; i < distance; i++) {
        nextFields.add(nextFields.get(i).getFieldInDirection(player.getDirection(), state.getBoard()));
        Field checkField = nextFields.get(i);
        if(!checkField.isPassable() || 
            (state.getOtherPlayer().getField(state.getBoard()).equals(checkField) && i != distance -1)) {
          throw new InvalidMoveException("Feld ist blockiert. Ungültiger Zug.");
        }
        if(checkField.getType() == FieldType.SANDBAR) {
          reduceSpeed = player.getSpeed() - 1;
          endsTurn = true;
          if(i != distance - 1) {
            // Zug endet hier, also darf nicht weitergelaufen werden
            throw new InvalidMoveException("Zug sollte bereits enden, da auf Sandbank gefahren wurde.");
          }
          return neededSpeed + 1;
        } else if(checkField.getType() == FieldType.LOG) {
          reduceSpeed++;
          neededSpeed += 2;
        } else {
          neededSpeed += 1;
        }
        
      }
    }
    return neededSpeed;
  }
  
  public Step clone() {
    return new Step(this.distance);
  }
  
  public boolean equals(Object o) {
    if(o instanceof Step) {
      return (this.distance == ((Step) o).distance);
    }
    return false;
  }
  
  public String toString() {
    return "Bewegung um " + distance + " Felder";
  }

}
