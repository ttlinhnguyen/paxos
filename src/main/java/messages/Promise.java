package messages;

public class Promise extends Message {
    public int proposalId;
    public int acceptedId = -1;
    public int acceptedValue = -1;

    public Promise(int proposalId) {
        super(MessageType.PROMISE);
        this.proposalId = proposalId;
    }

    public Promise(int proposalId, int acceptedId, int acceptedValue) {
        super(MessageType.PROMISE);
        this.proposalId = proposalId;
        this.acceptedId = acceptedId;
        this.acceptedValue = acceptedValue;
    }

}
