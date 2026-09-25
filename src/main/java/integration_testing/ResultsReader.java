package integration_testing;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

/**
 * Reads each night's solve result back from its CSV file. Owned by the planner's team.
 *
 * <p>The reader and the writer ({@link ResultsWriter}) share no code on purpose: each team encodes its own idea
 * of the file's format. Only a test that runs both of them against a real file can show that the two ideas
 * agree.
 */
public class ResultsReader implements ResultReader {

    private final Path folder;

    /**
     * Creates a reader that looks for results in {@code folder}.
     *
     * @param folder where the results files are.
     */
    public ResultsReader(Path folder) {
        this.folder = folder;
    }

    /**
     * Returns the result saved in {@code <folder>/<instanceId>.csv}.
     *
     * <p>Contract: the file holds a header line {@code instance_id,status} and one data line whose status is one
     * of the lowercase {@link SolveStatus} values, e.g. {@code 2026-09-26,infeasible}.
     *
     * @param instanceId the instance to read, e.g. "2026-09-26".
     * @return the saved result.
     * @throws MissingResultException   if no file exists for {@code instanceId}. Only a missing file becomes
     *                                  this exception; any other I/O failure is thrown as an
     *                                  {@link UncheckedIOException}.
     * @throws IllegalArgumentException if the status in the file is not a known status.
     */
    @Override
    public SolveResult read(String instanceId) {
        Path path = folder.resolve(instanceId + ".csv");
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            throw new MissingResultException("No result for instance " + instanceId + " in " + folder, e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        String[] fields = lines.get(1).split(",");
        return new SolveResult(fields[0], parseStatus(fields[1]));
    }

    private static SolveStatus parseStatus(String text) {
        for (SolveStatus status : SolveStatus.values()) {
            if (status.value().equals(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + text);
    }
}
