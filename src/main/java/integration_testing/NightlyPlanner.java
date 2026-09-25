package integration_testing;

/**
 * Reads tonight's result and notifies the planner on call when needed.
 *
 * <p>Every night, a solve job saves the result of tomorrow's plan to a file ({@link ResultsWriter}).
 * {@code NightlyPlanner} reads that file back through a {@link ResultReader} and, when a person must act before
 * morning, notifies them through a {@link Notifier}.
 *
 * <p>Both dependencies are received from outside (dependency injection). In an integration test the reader stays
 * real, because the results files are used only by this program (a managed dependency); the notifier is replaced
 * by a mock, because people observe it (an unmanaged dependency).
 */
public class NightlyPlanner {

    private final ResultReader reader;
    private final Notifier notifier;

    /**
     * Creates a planner that reads results with {@code reader} and notifies through {@code notifier}.
     *
     * @param reader   where tonight's result is read from.
     * @param notifier where notifications are sent.
     */
    public NightlyPlanner(ResultReader reader, Notifier notifier) {
        this.reader = reader;
        this.notifier = notifier;
    }

    /**
     * Reads the result of {@code instanceId} and notifies the planner on call when tomorrow has no plan.
     *
     * <p>Contract:
     * <ul>
     *   <li>{@code INFEASIBLE}: sends exactly one notification, {@code "Instance <instanceId>: no feasible plan
     *       exists."}</li>
     *   <li>{@code TIME_LIMIT_NO_SOLUTION}: sends exactly one notification, {@code "Instance <instanceId>: no plan
     *       found within the time limit."}</li>
     *   <li>{@code OPTIMAL} or {@code FEASIBLE}: sends no notification.</li>
     *   <li>No result saved for {@code instanceId}: the reader's {@link MissingResultException} propagates, and no
     *       notification is sent.</li>
     * </ul>
     *
     * @param instanceId the instance solved tonight, e.g. "2026-09-26".
     */
    public void reviewTonight(String instanceId) {
        SolveResult result = reader.read(instanceId);
        switch (result.status()) {
            case INFEASIBLE -> notifier.notify("Instance " + instanceId + ": no feasible plan exists.");
            case TIME_LIMIT_NO_SOLUTION ->
                    notifier.notify("Instance " + instanceId + ": no plan found within the time limit.");
            case OPTIMAL, FEASIBLE -> {
                // A plan exists: nothing to do.
            }
        }
    }
}
