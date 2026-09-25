package test_driven_development;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LinearExpression}, written test-first.
 *
 * <p>The three tests below are the three cycles of the book's worked example, in the order they were
 * written. Each one was red before the code that makes it pass existed. Continue from here: add one test for
 * the next requirement in the Javadoc of {@code LinearExpression}, watch it fail, then make it pass.
 *
 * <p>Test names follow {@code method_givenCondition_expectedOutcome}, so a failing test reports in plain
 * words which promise was broken.
 */
class LinearExpressionTest {

    /** Tolerance used when comparing doubles. */
    private static final double TOLERANCE = 1e-8;

    // =====================================================================================================
    // The scalar part: the book's worked example, one test per cycle.
    // =====================================================================================================

    @Test
    void linearExpression_givenNoArguments_hasScalarZero() {
        // Cycle 1. Red: LinearExpression did not exist yet.

        // Arrange
        // Nothing to prepare: the constructor itself is the unit under test.

        // Act
        LinearExpression expression = new LinearExpression();

        // Assert
        assertEquals(0.0, expression.scalar(), TOLERANCE);
    }

    @Test
    void linearExpression_givenAScalar_hasThatScalar() {
        // Cycle 2. Red: scalar() returned the constant 0.0.

        // Arrange
        double scalar = 3.0;

        // Act
        LinearExpression expression = new LinearExpression(scalar);

        // Assert
        assertEquals(scalar, expression.scalar(), TOLERANCE);
    }

    @Test
    void addScalar_givenAValue_increasesTheScalarByIt() {
        // Cycle 3. Red: addScalar did not exist yet.

        // Arrange
        LinearExpression expression = new LinearExpression(1.0);

        // Act
        expression.addScalar(3.0);

        // Assert
        assertEquals(4.0, expression.scalar(), TOLERANCE);
    }

    // =====================================================================================================
    // The variables: your cycles start here.
    // =====================================================================================================
}
