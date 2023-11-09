# Paxos Consensus Protocol
The Paxos consensus protocol has a set of members (Acceptors), some of which are
also Proposers. Proposers will send out voting proposal to other members, and each
member will try to vote fairly. 
 
The protocol can be simulated by using `Controller`, which controls some activities
of the Proposers and Acceptors such as propose, pause, and stop. A `Controller` can be
initialized by utilizing either a `ControllerBuilder`, which allows to add Proposers and Acceptors
manually, or a `DefaultController`, which creates 9 members and 3 of them are Proposers.

## Controller
### Initialization
There are two ways to initialize a `Controller`: `ControllerBuilder` and `DefaultController`.
It is recommended to use `DefaultController`.
#### ControllerBuilder
A `ControllerBuilder` asks for a fixed number of members and a starting port number.
After adding Proposers and Acceptors with a specified ID and delay time, run `build()` to 
retrieve the `Controller`.

Example:
```java
// Create a Controller with 1 Proposer and 2 Acceptors
Controller controller = new ControllerBuilder(3, 1000) // 3 members and starting port is 1000
                                .addProposer(1, 1000) // Proposer with ID 1 and delay time of 1000ms
                                .addAcceptor(2, 100) // Acceptor with ID 2 and delay time of 100ms
                                .addAcceptor(3, 200) // Acceptor with ID 3 and delay time of 200ms
                                .build();                               
```
#### DefaultController
The `DefaultController` has three Proposers with ID of 1,2, and 3, and six Acceptors with ID
from 5 to 9. Run `build()` to retrieve the `Controller`.

Example:
```java
// Create a Controller with 3 Proposers and 6 Acceptors with
// default delay time: {0, 300, 150, 50, 100, 150, 200, 250, 300}
Controller controller = new DefaultController().build();

// Create a Default Controller with custom delay time for each member
Controller controller = new DefaultController({0,1,2,3,4,5,6,7,8}).build();

// Create a Default Controller with the same delay time (e.g. 1000) for all members
Controller controller = new DefaultController(1000).build();
```
### Usage
* `propose(id)`: The Proposer with ID of `id` sends the proposal to other members.
* `stop(id)`: The member with ID of `id` stops handling and receiving messages.
* `stopAll()`: All members stop handling and receiving messages.
* `pause(id, time)`: The member with ID of `id` pauses handling messages for a period of specified time.

Example:
```java
Controller controller = new Controller.DefaultController().build();

// Member 1 and 2 propose
Future<Boolean> propose1 = controller.propose(1);
Future<Boolean> propose2 = controller.propose(2);

// Member 2 stops
controller.stop(2);

// Print out the result of each proposal
System.out.println(propose1.get(TIMEOUT, TimeUnit.MILLISECONDS));
System.out.println(propose2.get(TIMEOUT, TimeUnit.MILLISECONDS));

// All members stop
controller.stopAll();
```

## Testing
The integration testing is used to test different scenarios of members interacting
with others, including:
* One member sends the proposal.
* Two members sends the proposal simultaneously.
* All members have no delay in response.
* The Proposer crashes after sending the proposal.
* Three members send the proposals, then two of them go offline.

The test also prints the message log of the Proposers and Acceptors in the format of:
`<UUID> <MessageType> ID: <id> VALUE: <value>`

## How to run
```shell
# Compile all files
make comile

# Run testing
make tests
```

## More in depth
The `Controller` has a set of Acceptors and Proposers, which are made from class
`Acceptor` and `Proposer`. 

The Acceptors and Proposers communicate with each other
via socket using the `Message` object. There are five types of message - 
Proposers send PREPARE, REQUEST_ACCEPT, and DECIDE messages, while Acceptors
send PROMISE and ACCEPT messages.
### Acceptor
When the Acceptor is asked to connect to a Proposer, it creates a thread
running `AcceptorInputHandler`, which listens to messages from the specified
Proposer. If it's connected to 3 Proposers, there'll be 3 `AcceptorInputHandler` threads.

It sends out PROMISE message as a response to the PREPARE message
if the Acceptor promises the proposed ID, and sends out ACCEPT message as a response
to the REQUEST_ACCEPT message if it accepts the proposed ID. When it receives
a DECIDE message, update the accepted ID and value arbitrarily, and the Acceptor
now officially vote for the accepted value.

### Proposer
`Proposer` is a deprived class of `Acceptor`; therefore, it can vote for other
Proposers as described in the Acceptor role. For its Proposer role, it has a
`ProposerInputHandler` thread for each Acceptor connecting to it, and a 
`ProposerOutputHandler` thread to send messages to all or most of the Acceptors.
If there're 5 Acceptors, there'll be 5 `ProposerInputHandler` threads and 1 `ProposerOutputhanlder` thread.

The `ProposerInputHandler` will push the message from the Acceptor to an array of messages
of the same type mapping to the proposal ID.

The `ProposerOutputHandler` on the other hand, will send PREPARE messages to all Acceptors upon the
thread is started. It retrieves the map as processed in `ProposerInputHandler`
to count the majority of the messages responding to the proposal ID.
If the majority promise, continue by sending REQUEST_ACCEPT to the promised
Acceptors. Once the majority accept, send DECIDE messages to all.

