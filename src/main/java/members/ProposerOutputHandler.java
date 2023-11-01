package members;

import messages.*;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ProposerOutputHandler implements Runnable {
    int TIMEOUT = 10*1000;
    Proposer proposer;
    int proposalId;
    int proposalValue;
    public ProposerOutputHandler(Proposer proposer) {
        this.proposer = proposer;
    }

    @Override
    public void run() {
        prepare();
    }

    private void getNewProposalId() {
        Proposer.clock.increment();
        proposalId = Proposer.clock.get();
        proposer.promised.put(proposalId, new ArrayList<>());
        proposer.accepted.put(proposalId, new ArrayList<>());
//        proposer.promisedOutStream.put(proposalId, new ArrayList<>());
    }

    private void prepare() {
        try {
            Thread.sleep(proposer.delay);
            getNewProposalId();
            Message request = new Prepare(proposalId);
            System.out.println(proposer.UUID + " PREPARE " + proposalId);
            for (ObjectOutputStream outputStream : proposer.acceptorsOutStream) {
                outputStream.writeObject(request);
//                outputStream.flush();
            }
            handlePromise();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendAccept() throws IOException, InterruptedException {
        Thread.sleep(proposer.delay);
        Message request = new SendAccept(proposalId, proposalValue);
//        request.value = proposalValue;
        System.out.println(proposer.UUID + " SEND_ACCEPT " + proposalId);

        for (ObjectOutputStream outputStream : proposer.acceptorsOutStream) {
            outputStream.writeObject(request);
//            outputStream.flush();
        }
        handleAccept();
    }

    private void handlePromise() {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < TIMEOUT) {
            try {
                if (proposer.promised.get(proposalId).size() >= proposer.numAcceptors/2) {
                    startTime = System.currentTimeMillis();
                    int highestAcceptedId = -1;
                    proposalValue = proposer.UUID;

                    for (Promise m : proposer.promised.get(proposalId)) {
                        if (m.acceptedId > highestAcceptedId) {
                            highestAcceptedId = m.acceptedId;
                            proposalValue = m.acceptedValue;
                        }
                    }
                    sendAccept();
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        prepare();
    }

    private void handleAccept() {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < TIMEOUT) {
            if (proposer.accepted.get(proposalId).size() >= proposer.numAcceptors/2) {
                startTime = System.currentTimeMillis();
                // majority has accepted
                System.out.println("Majority has accepted Proposal " + proposalId + ": " + proposalValue +
                        " from Member " + proposer.UUID);
                return;
            }
        }

    }
}
