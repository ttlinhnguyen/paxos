import members.Acceptor;
import members.Proposer;

import java.io.IOException;
import java.util.HashMap;

public class Controller {
    HashMap<Integer, Acceptor> acceptors; // ID: members.Acceptor
    HashMap<Integer, Proposer> proposers; // ID: members.Proposer
    Controller(ControllerBuilder builder) {
        acceptors = builder.acceptors;
        proposers = builder.proposers;
    }

    public void propose(int id) {
        proposers.get(id).propose();
    }
    public void stopAll() {
        for (Acceptor acceptor : acceptors.values()) {
            acceptor.stop();
        }
    }

    public static class DefaultController {
        public DefaultController() {}
        public Controller build() {
            Integer[] delays = {0, 200, 400, 50, 100, 150, 200, 250, 300};
            ControllerBuilder builder = new ControllerBuilder(9, 1000);
            for (int i=1; i<10; i++) {
                if (i<4) builder = builder.addProposer(i, delays[i-1]);
                else builder = builder.addAcceptor(i, delays[i-1]);
            }

            return builder.build();
        }
    }

    public static class ControllerBuilder {
        int startingPort;
        int numMembers;
        HashMap<Integer, Acceptor> acceptors = new HashMap<>(); // ID: members.Acceptor
        HashMap<Integer, Proposer> proposers = new HashMap<>(); // ID: members.Proposer
        public ControllerBuilder(int numMembers, int startingPort) {
            this.numMembers = numMembers;
            this.startingPort = startingPort;
        }

        public ControllerBuilder addProposer(int uuid, int delay) {
            Proposer proposer = new Proposer(uuid, startingPort++, delay, numMembers-1);
            proposers.put(proposer.getUUID(), proposer);
            acceptors.put(proposer.getUUID(), proposer);
            return this;
        }

        public ControllerBuilder addAcceptor(int uuid, int delay) {
            Acceptor acceptor = new Acceptor(uuid, startingPort++, delay);
            acceptors.put(acceptor.getUUID(), acceptor);
            return this;
        }

        public Controller build() {
            try {
                for (Acceptor acceptor : acceptors.values()) {
                    new Thread(acceptor).start();
                }

                // Connect all acceptors
                for (Acceptor acceptor : acceptors.values()) {
                    for (Proposer proposer : proposers.values()) {
                        if (acceptor != proposer) {
                            acceptor.connectToProposer(proposer.getPort());
                        }
                    }
                }
                return new Controller(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
