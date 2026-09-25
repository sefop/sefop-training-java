package integration_testing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.nio.file.Path;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration tests for the nightly planner and its results files.
 *
 * <p>Every test here touches the real file system, so every test here is an integration test. JUnit's
 * {@code @TempDir} gives each test a new, empty temporary folder, and deletes it afterwards: the real file
 * system, without leaving files behind.
 *
 * <p>The class has two parts:
 * <ul>
 *   <li>One finished test, from the book's worked example: the real {@code ResultsWriter} writes tonight's
 *       file, the real {@code ResultsReader} reads it inside {@code NightlyPlanner}, and only the notifier is a
 *       mock.</li>
 *   <li>Three tests for you to write, each marked {@code @Disabled}.</li>
 * </ul>
 *
 * <p>Test names follow {@code method_givenCondition_expectedOutcome}, so a failing test reports in plain
 * words which promise was broken.
 */
class NightlyPlannerIntegrationTest {

    // =====================================================================================================
    // Worked example: the test from the book. It joins the writer and the reader.
    // =====================================================================================================

    @Test
    void reviewTonight_givenAnInfeasibleResultOnDisk_notifiesOnce(@TempDir Path folder) {
        // Arrange
        new ResultsWriter(folder).write(new SolveResult("2026-09-26", SolveStatus.INFEASIBLE));
        Notifier notifier = mock(Notifier.class);
        NightlyPlanner planner = new NightlyPlanner(new ResultsReader(folder), notifier);

        // Act
        planner.reviewTonight("2026-09-26");

        // Assert
        verify(notifier).notify("Instance 2026-09-26: no feasible plan exists.");
    }

    // =====================================================================================================
    // Your tests: take a @TempDir Path parameter for the folder, then delete the @Disabled line.
    // =====================================================================================================

    @Test
    @Disabled("Exercise: implement me")
    void read_givenNoFileForTheInstance_throwsMissingResultException(@TempDir Path folder) {
        // An edge case only the real file system can produce: tonight's file does not exist.
    }

    @Test
    @Disabled("Exercise: implement me")
    void write_givenAnInfeasibleResult_writesTheExpectedText(@TempDir Path folder) {
        // The writer's own test: compare the file's text with the exact text you expect.
    }

    @Test
    @Disabled("Exercise: implement me")
    void read_givenAHandWrittenFile_returnsItsResult(@TempDir Path folder) {
        // The reader's own test: write the file's text yourself, without ResultsWriter.
    }
}
