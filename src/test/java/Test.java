import members.Acceptor;
import members.Proposer;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Future;

public class Test {



    public static void main(String[] args) {
        try {
//            Test t = new Test();
            Controller controller = new Controller.DefaultController().build();

            Future<Boolean> propose1 = controller.propose(1);
            Future<Boolean> propose2 = controller.propose(2);

            System.out.println("Propose 1 " + propose1.get());
            System.out.println("Propose 2 " + propose2.get());

//            Thread.sleep(1000);
//            controller.stopAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
