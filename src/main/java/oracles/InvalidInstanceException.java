package oracles;

/** Thrown by {@link Optimization#run(Instance)} when the instance breaks what the contract requires of it. */
public class InvalidInstanceException extends IllegalArgumentException {

    public InvalidInstanceException(String message) {
        super(message);
    }
}
