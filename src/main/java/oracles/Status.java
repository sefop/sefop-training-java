package oracles;

/**
 * What {@link Optimization#run(Instance)} established about an instance.
 *
 * <p>{@code OPTIMAL} and {@code INFEASIBLE} are facts about the instance: they hold whichever algorithm runs
 * and however long it takes. {@code FEASIBLE} and {@code NOT_FOUND} are facts about the search: more time or
 * another algorithm could turn the first into {@code OPTIMAL} and the second into any other status.
 */
public enum Status {
    /** The solution is a feasible load, and no feasible load earns more. */
    OPTIMAL,
    /** The solution is a feasible load; a better one may exist. */
    FEASIBLE,
    /** No feasible load exists for this instance. */
    INFEASIBLE,
    /** The search stopped with no load and no proof that none exists. */
    NOT_FOUND
}
