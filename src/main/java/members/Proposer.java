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
    LamportClock clock;
//    static LamportClock clock = new LamportClock();
    int numAcceptors;
    HashMap<Integer, ArrayList<Promise>> promisedMap = new HashMap<>();
    HashMap<Integer, ArrayList<Accept>> acceptedMap = new HashMap<>();

    HashMap<Socket ,ObjectOutputStream> acceptorsOutStream = new HashMap<>();
    HashMap<Integer, ArrayList<Socket>> promisedSockets = new HashMap<>();
    public Proposer(int uuid, int port, int delay, int numAcceptors, LamportClock clock) {
        super(uuid, port, delay);
        this.numAcceptors = numAcceptors;
        this.clock = clock;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            listen(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Future<Boolean> propose() {
        return threadPool.submit(new ProposerOutputHandler(this));
    }


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
