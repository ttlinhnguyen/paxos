package members;

import messages.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Proposer extends Acceptor {
    static LamportClock clock = new LamportClock();
    int numAcceptors;
    HashMap<Integer, ArrayList<Promise>> promised = new HashMap<>();
    HashMap<Integer, ArrayList<Accept>> accepted = new HashMap<>();

    ArrayList<ObjectOutputStream> acceptorsOutStream = new ArrayList<>();
//    HashMap<Integer, ArrayList<ObjectOutputStream>> promisedOutStream = new HashMap<>();
    public Proposer(int uuid, int port, int delay, int numAcceptors) {
        super(uuid, port, delay);
        this.numAcceptors = numAcceptors;
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

    public void propose() {
        new Thread(new ProposerOutputHandler(this)).start();
    }


    private void listen(Proposer proposer) {
        new Thread(() -> {
            while (running) {
                try {
                    Socket acceptorSocket = serverSocket.accept();
                    acceptorsOutStream.add(new ObjectOutputStream(acceptorSocket.getOutputStream()));
                    new Thread(new ProposerInputHandler(proposer, acceptorSocket)).start();
                } catch (IOException e) {}
            }
        }).start();
    }

}
