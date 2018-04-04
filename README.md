# java-micro-fsm
A N + 1 th java micro library (where N -> 1000s) with abstractions to model a Finite State Machine

> **Warning:** Since all the event and entry/exit calls happen on the same thread cyclic state transitions and recursive transitions might blow the stack.
Refer the testcase `FSMTest::cyclicOrMutualTransitionToStatesShouldBlowTheStack` for proof :-)

Build steps:

```bash
./gradlew clean build
find . -name java-micro-fsm.jar
```

You can always copy the files and use instead of building a jar.

**Example:**

For a detailed example please refer to `FSMExample.java` in the tests.

1. First you have to define the possible events that might change the state of the FSM
```java
    private static final Event START_TASK_EVENT = Event.of("START_TASK");
    private static final Event TASK_FAILED =  Event.of("TASK_FAILED");
    private static final Event TASK_SUCCESS = Event.of("TASK_SUCCESS");
    private static final Event POST_TASK_FAILURE = Event.of("POST_TASK_FAILURE");
    private static final Event POST_TASK_SUCCESS = Event.of("POST_TASK_SUCCESS");
```
2. Then define the states with onEntry and onExit callbacks along with event handlers for specific events
 ```java
         postSuccessState = new State.Builder()
                 .withName("POST_SUCCESS")
                 .onEntry(machine -> {
                     Task task = new Task();
                     System.out.println(">>> Updating task to Success Processed <<<");
                     task.status = Status.SUCCESS_PROCESSED;
                 })
                 .create();
 
         successState = new State.Builder()
                 .withName("SUCCESS")
                 .onEntry(this::onTaskSuccess)
                 .withEventHandler(POST_TASK_SUCCESS, machine -> machine.transitionTo(postSuccessState))
                 .create();
 
         postFailureState = new State.Builder()
                 .withName("POST_FAILURE")
                 .onEntry(machine -> {
                     Task task = new Task();
                     task.status = Status.FAILURE_PROCESSED;
                 })
                 .create();
 
         failureState = new State.Builder()
                 .withName("FAILURE")
                 .onEntry(this::onTaskFailure)
                 .withEventHandler(POST_TASK_FAILURE, machine -> machine.transitionTo(postFailureState))
                 .create();
 
         inProgressTaskState = new State.Builder()
                 .withName("IN_PROGRESS")
                 .onEntry(this::executeTask)
                 .withEventHandler(TASK_FAILED, machine -> machine.transitionTo(failureState))
                 .withEventHandler(TASK_SUCCESS, machine -> machine.transitionTo(successState))
                 .create();
 
         newTaskState = new State.Builder()
                 .withName("NEW")
                 .onEntry(machine -> machine.fireEvent(START_TASK_EVENT))
                 .withEventHandler(START_TASK_EVENT, this::startTask)
                 .create();
 ```
 
 3. Create the FSM instance and set the initial state, based on the callbacks and events it will process and complete
 ```java
         this.taskFSM = new FSM();
         taskFSM.setInitialState(newTaskState);
         taskFSM.getTransitions()
                         .stream()
                         .forEach(System.out::println);
 ```
 Invoke `getTransitions()` on the fsm instance to get the history of state transitions for debugging or analysis
 
 Above code would print something like:
 
 ```
 Transition{fromState=null, toState=State{name='NEW'}}
 Transition{fromState=State{name='NEW'}, toState=State{name='IN_PROGRESS'}}
 Transition{fromState=State{name='IN_PROGRESS'}, toState=State{name='FAILURE'}}
 Transition{fromState=State{name='FAILURE'}, toState=State{name='POST_FAILURE'}}
 ```