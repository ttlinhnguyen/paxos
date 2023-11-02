package messages;

public class Decide extends Message {
    public int id;
    public int value;

    public Decide(int id, int value) {
        super(MessageType.DECIDE);
        this.id = id;
        this.value = value;
    }
}
