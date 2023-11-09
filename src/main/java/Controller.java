import members.Acceptor;
import members.LamportClock;
import members.Proposer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Future;

public class Controller {
    LamportClock clock;
    HashMap<Integer, Acceptor> acceptors; // ID - acceptor
    HashMap<Integer, Proposer> proposers; // ID - proposer

    /**
     * A Paxos Controller to stimulate voting proposals among members.
     * @param builder A Controller builder which provides proposers and acceptors
     */
    Controller(ControllerBuilder builder) {
        acceptors = builder.acceptors;
        proposers = builder.proposers;
        clock = builder.clock;
    }

    /**
     * Proposer with the user ID {@code id} proposes to all acceptors.
     * @param id User ID of the proposer
     * @return The result of the proposal. If the majority agrees, return {@code true}, otherwise return {@code false}.
     */
    public Future<Boolean> propose(int id) {
        return proposers.get(id).propose();
    }

    /**
     * Stop all proposers and acceptors from receiving messages from others.
     */
    public void stopAll() {
        for (Acceptor acceptor : acceptors.values()) {
            acceptor.stop();
        }
    }

    /**
     * Stop a certain acceptor/proposer from receiving messages from others.
     * @param id User ID of the acceptor/proposer
     */
    public void stop(int id) {
        System.out.println(id + " stops");
        acceptors.get(id).stop();
    }

    public void pause(int id, int time) {
        System.out.println(id + " pauses");
        new Thread(() -> acceptors.get(id).pause(time)).start();
    }

    public static void main(String[] args) {
        Controller controller = new DefaultController().build();
        for (String i : args) {
            controller.propose(Integer.parseInt(i));
        }
    }

    public static class DefaultController {
        Integer[] delays = {0, 300, 150, 50, 100, 150, 200, 250, 300};

        /**
         * Default controller with 9 members, in which Member 1, 2, and 3 are proposers.
         * Their response delays are set to {@code 0, 300, 150, 50, 100, 150, 200, 250, 300} respectively.
         */
        public DefaultController() {}

        /**
         * Default controller with 9 members with a specified delay time, in which Member 1, 2, and 3 are proposers.
         * @param delays delays[i] is the delay time of Member i.
         */
        public DefaultController(Integer[] delays) {
            this.delays = delays;
        }

        /**
         * Default controller with 9 members with the same delay time, in which Member 1, 2, and 3 are proposers.
         * @param delay delay time for all members
         */
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
        LamportClock clock;
        int startingPort;
        int numMembers;
        HashMap<Integer, Acceptor> acceptors = new HashMap<>(); // ID - acceptor
        HashMap<Integer, Proposer> proposers = new HashMap<>(); // ID - proposer

        /**
         * Constructor for a Controller builder. The builder can be used to
         * add proposers and acceptors via {@code addProposer} and {@code addAcceptor}.
         * Run {@code build} upon finishing adding to activate the socket in proposers
         * and acceptors.
         *
         * @param numMembers Total number of members to be added
         * @param startingPort The port of the first member
         */
        public ControllerBuilder(int numMembers, int startingPort) {
            this.numMembers = numMembers;
            this.startingPort = startingPort;
            clock = new LamportClock();
        }

        /**
         * Add a Proposer with a given user ID and a delay for response.
         * @param uuid User ID
         * @param delay response delay in milliseconds
         * @return ControllerBuilder
         */
        public ControllerBuilder addProposer(int uuid, int delay) {
            Proposer proposer = new Proposer(uuid, startingPort++, delay, numMembers-1, clock);
            proposers.put(proposer.getUUID(), proposer);
            acceptors.put(proposer.getUUID(), proposer);
            return this;
        }

        /**
         * Add an Acceptor with a given user ID and a delay for response
         * @param uuid User ID
         * @param delay response delay in milliseconds
         * @return ControllerBuilder
         */
        public ControllerBuilder addAcceptor(int uuid, int delay) {
            Acceptor acceptor = new Acceptor(uuid, startingPort++, delay);
            acceptors.put(acceptor.getUUID(), acceptor);
            return this;
        }

        /**
         * Build a {@code Controller} based on added proposers and acceptors.
         * @return {@code Controller}
         */
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
