package oracles;

/**
 * What {@link Optimization#run(Instance)} returns.
 *
 * @param status   what {@code run} established about the instance.
 * @param solution the load, when {@code status} is {@code OPTIMAL} or {@code FEASIBLE}; {@code null} otherwise.
 */
public record Result(Status status, Solution solution) {
}
