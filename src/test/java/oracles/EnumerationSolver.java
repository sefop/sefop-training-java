package oracles;

/**
 * A pseudo-oracle for the cargo model: a second, independent way to find the best load.
 *
 * <p>{@code EnumerationSolver} has the same contract as {@link Optimization#run(Instance)}, but no solver behind
 * it: it tries every candidate load and keeps the best. It is slow, so it only makes sense on small instances,
 * but each step can be checked by reading it. It lives with the tests because nothing in the program uses it.
 */
class EnumerationSolver {

    /**
     * Returns the load with the most revenue and the status {@code OPTIMAL}, or the status {@code INFEASIBLE}.
     *
     * <p>Same contract as {@link Optimization#run(Instance)}. Trying every candidate load is a proof, so the
     * status is {@code OPTIMAL} when a load respects both capacities and {@code INFEASIBLE} when none does, and
     * never {@code FEASIBLE} or {@code NOT_FOUND}. Product i can load at most
     * m_i = floor(min(W / w_i, V / v_i)) pallets, and at least its committed pallets, so the candidate loads are
     * every combination of counts between those two bounds.
     *
     * <p>Part 1 of the exercise: implement this method. A recursive method that fixes one product's count at a
     * time lists every combination; keep the feasible one with the most revenue.
     *
     * @throws InvalidInstanceException if {@code instance} is null.
     */
    Result run(Instance instance) {
        throw new UnsupportedOperationException("Exercise: implement me");
    }
}
