package mocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link NightlyPlanner}.
 *
 * <p>The behavior of {@code NightlyPlanner} is a call to another system: it pages a person. It returns
 * nothing, so these tests replace the pager with a mock, a stand-in that records every call it receives, and
 * assert on those calls.
 *
 * <p>The class has two parts:
 * <ul>
 *   <li>Two finished tests, from the book's worked example. The first uses a mock written by hand, so you can
 *       see that a mock is nothing more than an object that records its calls. The second uses Mockito, the
 *       library that builds such an object for you.</li>
 *   <li>Three tests for you to write, each marked {@code @Disabled}.</li>
 * </ul>
 *
 * <p>Test names follow {@code method_givenCondition_expectedOutcome}, so a failing test reports in plain
 * words which promise was broken.
 */
class NightlyPlannerTest {

    /** A mock written by hand: it sends nothing and records every message. */
    private static class RecordingPager implements Pager {
        final List<String> messages = new ArrayList<>();

        @Override
        public void page(String message) {
            messages.add(message);
        }
    }

    // =====================================================================================================
    // Worked example: the two tests from the book.
    // =====================================================================================================

    @Test
    void review_givenAnInfeasiblePlan_pagesOnceWithTheInstance() {
        // Arrange
        RecordingPager pager = new RecordingPager();
        NightlyPlanner planner = new NightlyPlanner(pager);
        SolveResult result = new SolveResult("2026-09-26", SolveStatus.INFEASIBLE);

        // Act
        planner.review(result);

        // Assert
        assertEquals(List.of("Instance 2026-09-26: no feasible plan exists."), pager.messages);
    }

    @Test
    void review_givenAFeasiblePlan_sendsNoPage() {
        // Arrange
        Pager pager = mock(Pager.class);
        NightlyPlanner planner = new NightlyPlanner(pager);
        SolveResult result = new SolveResult("2026-09-26", SolveStatus.FEASIBLE);

        // Act
        planner.review(result);

        // Assert
        verifyNoInteractions(pager);
    }

    // =====================================================================================================
    // Your tests: use mock(Pager.class), then delete the @Disabled line.
    // =====================================================================================================

    @Test
    @Disabled("Exercise: implement me")
    void review_givenTheTimeLimitRanOut_pagesOnceWithTheTimeLimitMessage() {
    }

    @Test
    @Disabled("Exercise: implement me")
    void review_givenAnOptimalPlan_sendsNoPage() {
    }

    @Test
    @Disabled("Exercise: implement me")
    void review_givenAnotherInstance_namesThatInstanceInThePage() {
    }
}
