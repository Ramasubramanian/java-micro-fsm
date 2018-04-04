package in.util.microfsm;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assert;
import org.junit.Test;

/**
 * This example about a simple asynchronous job/task processing queue. The tasks are saved in some queue which is picked up by a worker and processed.
 * Initial state of every task is new and it transitions to In Progress once it is picked up, then success or failure depending on the execution.
 * After success or failure there is a post processing step based on the execution result which will terminate the life cycle of a job.
 * Modelling it as an FSM helps to enable crash recovery i.e. restarting a task from any of the intermediate states. Avoiding transitions to invalid states etc.
 */
public class FSMExample {

    private static boolean FAIL_TASK = false;

    private State newTaskState;
    private State inProgressTaskState;
    private State failureState;
    private State successState;
    private State postFailureState;
    private State postSuccessState;

    private FSM taskFSM;

    private static final Event START_TASK_EVENT = Event.of("START_TASK");
    private static final Event TASK_FAILED =  Event.of("TASK_FAILED");
    private static final Event TASK_SUCCESS = Event.of("TASK_SUCCESS");
    private static final Event POST_TASK_FAILURE = Event.of("POST_TASK_FAILURE");
    private static final Event POST_TASK_SUCCESS = Event.of("POST_TASK_SUCCESS");


    public enum Status {
        NEW, INPROGRESS, SUCCESS, FAILURE, SUCCESS_PROCESSED, FAILURE_PROCESSED
    }

    public static class Task {
        public Status status;

        public void execute() {
            if(FAIL_TASK) {
                throw new RuntimeException("Failure");
            }
        }
    }

    @Test
    public void fsmBasedTaskProcessingSystemShouldSuccessfullyProcessTasksForSuccessScenario() {
        initStates();
        FAIL_TASK = false;
        this.taskFSM = new FSM();
        taskFSM.setInitialState(newTaskState);
        taskFSM.getTransitions()
                .stream()
                .forEach(System.out::println);
        Assert.assertThat(taskFSM.getTransitions(), IsIterableContainingInOrder.contains(new Transition[]{
                Transition.of(null, newTaskState),
                Transition.of(newTaskState, inProgressTaskState),
                Transition.of(inProgressTaskState, successState),
                Transition.of(successState, postSuccessState)
        }));
    }

    @Test
    public void fsmBasedTaskProcessingSystemShouldSuccessfullyProcessTasksForFailureScenario() {
        initStates();
        FAIL_TASK = true;
        this.taskFSM = new FSM();
        taskFSM.setInitialState(newTaskState);
        taskFSM.getTransitions()
                .stream()
                .forEach(System.out::println);
        Assert.assertThat(taskFSM.getTransitions(), IsIterableContainingInOrder.contains(new Transition[]{
                Transition.of(null, newTaskState),
                Transition.of(newTaskState, inProgressTaskState),
                Transition.of(inProgressTaskState, failureState),
                Transition.of(failureState, postFailureState)
        }));
    }

    @Test
    public void fsmBasedTaskProcessingSystemShouldSuccessfullyResumeFromInProgressState() {
        initStates();
        this.taskFSM = new FSM();
        FAIL_TASK = false;
        taskFSM.setInitialState(inProgressTaskState);
        taskFSM.getTransitions()
                .stream()
                .forEach(System.out::println);
        Assert.assertThat(taskFSM.getTransitions(), IsIterableContainingInOrder.contains(new Transition[]{
                Transition.of(null, inProgressTaskState),
                Transition.of(inProgressTaskState, successState),
                Transition.of(successState, postSuccessState)
        }));
    }

    @Test
    public void fsmBasedTaskProcessingSystemShouldSuccessfullyProceedFromFailureState() {
        FAIL_TASK = false;
        initStates();
        this.taskFSM = new FSM();
        taskFSM.setInitialState(failureState);
        taskFSM.getTransitions()
                .stream()
                .forEach(System.out::println);
        Assert.assertThat(taskFSM.getTransitions(), IsIterableContainingInOrder.contains(new Transition[]{
                Transition.of(null, failureState),
                Transition.of(failureState, postFailureState)
        }));
    }

    private void initStates() {

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
    }


    private void startTask(FSM machine) {
        Task task = new Task();
        task.status = Status.INPROGRESS;
        System.out.println(">>> Starting task <<<");
        machine.transitionTo(inProgressTaskState);
    }

    private void executeTask(FSM machine) {
        Task task = new Task();
        try {
            System.out.println(">>> Executing task <<<");
            task.execute();
            machine.fireEvent(TASK_SUCCESS);
        } catch (Exception e) {
            System.out.println(">>> Task execution Failed <<<");
            machine.fireEvent(TASK_FAILED);
        }
    }

    private void onTaskFailure(FSM machine) {
        Task task = new Task();
        task.status = Status.FAILURE;
        System.out.println(">>> Invoking onFailure of Task <<<");
        machine.fireEvent(POST_TASK_FAILURE);
    }

    private void onTaskSuccess(FSM machine) {
        Task task = new Task();
        task.status = Status.SUCCESS;
        System.out.println(">>> Invoking onSuccess of Task <<<");
        machine.fireEvent(POST_TASK_SUCCESS);
    }

}
