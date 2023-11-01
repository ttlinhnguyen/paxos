package messages;

public class SendAccept extends Message {
    public int proposalId;
    public int proposalValue;
    public SendAccept(int proposalId, int proposalValue) {
        super(MessageType.SEND_ACCEPT);
        this.proposalId = proposalId;
        this.proposalValue = proposalValue;
    }
}
