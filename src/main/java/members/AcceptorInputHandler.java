package members;

import messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class AcceptorInputHandler implements Runnable {
    Acceptor acceptor;
    Socket socket;
    ObjectOutputStream outputStream;
    ObjectInputStream inputStream;

    /**
     * Responsible for listening to messages from a certain proposer and responding correspondingly.
     * @param acceptor An acceptor
     * @param socket An acceptor's socket connection to a certain proposer
     * @throws IOException I/O Exception in the output stream and input stream
     */
    public AcceptorInputHandler(Acceptor acceptor, Socket socket) throws IOException {
        this.acceptor = acceptor;
        this.socket = socket;
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * While it's not stopped, listen to and handle incoming messages.
     */
    @Override
    public void run() {
        try {
            while (acceptor.running) {
                try {
                    socket.setSoTimeout(1000);
                    Message request = (Message) inputStream.readObject();
                    socket.setSoTimeout(0);
                    new Thread(() -> {
                        try {
                            Thread.sleep(acceptor.delay);
                            if (request.type.equals(MessageType.PREPARE)) promise((Prepare) request);
                            else if (request.type.equals(MessageType.REQUEST_ACCEPT)) accept((RequestAccept) request);
                            else if (request.type.equals(MessageType.DECIDE)) decide((Decide) request);
                        } catch (Exception e) {
                        }
                    }).start();
                } catch (IOException e) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * If the acceptor promises on the proposed ID, send a PROMISE message back to the Proposer.
     * @param request The PREPARE message from the Proposer
     */
    private void promise(Prepare request) throws IOException {
        if (acceptor.promise(request.proposalId)) {
            Message response = new Promise(request.proposalId);
            if (acceptor.acceptedId !=-1) {
                response = new Promise(request.proposalId, acceptor.acceptedId, acceptor.acceptedValue);
            }
            outputStream.writeObject(response);

            acceptor.debug(acceptor.UUID + " PROMISE ID: " + request.proposalId);
        }

    }

    /**
     * If the acceptor accepts the proposed ID, send an ACCEPT message back to the Proposer.
     * @param request The REQUEST_ACCEPT message from the Proposer
     */
    private void accept(RequestAccept request) throws IOException {
        if (acceptor.accept(request.proposalId, request.proposalValue)) {
            Message response = new Accept(acceptor.acceptedId);
            outputStream.writeObject(response);

            acceptor.debug(acceptor.UUID + " ACCEPT ID: " + acceptor.acceptedId);

        }
    }

    /**
     * Set the accepted ID and value to the ID and value that are decided across other members.
     * @param request The DECIDE message from the Proposer
     */
    private void decide(Decide request) throws InterruptedException {
        try {
            socket.close();
        } catch (IOException e) {}
        acceptor.decide(request.id, request.value);
    }
}
