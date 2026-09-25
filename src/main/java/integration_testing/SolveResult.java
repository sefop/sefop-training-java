package integration_testing;

/**
 * The outcome of one night's solve.
 *
 * @param instanceId the name of the instance that was solved, e.g. "2026-09-26".
 * @param status     how the solve ended.
 */
public record SolveResult(String instanceId, SolveStatus status) {
}
