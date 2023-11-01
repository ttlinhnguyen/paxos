package members;

import messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ProposerInputHandler implements Runnable {
    Proposer proposer;
    Socket acceptorSocket;
    public ProposerInputHandler(Proposer proposer, Socket acceptorSocket) {
        this.proposer = proposer;
        this.acceptorSocket = acceptorSocket;
    }

    @Override
    public void run() {

        try {
            ObjectInputStream inputStream = new ObjectInputStream(acceptorSocket.getInputStream());
            while (proposer.running) {
                try {
                    acceptorSocket.setSoTimeout(1000);
                    Message message = (Message) inputStream.readObject();
                    if (message.type.equals(MessageType.PROMISE)) receivePromise((Promise) message);
                    else if (message.type.equals(MessageType.ACCEPT)) receiveAccept((Accept) message);
                } catch (IOException e) {}
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void receivePromise(Promise message) throws IOException {
        proposer.promised.get(message.proposalId).add(message);
//        proposer.promisedOutStream.get(message.proposalId).add(new ObjectOutputStream(acceptorSocket.getOutputStream()));
    }
    private void receiveAccept(Accept message) {
        proposer.accepted.get(message.acceptedId).add(message);
    }
}
