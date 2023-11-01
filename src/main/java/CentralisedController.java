import members.Acceptor;
import members.Proposer;

import java.io.IOException;
import java.util.HashMap;

public class CentralisedController {
    HashMap<Integer, Acceptor> acceptors = new HashMap<>(); // ID: members.Acceptor
    HashMap<Integer, Proposer> proposers = new HashMap<>(); // ID: members.Proposer
//    public CentralisedController() {
//
//    }

    public void addProposer(Proposer proposer) throws IOException {
        new Thread(proposer).start();
        for (Proposer prop : proposers.values()) {
            if (proposer != prop) {
                proposer.connectToProposer(prop.getPort());
            }
        }
        for (Acceptor acceptor : acceptors.values()) {
            if (proposer != acceptor) {
                acceptor.connectToProposer(proposer.getPort());
            }
        }
        proposers.put(proposer.getUUID(), proposer);
        acceptors.put(proposer.getUUID(), proposer);
    }

    public void addAcceptor(Acceptor acceptor) throws IOException {
        new Thread(acceptor).start();
        for (Proposer proposer : proposers.values()) {
            if (proposer != acceptor) {
                acceptor.connectToProposer(proposer.getPort());
            }
        }
        acceptors.put(acceptor.getUUID(), acceptor);
    }

    public void propose(int id) {
        proposers.get(id).propose();
    }
    public void stopAll() {
        for (Acceptor acceptor : acceptors.values()) {
            acceptor.stop();
        }
    }
}
