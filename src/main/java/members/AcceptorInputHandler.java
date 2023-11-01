package members;

import messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class AcceptorInputHandler implements Runnable {
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
                    socket.setSoTimeout(5000);
                    Message request = (Message) inputStream.readObject();
                    if (request.type.equals(MessageType.PREPARE)) promise((Prepare) request);
                    else if (request.type.equals(MessageType.SEND_ACCEPT)) accept((SendAccept) request);
                } catch (IOException e) {
                }
            }
//        } catch (IOException e) {}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void promise(Prepare request) throws IOException {
        if (request.proposalId >= acceptor.highestPromiseId) {
            acceptor.highestPromiseId = request.proposalId;

            Message response = new Promise(request.proposalId);
            if (acceptor.acceptedId !=-1) {
                response = new Promise(request.proposalId, acceptor.acceptedId, acceptor.acceptedValue);
            }

            System.out.println(acceptor.UUID + " PROMISE " + acceptor.highestPromiseId);

            outputStream.writeObject(response);
//            outputStream.flush();
        }

    }
    public void accept(SendAccept request) throws IOException {
        if (request.proposalId >= acceptor.highestPromiseId) {
            acceptor.highestPromiseId = request.proposalId;
            acceptor.acceptedId = acceptor.highestPromiseId;
            acceptor.acceptedValue = request.proposalValue;

            Message response = new Accept(acceptor.acceptedId);

            System.out.println(acceptor.UUID + " ACCEPT " + acceptor.acceptedId);

            outputStream.writeObject(response);
//            outputStream.flush();
        }
    }
}
