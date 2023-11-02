package members;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Acceptor implements Runnable {
    int UUID;
    int port;
    int delay;

    boolean running = true;

    int highestPromiseId = -1;
    int acceptedId = -1;
    int acceptedValue = -1;
    boolean decided = false;
    ServerSocket serverSocket;
    ExecutorService threadPool;

    public Acceptor(int UUID, int port, int delay) {
        this.UUID = UUID;
        this.port = port;
        this.delay = delay;
        threadPool = Executors.newFixedThreadPool(20);
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
        threadPool.shutdown();
        try {
            serverSocket.close();
        } catch (IOException ignored) {}
    }

    public int getPort() {
        return port;
    }
    public int getUUID() { return UUID; }
    public int isDecided() {
        if (decided) return acceptedValue;
        return -1;
    }

    public void connectToProposer(int port) throws IOException {
        if (port == this.port) return;
        Socket socket = new Socket("localhost", port);
        threadPool.submit(new AcceptorInputHandler(this, socket));
    }

    protected synchronized boolean promise(int id) {
        if (id >= highestPromiseId) {
            highestPromiseId = id;
            return true;
        }
        return false;
    }

    protected synchronized boolean accept(int id, int value) {
        if (id >= highestPromiseId) {
            highestPromiseId = id;
            acceptedId = id;
            acceptedValue = value;
            return true;
        }
        return false;
    }

    protected synchronized void decide(int id, int value) {
        highestPromiseId = id;
        acceptedId = id;
        acceptedValue = value;
        decided = true;
        stop();
        System.out.println(UUID + " DECIDE ON VALUE " + acceptedValue);
    }


}
