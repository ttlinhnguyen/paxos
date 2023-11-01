package messages;

public class Accept extends Message {
    public int acceptedId;
    public Accept(int acceptedId) {
        super(MessageType.ACCEPT);
        this.acceptedId = acceptedId;
    }
}
