package members;

import messages.*;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Callable;

class ProposerOutputHandler implements Callable<Boolean> {
    int TIMEOUT = 5*1000;
    Proposer proposer;
    int proposalId;
    int proposalValue;
    public ProposerOutputHandler(Proposer proposer) {
        this.proposer = proposer;
    }

    /**
     * Return the result of the proposal
     * @return {@code true} if proposed successfully
     */
    @Override
    public Boolean call() {
        try {
            return prepare();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a new proposal ID that are distinct from any other proposal IDs that have been used by other Proposers
     */
    private void getNewProposalId() {
        proposer.clock.increment();
        proposalId = proposer.clock.get();
        proposer.promisedMap.put(proposalId, new ArrayList<>());
        proposer.acceptedMap.put(proposalId, new ArrayList<>());
        proposer.promisedSockets.put(proposalId, new ArrayList<>());
    }

    /**
     * Sends a PREPARE messages to other members if itself hasn't accepted any other value.
     * @return {@code true} if proposed successfully.
     */
    private boolean prepare() throws Exception {
        if (!proposer.accepted && proposer.running) {
            getNewProposalId();
            Message request = new Prepare(proposalId);
            proposer.debug(proposer.UUID + " PREPARE ID: " + proposalId);

            for (ObjectOutputStream outputStream : proposer.acceptorsOutStream.values()) {
                outputStream.writeObject(request);
            }
            return handlePromise();
        }
        return false;
    }

    /**
     * Sends a REQUEST_ACCEPT message to member that has promised if itself hasn't accepted any other value
     * @return {@code true} if proposed successfully.
     */
    private boolean requestAccept() throws Exception {
        if (!proposer.accepted && proposer.running) {
            Thread.sleep(proposer.delay);
            Message request = new RequestAccept(proposalId, proposalValue);
            proposer.debug(proposer.UUID + " REQUEST_ACCEPT ID: " + proposalId + " VALUE: " + proposalValue);

            try {
                proposer.lock.acquire();
                for (Socket socket : proposer.promisedSockets.get(proposalId)) {
                    ObjectOutputStream outputStream = proposer.acceptorsOutStream.get(socket);
                    outputStream.writeObject(request);
                }
                proposer.lock.release();
            } catch (InterruptedException e) {}
            return handleAccept();
        }
        return false;
    }

    /**
     * Sends a DECIDE message to all members
     * @return {@code true} if proposed successfully.
     */
    private boolean decide() throws Exception {
        if (proposer.running) {
            Thread.sleep(proposer.delay);
            Message request = new Decide(proposalId, proposalValue);
            proposer.debug(proposer.UUID + " DECIDE ID: " + proposalId + " VALUE: " + proposalValue);
            proposer.decide(proposalId, proposalValue);
            for (ObjectOutputStream outputStream : proposer.acceptorsOutStream.values()) {
                outputStream.writeObject(request);
            }
            return true;
        }
        return false;
    }

    /**
     * If the majority promises, go to the REQUEST_ACCEPT stage. If there is not enough promise, timeout after 5s
     * and send a new proposal (PREPARE stage).
     * @return {@code true} if proposed successfully.
     */
    private boolean handlePromise() throws Exception {
        // promise to self
        int majorityTarget =  proposer.promise(proposalId) ? proposer.numAcceptors/2 : proposer.numAcceptors/2+1;

        long startTime = System.currentTimeMillis();
        while (notTimeout(startTime) && proposer.running) {
            if (proposer.accepted) return false;
            if (proposer.promisedMap.get(proposalId).size() >= majorityTarget) {
                int highestAcceptedId = -1;
                proposalValue = proposer.UUID;

                for (Promise m : proposer.promisedMap.get(proposalId)) {
                    if (m.acceptedId > highestAcceptedId) {
                        highestAcceptedId = m.acceptedId;
                        proposalValue = m.acceptedValue;
                    }
                }
                return requestAccept();
            }
        }
        return prepare();
    }

    /**
     * If the majority accepts, go to the DECIDE stage. Otherise, timeout after 5s and sends a new proposal (PREPARE stage)
     * @return {@code true} if proposed successfully.
     */
    private boolean handleAccept() throws Exception {
        // accept to self
        int majorityTarget = proposer.accept(proposalId, proposalValue) ? proposer.numAcceptors/2 : proposer.numAcceptors/2+1;

        long startTime = System.currentTimeMillis();
        while (notTimeout(startTime) && proposer.running) {
            if (proposer.acceptedMap.get(proposalId).size() >= majorityTarget) {
                return decide();
            }
        }
        return prepare();
    }

    private boolean notTimeout(long startTime) {
        return System.currentTimeMillis() - startTime < TIMEOUT;
    }
}
