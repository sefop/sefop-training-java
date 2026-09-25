package integration_testing;

/** How a solve ended. Each status has a lowercase name, used in the results files. */
public enum SolveStatus {
    /** A plan was found and proven optimal. */
    OPTIMAL("optimal"),
    /** A plan was found, but not proven optimal. */
    FEASIBLE("feasible"),
    /** The model proved that no plan satisfies every constraint. */
    INFEASIBLE("infeasible"),
    /** The time limit ran out before any plan was found. */
    TIME_LIMIT_NO_SOLUTION("time_limit_no_solution");

    private final String value;

    SolveStatus(String value) {
        this.value = value;
    }

    /** @return the lowercase name of this status, e.g. {@code "infeasible"}. */
    public String value() {
        return value;
    }
}
