package mocks;

/**
 * Reviews the result of the nightly solve and pages the planner on call when needed.
 *
 * <p>Every night a planning job solves tomorrow's plan. {@code NightlyPlanner} receives the result of that
 * solve and decides whether a person must act before morning. When one must, it pages them through a
 * {@link Pager}.
 *
 * <p>{@code NightlyPlanner} never calls the solver itself: the solve result arrives as an argument. Its only
 * dependency is the pager, and it receives it from outside (dependency injection), so a test can pass in a
 * mock instead of a real pager.
 */
public class NightlyPlanner {

    private final Pager pager;

    /**
     * Creates a planner that pages through the given pager.
     *
     * @param pager where pages are sent.
     */
    public NightlyPlanner(Pager pager) {
        this.pager = pager;
    }

    /**
     * Pages the planner on call when tomorrow has no plan.
     *
     * <p>Contract:
     * <ul>
     *   <li>{@code INFEASIBLE}: sends exactly one page, {@code "Instance <instanceId>: no feasible plan
     *       exists."}</li>
     *   <li>{@code TIME_LIMIT_NO_SOLUTION}: sends exactly one page, {@code "Instance <instanceId>: no plan
     *       found within the time limit."}</li>
     *   <li>{@code OPTIMAL} or {@code FEASIBLE}: sends no page. A plan exists, so nobody needs to be woken
     *       up.</li>
     * </ul>
     *
     * @param result the outcome of the nightly solve.
     */
    public void review(SolveResult result) {
        switch (result.status()) {
            case INFEASIBLE ->
                    pager.page("Instance " + result.instanceId() + ": no feasible plan exists.");
            case TIME_LIMIT_NO_SOLUTION ->
                    pager.page("Instance " + result.instanceId() + ": no plan found within the time limit.");
            case OPTIMAL, FEASIBLE -> {
                // A plan exists: nothing to do.
            }
        }
    }
}
