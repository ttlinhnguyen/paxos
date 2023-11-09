package members;

import messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

class ProposerInputHandler implements Runnable {
    Proposer proposer;
    Socket acceptorSocket;

    /**
     * Handles messages from a specified Acceptor.
     * @param proposer The Proposer itself
     * @param acceptorSocket The socket of the Acceptor it's listening to
     */
    public ProposerInputHandler(Proposer proposer, Socket acceptorSocket) {
        this.proposer = proposer;
        this.acceptorSocket = acceptorSocket;
    }

    /**
     * While it's not stopped, listen to and handle incoming messages from a specified Acceptor.
     */
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


    /**
     * Stores the PROMISE message according to the proposal ID
     * @param message The PROMISE message from the Acceptor
     */
    private void receivePromise(Promise message) throws IOException {
        try {
            proposer.lock.acquire();
            proposer.promisedMap.get(message.proposalId).add(message);
            proposer.promisedSockets.get(message.proposalId).add(acceptorSocket);
            proposer.lock.release();
        } catch (InterruptedException ignored) {}
    }

    /**
     * Stores the ACCEPT message according to the proposal ID
     * @param message The ACCEPT message from the Acceptor
     */
    private void receiveAccept(Accept message) {
        proposer.acceptedMap.get(message.acceptedId).add(message);
    }
}
