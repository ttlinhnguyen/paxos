package members;

import messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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

    @Override
    public void run() {
        try {
            while (acceptor.running) {
                try {
                    socket.setSoTimeout(1000);
                    Message request = (Message) inputStream.readObject();
                    socket.setSoTimeout(0);
                    Thread.sleep(acceptor.delay);
                    try {
                        if (request.type.equals(MessageType.PREPARE)) promise((Prepare) request);
                        else if (request.type.equals(MessageType.REQUEST_ACCEPT)) accept((RequestAccept) request);
                        else if (request.type.equals(MessageType.DECIDE)) decide((Decide) request);
                    } catch (Exception e) {}
                } catch (IOException e) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
    private void accept(RequestAccept request) throws IOException {
        if (acceptor.accept(request.proposalId, request.proposalValue)) {
            Message response = new Accept(acceptor.acceptedId);
            outputStream.writeObject(response);

            acceptor.debug(acceptor.UUID + " ACCEPT ID: " + acceptor.acceptedId);

        }
    }

    private void decide(Decide request) {
        try {
            socket.close();
        } catch (IOException e) {}
        acceptor.decide(request.id, request.value);
    }
}
