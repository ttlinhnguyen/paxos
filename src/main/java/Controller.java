import members.Acceptor;
import members.Proposer;

import java.io.IOException;
import java.util.HashMap;

public class Controller {
    HashMap<Integer, Acceptor> acceptors = new HashMap<>(); // ID: members.Acceptor
    HashMap<Integer, Proposer> proposers = new HashMap<>(); // ID: members.Proposer
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
            Controller.ControllerBuilder builder = new Controller.ControllerBuilder();
            for (int i=1; i<4; i++) builder.addProposer(new Proposer(i, 1000 + i, delays[i-1], 8));
            for (int i=4; i<10; i++) builder.addAcceptor(new Acceptor(i, 1000+i, delays[i-1]));

            return builder.build();
        }
    }

    public static class ControllerBuilder {
        HashMap<Integer, Acceptor> acceptors = new HashMap<>(); // ID: members.Acceptor
        HashMap<Integer, Proposer> proposers = new HashMap<>(); // ID: members.Proposer
        public ControllerBuilder() {}

        public ControllerBuilder addProposer(Proposer proposer) {
            proposers.put(proposer.getUUID(), proposer);
            acceptors.put(proposer.getUUID(), proposer);
            return this;
        }

        public ControllerBuilder addAcceptor(Acceptor acceptor) {
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
