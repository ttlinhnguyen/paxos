import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Test {
    int TIMEOUT = 30000;
    private Boolean onePropose() {
        try {
            Controller controller = new Controller.DefaultController().build();
            Future<Boolean> propose1 = controller.propose(1);
            boolean res = propose1.get(TIMEOUT, TimeUnit.MILLISECONDS);
            controller.stopAll();
            return res;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private Boolean concurrentProposes() {
        try {
            Controller controller = new Controller.DefaultController().build();
            Future<Boolean> propose1 = controller.propose(1);
            Future<Boolean> propose2 = controller.propose(2);

            boolean res = propose1.get(TIMEOUT, TimeUnit.MILLISECONDS) != propose2.get(TIMEOUT, TimeUnit.MILLISECONDS);
            controller.stopAll();
            return res;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private Boolean immediateResponse() {
        try {
            Controller controller = new Controller.DefaultController(0).build();
            Future<Boolean> propose1 = controller.propose(1);
            Future<Boolean> propose2 = controller.propose(2);

            boolean res = propose1.get(TIMEOUT, TimeUnit.MILLISECONDS) != propose2.get(TIMEOUT, TimeUnit.MILLISECONDS);
            controller.stopAll();
            return res;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private Boolean proposerCrash() {
        try {
            Controller controller = new Controller.DefaultController().build();
            Future<Boolean> propose2 = controller.propose(2);
            Thread.sleep(100);
            controller.stop(2);
            Thread.sleep(1000);
            Future<Boolean> propose1 = controller.propose(1);

            boolean res = propose1.get(TIMEOUT, TimeUnit.MILLISECONDS) && !propose2.get(TIMEOUT, TimeUnit.MILLISECONDS);
            controller.stopAll();
            return res;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private Boolean twoProposersOffline() {
        try {
            Controller controller = new Controller.DefaultController().build();
            Future<Boolean> propose2 = controller.propose(2);
            Future<Boolean> propose1 = controller.propose(1);
            Future<Boolean> propose3 = controller.propose(3);

            Thread.sleep(100);
            controller.pause(2, 2000);
            controller.pause(3, 3000);

            boolean res = propose1.get(TIMEOUT, TimeUnit.MILLISECONDS);
            controller.stopAll();
            return res;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static void main(String[] args) {
        Test t = new Test();
//        new TestBuilder()
//                .setName("Only Member 1 proposes.")
//                .setExpected("All members vote for Member 1")
//                .setTest(t::onePropose)
//                .run();
//        new TestBuilder()
//                .setName("Member 1 and 2 propose simultaneously.")
//                .setExpected("All members vote for either Member 1 and 2 based on the ID of their proposals.")
//                .setTest(t::concurrentProposes)
//                .run();
//        new TestBuilder()
//                .setName("All members have no delays in response time. Member 1 and 2 propose simultaneously.")
//                .setExpected("All members vote for either Member 1 and 2 based on the ID of their proposals.")
//                .setTest(t::immediateResponse)
//                .run();
//        new TestBuilder()
//                .setName("Member 2 proposes but then crashes after 100ms. Member 1 proposes after 1s.")
//                .setExpected("All members vote for Member 1")
//                .setTest(t::proposerCrash)
//                .run();
        new TestBuilder()
                .setName("Member 1, 2, and 3 proposes simultaneously. After 100ms, Member 2 goes offline for 2s, " +
                        "and Member 3 goes offline for 3 seconds.")
                .setExpected("All members vote for Member 1")
                .setTest(t::twoProposersOffline)
                .run();



    }

    public static class TestBuilder {
        String name;
        String expected;
        Callable<Boolean> test;
        public TestBuilder setName(String name) {
            this.name = name;
            return this;
        }
        public TestBuilder setExpected(String expected) {
            this.expected = expected;
            return this;
        }
        public TestBuilder setTest(Callable<Boolean> test) {
            this.test = test;
            return this;
        }
        public void run() {
            System.out.println("==========================");
            System.out.println("TEST: " + name);
            System.out.println("EXPECTED: " + expected);
            System.out.println();
            try {
                if (test.call()) {
                    System.out.println("TEST PASSED!");
                } else System.out.println("TEST FAILED!");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
