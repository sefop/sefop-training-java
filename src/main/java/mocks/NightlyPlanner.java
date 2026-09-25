package mocks;

/**
 * Reviews the result of the nightly solve and notifies the planner on call when needed.
 *
 * <p>Every night a planning job solves tomorrow's plan. {@code NightlyPlanner} receives the result of that
 * solve and decides whether a person must act before morning. When one must, it notifies them through a
 * {@link Notifier}.
 *
 * <p>{@code NightlyPlanner} never calls the solver itself: the solve result arrives as an argument. Its only
 * dependency is the notifier, and it receives it from outside (dependency injection), so a test can pass in a
 * mock instead of a real notifier.
 */
public class NightlyPlanner {

    private final Notifier notifier;

    /**
     * Creates a planner that notifies through the given notifier.
     *
     * @param notifier where notifications are sent.
     */
    public NightlyPlanner(Notifier notifier) {
        this.notifier = notifier;
    }

    /**
     * Notifies the planner on call when tomorrow has no plan.
     *
     * <p>Contract:
     * <ul>
     *   <li>{@code INFEASIBLE}: sends exactly one notification, {@code "Instance <instanceId>: no feasible plan
     *       exists."}</li>
     *   <li>{@code TIME_LIMIT_NO_SOLUTION}: sends exactly one notification, {@code "Instance <instanceId>: no plan
     *       found within the time limit."}</li>
     *   <li>{@code OPTIMAL} or {@code FEASIBLE}: sends no notification. A plan exists, so nobody needs to be woken
     *       up.</li>
     * </ul>
     *
     * @param result the outcome of the nightly solve.
     */
    public void review(SolveResult result) {
        switch (result.status()) {
            case INFEASIBLE ->
                    notifier.notify("Instance " + result.instanceId() + ": no feasible plan exists.");
            case TIME_LIMIT_NO_SOLUTION ->
                    notifier.notify("Instance " + result.instanceId() + ": no plan found within the time limit.");
            case OPTIMAL, FEASIBLE -> {
                // A plan exists: nothing to do.
            }
        }
    }
}
