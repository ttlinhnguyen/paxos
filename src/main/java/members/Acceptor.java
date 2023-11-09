package members;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class Acceptor implements Runnable {
    boolean DEBUG = true;

    int UUID;
    int port;
    int delay; // The response delay time in milliseconds
    boolean running = true;

    ServerSocket serverSocket;
    ExecutorService threadPool;
    Semaphore lock;

    // Paxos variables for Acceptor roles
    int highestPromiseId = -1;
    int acceptedId = -1;
    int acceptedValue = -1;
    boolean accepted = false;

    /**
     * Constructor for the acceptor role.
     * @param UUID User ID
     * @param port port where it runs
     * @param delay response time in milliseconds
     */
    public Acceptor(int UUID, int port, int delay) {
        this.UUID = UUID;
        this.port = port;
        this.delay = delay;
        threadPool = Executors.newFixedThreadPool(20);
        lock = new Semaphore(1);
    }

    /**
     * Create a server socket bound to a given port.
     */
    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Pause handling messages in a specified time.
     * @param time pausing time in milliseconds
     */
    public void pause(int time) {
        running = false;
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        running = true;
    }

    /**
     * Stop all threads and connections.
     */
    public void stop() {
        running = false;
        try {
            threadPool.shutdown();
            serverSocket.close();
        } catch (Exception ignored) {}

    }

    /**
     * Get the port its server socket is running on.
     * @return port of server socket
     */
    public int getPort() {
        return port;
    }

    /**
     * Get its User ID.
     * @return User ID
     */
    public int getUUID() { return UUID; }

    /**
     * Connect and listen to the proposer running on a specified port for messages.
     * @param port Port of the proposer
     * @throws IOException The error when creating a socket connecting to the proposer.
     */
    public void connectToProposer(int port) throws IOException {
        if (port == this.port) return;
        Socket socket = new Socket("localhost", port);
        threadPool.submit(new AcceptorInputHandler(this, socket));
    }

    /**
     * Paxos Stage 1b - Acceptor updates the highest ID it has promised.
     * If the specified ID is not lower than the promised one, it will promise that ID to be the highest.
     * @param id The ID received from the proposer
     * @return The result whether that ID becomes the highest promised ID.
     */
    protected synchronized boolean promise(int id) {
        if (id >= highestPromiseId) {
            highestPromiseId = id;
            return true;
        }
        return false;
    }

    /**
     * Paxos Stage 2b - Acceptor accepts the ID not lower than the promised one.
     * If the specified ID is not lower than the promised one, it will accept that specified ID.
     * @param id The ID received from the proposer
     * @param value The value received from the proposer
     * @return The result whether it accepts that value from the proposer.
     */
    protected synchronized boolean accept(int id, int value) {
        if (acceptedId==-1 && id >= highestPromiseId) {
            highestPromiseId = id;
            acceptedId = id;
            acceptedValue = value;
            return true;
        }
        return false;
    }

    /**
     * Paxos Stage 3b - Acceptor records the decided value across all other members.
     * @param id The ID received from the proposer
     * @param value The value everyone decides on
     */
    protected synchronized void decide(int id, int value) {
        highestPromiseId = id;
        acceptedId = id;
        acceptedValue = value;
        accepted = true;
        stop();
//        debug(UUID + " DECIDE ON VALUE " + acceptedValue);
    }

    protected void debug(String s) {
        if (DEBUG) System.out.println(s);
    }
}
