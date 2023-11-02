package members;

import messages.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;

class ProposerOutputHandler implements Callable<Boolean> {
    int TIMEOUT = 10*1000;
    Proposer proposer;
    int proposalId;
    int proposalValue;
    public ProposerOutputHandler(Proposer proposer) {
        this.proposer = proposer;
    }

    @Override
    public Boolean call() {
        return prepare();
    }

    private void getNewProposalId() {
        Proposer.clock.increment();
        proposalId = Proposer.clock.get();
        proposer.promised.put(proposalId, new ArrayList<>());
        proposer.accepted.put(proposalId, new ArrayList<>());
        proposer.promisedOutStream.put(proposalId, new ArrayList<>());
    }

    private boolean prepare() {
        if (!proposer.decided) {
            try {
//                Thread.sleep(proposer.delay);
                getNewProposalId();
                Message request = new Prepare(proposalId);
                System.out.println(proposer.UUID + " PREPARE " + proposalId);

                for (ObjectOutputStream outputStream : proposer.acceptorsOutStream) {
                    outputStream.writeObject(request);
                }
                return handlePromise();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean requestAccept() throws IOException, InterruptedException {
        if (!proposer.decided) {
            Thread.sleep(proposer.delay);
            Message request = new RequestAccept(proposalId, proposalValue);
            System.out.println(proposer.UUID + " SEND_ACCEPT " + proposalId + " " + proposalValue);

            for (ObjectOutputStream outputStream : proposer.acceptorsOutStream) {
                outputStream.writeObject(request);
            }
            return handleAccept();
        }
        return false;
    }

    private boolean decide() throws InterruptedException, IOException {
        if (!proposer.decided) {
            Thread.sleep(proposer.delay);
            Message request = new Decide(proposalId, proposalValue);
            System.out.println(proposer.UUID + " DECIDE " + proposalId + " " + proposalValue);
            proposer.decide(proposalId, proposalValue);
            for (ObjectOutputStream outputStream : proposer.acceptorsOutStream) {
                outputStream.writeObject(request);
            }
            return true;
        }
        return false;
    }

    private boolean handlePromise() {
        // promise to self
        int majorityTarget =  proposer.promise(proposalId) ? proposer.numAcceptors/2 - 1 : proposer.numAcceptors/2;

        long startTime = System.currentTimeMillis();
        while (notTimeout(startTime)) {
            try {
                if (proposer.decided) return false;
                if (proposer.promised.get(proposalId).size() >= majorityTarget) {
                    startTime = System.currentTimeMillis();
                    int highestAcceptedId = -1;
                    proposalValue = proposer.UUID;

                    for (Promise m : proposer.promised.get(proposalId)) {
                        if (m.acceptedId > highestAcceptedId) {
                            highestAcceptedId = m.acceptedId;
                            proposalValue = m.acceptedValue;
                        }
                    }
                    return requestAccept();

                }
            } catch (Exception e) {}
        }
        return prepare();
    }

    private boolean handleAccept() {
        // accept to self
        int majorityTarget = proposer.accept(proposalId, proposalValue) ? proposer.numAcceptors/2 - 1 : proposer.numAcceptors/2;

        long startTime = System.currentTimeMillis();
        while (notTimeout(startTime)) {
            try {
                if (proposer.decided) return false;
                if (proposer.accepted.get(proposalId).size() >= majorityTarget) {
                    startTime = System.currentTimeMillis();
                    // majority has accepted
                    System.out.println("Majority has accepted Proposal " + proposalId + ": " + proposalValue +
                            " from Member " + proposer.UUID);
                    return decide();
                }
            } catch (Exception e) {}
        }
        return false;
    }

    private boolean notTimeout(long startTime) {
        return System.currentTimeMillis() - startTime < TIMEOUT;
    }
}
