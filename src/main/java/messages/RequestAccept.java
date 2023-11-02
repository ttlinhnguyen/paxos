package messages;

public class RequestAccept extends Message {
    public int proposalId;
    public int proposalValue;
    public RequestAccept(int proposalId, int proposalValue) {
        super(MessageType.REQUEST_ACCEPT);
        this.proposalId = proposalId;
        this.proposalValue = proposalValue;
    }
}
