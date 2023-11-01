import members.Acceptor;
import members.Proposer;

import java.io.IOException;
import java.util.ArrayList;

public class Test {
    private void create3Proposers(CentralisedController controller, Integer[] delays) throws IOException {
        for (int i=1; i<4; i++) {
            controller.addProposer(new Proposer(i, 1000+i, delays[i-1],8));
        }
    }

    private void create6Acceptors(CentralisedController controller, Integer[] delays) throws IOException {
        for (int i=4; i<10; i++) {
            controller.addAcceptor(new Acceptor(i, 1000+i, delays[i-4]));
        }
    }
    public static void main(String[] args) {
        try {
            Test t = new Test();
            CentralisedController controller = new CentralisedController();

            Integer[] proposerDelays = {0, 400, 200};
            Integer[] acceptorDelays = {0, 50, 100, 150, 200, 250, 300, 350, 400};

            t.create3Proposers(controller, proposerDelays);
            t.create6Acceptors(controller, acceptorDelays);
            controller.propose(1);
            controller.propose(2);

            Thread.sleep(1000);
            controller.stopAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
