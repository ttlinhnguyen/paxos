import members.Acceptor;
import members.Proposer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Future;

public class Controller {
    HashMap<Integer, Acceptor> acceptors; // ID: members.Acceptor
    HashMap<Integer, Proposer> proposers; // ID: members.Proposer
    Controller(ControllerBuilder builder) {
        acceptors = builder.acceptors;
        proposers = builder.proposers;
    }

    public Future<Boolean> propose(int id) {
        return proposers.get(id).propose();
//        proposers.get(id).propose();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                proposers.get(id).propose();
//            }
//        }).start();
//        return null;
    }
    public void stopAll() {
        for (Acceptor acceptor : acceptors.values()) {
            acceptor.stop();
        }
    }

    public HashMap<Integer, Acceptor> getAcceptors() { return acceptors; }

    public static class DefaultController {
        Integer[] delays = {0, 300, 150, 50, 100, 150, 200, 250, 300};
        public DefaultController() {}
        public DefaultController(Integer[] delays) {
            this.delays = delays;
        }
        public DefaultController(int delay) {
            Arrays.fill(delays, delay);
        }
        public Controller build() {
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
