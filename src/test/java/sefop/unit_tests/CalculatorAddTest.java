package sefop.unit_tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Worked example: unit tests for {@link Calculator#add(double, double)}.
 *
 * <p>Read this class before writing {@code CalculatorDivideTest}. It shows the patterns the exercise
 * expects: one behaviour per test, the Arrange / Act / Assert layout, parameterized tests for properties
 * that must hold for many inputs, and assertions on the exceptions that form part of the contract.
 *
 * <p>Test names follow {@code method_givenCondition_expectedOutcome}, so a failing test reports in plain
 * words which promise was broken.
 */
class CalculatorAddTest {

    /** Relative tolerance used when comparing doubles, matching the contract in the Javadoc of add. */
    private static final double RELATIVE_TOLERANCE = 1e-8;

    @Test
    void add_givenTwoNumbers_returnsTheirSum() {
        // Arrange
        Calculator calc = new Calculator();

        // Act
        double result = calc.add(1.0, 2.0);

        // Assert
        // Doubles are compared with a tolerance, never with exact equality, because most decimal
        // numbers have no exact binary representation (0.1 + 0.2 is not exactly 0.3).
        // JUnit's delta is ABSOLUTE, so we scale it by the expected value to make it RELATIVE.
        double expected = 3.0;
        assertEquals(expected, result, Math.abs(expected) * RELATIVE_TOLERANCE);
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1.5, -3.7, 1e15})
    void add_givenZeroAsSecondOperand_returnsFirstOperand(double x) {
        // A relative tolerance matters here because the inputs span many orders of magnitude:
        // an absolute delta of 1e-8 would be far too strict for 1e15 and meaningless near 1e-10.
        // For x = 0.0 the delta becomes 0, i.e. an exact comparison. That is intended: 0.0 + 0.0
        // is exactly representable, so any difference would be a real bug.

        // Arrange
        Calculator calc = new Calculator();

        // Act
        double result = calc.add(x, 0.0);

        // Assert
        assertEquals(x, result, Math.abs(x) * RELATIVE_TOLERANCE);
    }

    @ParameterizedTest
    @CsvSource({
            "1.0,  2.0",
            "-1.5, 3.5",
            "1e10, -1e10",
            "0.0,  0.0"
    })
    void add_givenReversedOperands_returnsSameResult(double a, double b) {
        // Tested explicitly to document the commutativity contract. If add were ever reimplemented
        // with a non-commutative algorithm, this test would catch the regression.

        // Arrange
        Calculator calc = new Calculator();

        // Act
        double forward = calc.add(a, b);
        double backward = calc.add(b, a);

        // Assert
        assertEquals(forward, backward, Math.abs(forward) * RELATIVE_TOLERANCE);
    }

    @ParameterizedTest
    @CsvSource({
            "Infinity, 1.0",  // infinity as first operand
            "1.0,      NaN"   // NaN as second operand
    })
    void add_givenNonFiniteOperand_throwsIllegalArgumentException(double a, double b) {
        // JUnit converts the text "Infinity" and "NaN" into the matching double values,
        // so the special IEEE 754 values can be listed next to ordinary numbers.

        // Arrange
        Calculator calc = new Calculator();

        // Act / Assert
        // assertThrows runs the lambda and fails the test unless it throws the given exception type.
        // The lambda delays the call so that JUnit, not the test itself, catches the exception.
        assertThrows(IllegalArgumentException.class, () -> calc.add(a, b));
    }

    @Test
    void add_givenNearMaxDoubleInputs_throwsArithmeticException() {
        // We expect an exception rather than accepting a silent Infinity, because Infinity would
        // propagate invisibly through further calculations and cause hard-to-diagnose errors downstream.
        // Double.MAX_VALUE is the largest finite double (~1.8 x 10^308); adding it to itself overflows.

        // Arrange
        Calculator calc = new Calculator();

        // Act / Assert
        assertThrows(ArithmeticException.class, () -> calc.add(Double.MAX_VALUE, Double.MAX_VALUE));
    }
}
