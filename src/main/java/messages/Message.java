package messages;

import java.io.Serializable;

public abstract class Message implements Serializable {
    public MessageType type;
//    public int proposalId;
//    public int acceptedId;
//    public int value;
//    public Message() {}
    public Message(MessageType messageType) {
        this.type = messageType;
    }
}

