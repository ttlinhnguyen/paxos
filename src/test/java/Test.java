import members.Acceptor;
import members.Proposer;

import java.io.IOException;

public class Test {
    public static void main(String[] args) {
        try {
//            Test t = new Test();
            Controller controller = new Controller.DefaultController().build();

            controller.propose(1);
            controller.propose(2);

            Thread.sleep(1000);
            controller.stopAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
