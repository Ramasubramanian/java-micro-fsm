package in.util.microfsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class FSM {
    private State currentState;
    private List<Transition> transitions = new ArrayList<>();

    public void setInitialState(State initialState) {
        transitionTo(initialState);
    }

    public void transitionTo(State newState) {
        Objects.requireNonNull(newState, "newState cannot be null");
        if(currentState != null) {
            Consumer<FSM> onExit = currentState.getOnExit();
            if(onExit != null) {
                onExit.accept(this);
            }
        }
        transitions.add(Transition.of(currentState, newState));
        currentState = newState;
        Consumer<FSM> onEntry = currentState.getOnEntry();
        if(onEntry != null) {
            onEntry.accept(this);
        }
    }

    public void fireEvent(Event event) {
        if(currentState == null) {
            throw new IllegalStateException("FSM is not in any valid state to accept events, did you forget to set an initial state?");
        }
        Consumer<FSM> eventHandler = currentState.getEventHandlers().get(event);
        if(eventHandler == null) {
            throw new IllegalStateException(String.format("There is no valid event handler for event type: %s in state: %s. Try adding a handler!", event, currentState));
        } else {
            eventHandler.accept(this);
        }
    }

    public State getCurrentState() {
        return currentState;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

}
