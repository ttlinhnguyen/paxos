package messages;

public class Prepare extends Message {
    public int proposalId;

    public Prepare(int proposalId) {
        super(MessageType.PREPARE);
        this.proposalId = proposalId;
    }
}
