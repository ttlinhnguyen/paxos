package members;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Acceptor implements Runnable {
    int UUID;
    int port;
    int delay;

    boolean running = true;

    int highestPromiseId;
    int acceptedId = -1;
    int acceptedValue;
    ServerSocket serverSocket;

//    Socket socket;
    ObjectOutputStream outStream;
    ObjectInputStream inStream;
    public Acceptor(int UUID, int port, int delay) {
        this.UUID = UUID;
        this.port = port;
        this.delay = delay;
        highestPromiseId = -1;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void stop() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException ignored) {}
    }

    public int getPort() {
        return port;
    }
    public int getUUID() { return UUID; }

    public void connectToProposer(int port) throws IOException {
        if (port == this.port) return;
        Socket socket = new Socket("localhost", port);
//        System.out.println(UUID + " connect to port " + port);
        new Thread(new AcceptorInputHandler(this, socket)).start();
    }

//    public synchronized int getHighestPromiseId() { return highestPromiseId; }
//    public synchronized void updateHighestPromiseId(int val) { highestPromiseId = val; }
//    public synchronized int getAcceptedId() { return acceptedId; }
//    public synchronized void updateAcceptId(int val) { acceptedId = val; }

}
