package in.util.microfsm;


public class Transition {
    public final State fromState;
    public final State toState;

    private Transition(State fromState, State toState) {
        this.fromState = fromState;
        this.toState = toState;
    }

    public static Transition of(State fromState, State toState) {
        return new Transition(fromState, toState);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Transition{");
        sb.append("fromState=").append(fromState);
        sb.append(", toState=").append(toState);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transition)) return false;

        Transition that = (Transition) o;

        if (fromState != null ? !fromState.equals(that.fromState) : that.fromState != null) return false;
        return toState != null ? toState.equals(that.toState) : that.toState == null;

    }

    @Override
    public int hashCode() {
        int result = fromState != null ? fromState.hashCode() : 0;
        result = 31 * result + (toState != null ? toState.hashCode() : 0);
        return result;
    }
}
