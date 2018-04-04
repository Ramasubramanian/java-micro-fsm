package in.util.microfsm;


import java.util.Objects;

public class Event {
    public final String name;

    private Event(String name) {
        this.name = name;
    }

    public static Event of(String eventName) {
        Objects.requireNonNull(eventName, "eventName cannot be null");
        return new Event(eventName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;

        Event event = (Event) o;

        return name.equals(event.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Event{");
        sb.append("name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
