package members;

import messages.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Future;

public class Proposer extends Acceptor {
    LamportClock clock; // Clock that is shared with other Proposers to get a distinct proposal ID
    int numAcceptors; // Number of Acceptors it's connected to
    HashMap<Integer, ArrayList<Promise>> promisedMap = new HashMap<>(); // Map the proposal ID to an array of PROMISE messages
    HashMap<Integer, ArrayList<Accept>> acceptedMap = new HashMap<>(); // Map the proposal ID to an array of ACCEPT messages

    HashMap<Socket ,ObjectOutputStream> acceptorsOutStream = new HashMap<>(); // Map the Acceptor's socket to its output stream
    HashMap<Integer, ArrayList<Socket>> promisedSockets = new HashMap<>(); // Map the proposal ID to an array of sockets of Acceptors that have promised

    /**
     * Constructor for the Proposer role - a deprived class from {@code Acceptor}
     * @param uuid User ID of the Proposer
     * @param port Socket port
     * @param delay Delay time in milliseconds
     * @param numAcceptors Number of acceptors it will connect to
     * @param clock A clock shared with other Proposers to get a distinct proposal ID
     */
    public Proposer(int uuid, int port, int delay, int numAcceptors, LamportClock clock) {
        super(uuid, port, delay);
        this.numAcceptors = numAcceptors;
        this.clock = clock;
    }

    /**
     * Creates a server socket on a specified port and listens to connections from Acceptors.
     */
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            listen(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a proposal to other members
     * @return {@code true} if proposed successfully
     */
    public Future<Boolean> propose() {
        return threadPool.submit(new ProposerOutputHandler(this));
    }


    /**
     * Listens to connections from Acceptors and creates a new thread to handle messages from that Acceptor
     * @param proposer The Proposer itself
     */
    private void listen(Proposer proposer) {
        threadPool.submit(() -> {
            while (running) {
                try {
                    Socket acceptorSocket = serverSocket.accept();
                    acceptorsOutStream.put(acceptorSocket, new ObjectOutputStream(acceptorSocket.getOutputStream()));
                    threadPool.submit(new ProposerInputHandler(proposer, acceptorSocket));
                } catch (IOException e) {}
            }
        });
    }

}
