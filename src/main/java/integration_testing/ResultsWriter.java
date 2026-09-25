package integration_testing;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Saves each night's solve result to a CSV file, one file per instance, in a folder. Owned by the solve job's
 * team.
 *
 * <p>The writer and the reader ({@link ResultsReader}) share no code on purpose: each team encodes its own idea
 * of the file's format. Only a test that runs both of them against a real file can show that the two ideas
 * agree.
 */
public class ResultsWriter {

    private final Path folder;

    /**
     * Creates a writer that saves results in {@code folder}.
     *
     * @param folder where the results files go.
     */
    public ResultsWriter(Path folder) {
        this.folder = folder;
    }

    /**
     * Saves {@code result} to {@code <folder>/<instanceId>.csv}.
     *
     * <p>Contract: the file holds a header line and one data line, with the status in lowercase, e.g.
     * <pre>
     * instance_id,status
     * 2026-09-26,infeasible
     * </pre>
     * An existing file for the same instance is replaced.
     *
     * @param result the result to save.
     * @throws UncheckedIOException if the file cannot be written.
     */
    public void write(SolveResult result) {
        Path path = folder.resolve(result.instanceId() + ".csv");
        String status = result.status().value();
        String text = "instance_id,status\n" + result.instanceId() + "," + status + "\n";
        try {
            Files.writeString(path, text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
