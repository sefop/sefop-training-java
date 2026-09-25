package integration_testing;

/** Anything that can read tonight's result: the real {@link ResultsReader}, or a stand-in in unit tests. */
public interface ResultReader {

    /**
     * Returns the saved result of the given instance.
     *
     * @param instanceId the instance to read, e.g. "2026-09-26".
     * @return the saved result.
     * @throws MissingResultException if no result was saved for {@code instanceId}.
     */
    SolveResult read(String instanceId);
}
