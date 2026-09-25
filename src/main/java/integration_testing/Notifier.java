package integration_testing;

/**
 * Sends a short message to the planner on call.
 *
 * <p>The real implementation sends an SMS or a chat message to a person. Tests never use it: they pass in a
 * mock that records the calls instead.
 */
public interface Notifier {

    /**
     * Sends {@code message} to the planner on call.
     *
     * @param message the text of the notification.
     */
    void notify(String message);
}
