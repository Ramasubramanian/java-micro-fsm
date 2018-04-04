package in.util.microfsm;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class State {
    private String name;
    private Consumer<FSM> onEntry;
    private Consumer<FSM> onExit;
    private Map<Event, Consumer<FSM>> eventHandlers;

    private State() {}

    public String getName() {
        return name;
    }

    public Consumer<FSM> getOnEntry() {
        return onEntry;
    }

    public Consumer<FSM> getOnExit() {
        return onExit;
    }

    public Map<Event, Consumer<FSM>> getEventHandlers() {
        return eventHandlers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;

        State state = (State) o;

        return name.equals(state.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("State{");
        sb.append("name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private State state = new State();

        public Builder() {
            state.eventHandlers = new HashMap<>();
        }

        public Builder withName(String stateName) {
            state.name = stateName;
            return this;
        }

        public Builder onEntry(Consumer<FSM> onEntry) {
            state.onEntry = onEntry;
            return this;
        }

        public Builder onExit(Consumer<FSM> onExit) {
            state.onExit = onExit;
            return this;
        }

        public Builder withEventHandler(Event event, Consumer<FSM> block) {
            state.eventHandlers.put(event, block);
            return this;
        }

        public State create() {
            Objects.requireNonNull(state.name, "State name cannot be null");
            state.eventHandlers = Collections.unmodifiableMap(state.eventHandlers);
            return state;
        }
    }
}
