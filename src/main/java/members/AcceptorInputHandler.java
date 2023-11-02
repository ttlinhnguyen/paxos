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
                    Thread.sleep(acceptor.delay);
                    socket.setSoTimeout(1000);
                    Message request = (Message) inputStream.readObject();
                    socket.setSoTimeout(0); // to prevent the effect of thread sleep
                    if (request.type.equals(MessageType.PREPARE)) promise((Prepare) request);
                    else if (request.type.equals(MessageType.REQUEST_ACCEPT)) accept((RequestAccept) request);
                    else if (request.type.equals(MessageType.DECIDE)) decide((Decide) request);
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

            System.out.println(acceptor.UUID + " PROMISE " + request.proposalId);
        }

    }
    private void accept(RequestAccept request) throws IOException {
        if (acceptor.accept(request.proposalId, request.proposalValue)) {
            Message response = new Accept(acceptor.acceptedId);
            outputStream.writeObject(response);

            System.out.println(acceptor.UUID + " ACCEPT " + acceptor.acceptedId);

        }
    }

    private void decide(Decide request) {
        try {
            socket.close();
        } catch (IOException e) {}
        acceptor.decide(request.id, request.value);
    }
}
