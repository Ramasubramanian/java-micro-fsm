package in.util.microfsm;

import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class FSMTest {

    Event event1 = Event.of("EVENT1");
    Event event2 = Event.of("EVENT2");


    State first = new State.Builder()
            .withName("first")
            .onEntry(machine -> machine.fireEvent(event1))
            .withEventHandler(event1, this::callState2)
            .create();

    State second = new State.Builder()
            .withName("second")
            .onEntry(machine -> machine.fireEvent(event2))
            .withEventHandler(event2, this::callState1)
            .create();

    private void callState1(FSM machine) {
        machine.transitionTo(first);
    }

    private void callState2(FSM machine) {
        machine.transitionTo(second);
    }

    @Test(expected = IllegalStateException.class)
    public void fireInvalidEventShouldThrowIllegalStateException() {
        State first = new State.Builder()
                .withName("first")
                .withEventHandler(event1, machine -> System.out.println("Fired event1"))
                .create();
        FSM fsm = new FSM();
        fsm.setInitialState(first);
        fsm.fireEvent(event2);
    }

    @Test
    public void fireValidEventShouldInvokeAppropriateEventHandler() {
        StringWriter writer = new StringWriter();
        State first = new State.Builder()
                .withName("first")
                .withEventHandler(event1, machine -> writer.write("Fired event1"))
                .withEventHandler(event2, machine -> writer.write("Fired event2"))
                .create();
        FSM fsm = new FSM();
        fsm.setInitialState(first);
        fsm.fireEvent(event1);
        fsm.fireEvent(event2);
        assertEquals("Fired event1Fired event2", writer.getBuffer().toString());
    }

    @Test
    public void transitionToStateShouldCallOnEntryAndOnExitAppropriately() {
        StringWriter writer = new StringWriter();
        State first = new State.Builder()
                .withName("first")
                .onEntry(machine -> writer.write("onEntry of first state"))
                .onExit(machine -> writer.write("onExit of first state"))
                .create();
        State second = new State.Builder()
                .withName("second")
                .onEntry(machine -> writer.write("onEntry of second state"))
                .onExit(machine -> writer.write("onExit of second state"))
                .create();

        FSM fsm = new FSM();
        fsm.setInitialState(first);
        assertEquals("onEntry of first state", writer.getBuffer().toString());
        fsm.transitionTo(second);
        assertEquals("onEntry of first stateonExit of first stateonEntry of second state", writer.getBuffer().toString());
    }

    @Test(expected = java.lang.StackOverflowError.class)
    public void cyclicOrMutualTransitionToStatesShouldBlowTheStack() {
        FSM fsm = new FSM();
        fsm.setInitialState(first);
    }

}
