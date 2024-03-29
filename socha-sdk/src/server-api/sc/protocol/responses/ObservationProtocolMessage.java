package sc.protocol.responses;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/** Sent to client as response to successfully joining a GameRoom as Observer */
@XStreamAlias(value = "observed")
public class ObservationProtocolMessage implements ProtocolMessage{
  @XStreamAsAttribute
  private String roomId;
  
  /** might be needed by XStream */
  public ObservationProtocolMessage() {
  }
  
  public ObservationProtocolMessage(String roomId) {
    this.roomId = roomId;
  }
  
  public String getRoomId() {
    return this.roomId;
  }
  
}