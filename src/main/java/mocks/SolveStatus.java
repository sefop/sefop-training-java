package mocks;

/** How a solve ended. */
public enum SolveStatus {
    /** A plan was found and proven optimal. */
    OPTIMAL,
    /** A plan was found, but not proven optimal. */
    FEASIBLE,
    /** The model proved that no plan satisfies every constraint. */
    INFEASIBLE,
    /** The time limit ran out before any plan was found. */
    TIME_LIMIT_NO_SOLUTION
}
