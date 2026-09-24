package sefop.unit_tests;

/**
 * Performs arithmetic operations on floating-point numbers.
 *
 * <p>This class is stateless: it holds no data between calls. Each method receives all the values it
 * needs as arguments and returns a result. A stateless design makes objects trivially safe to share and
 * reuse, and it makes tests simple, because there is no setup state to prepare or tear down.
 *
 * <p>The operands are declared as {@code double}. This is where the Java version differs most from its
 * Python sibling: Python has to check at runtime that each operand is a number (and not a string or a
 * boolean), and has to promote integers to floats by hand. In Java the compiler does both jobs. A call
 * such as {@code add("1", 2.0)} or {@code add(true, 2.0)} does not compile, and an {@code int} argument is
 * widened to {@code double} automatically. The tests for those cases therefore have nothing left to check
 * and do not exist in this exercise.
 */
public class Calculator {

    /**
     * Returns the sum of two numbers.
     *
     * <p>Satisfies, within a relative tolerance of 1e-8:
     * <ul>
     *   <li>Identity: {@code add(x, 0) = x}</li>
     *   <li>Commutativity: {@code add(a, b) = add(b, a)}</li>
     * </ul>
     *
     * @param a first operand; must be finite (neither NaN nor infinite).
     * @param b second operand; must be finite (neither NaN nor infinite).
     * @return the arithmetic sum {@code a + b}, always a finite number.
     * @throws IllegalArgumentException if either operand is NaN or infinite.
     * @throws ArithmeticException      if the sum exceeds the range of a {@code double}. Callers get a
     *                                  clear error instead of an infinite value that would spread silently
     *                                  through later calculations.
     */
    public double add(double a, double b) {
        // NaN and ±Infinity are valid double values, so the type system lets them through.
        // We must reject them explicitly to honour the "finite in, finite out" contract.
        if (!Double.isFinite(a)) {
            throw new IllegalArgumentException("a must be finite, got " + a);
        }
        if (!Double.isFinite(b)) {
            throw new IllegalArgumentException("b must be finite, got " + b);
        }
        double result = a + b;
        // Java follows IEEE 754, just like Python: adding two sufficiently large values produces
        // Infinity instead of throwing. We detect it explicitly because returning Infinity would break
        // the contract that add always returns a finite number.
        if (Double.isInfinite(result)) {
            throw new ArithmeticException("Result of " + a + " + " + b + " exceeds double range");
        }
        return result;
    }

    /**
     * Returns the quotient of two numbers.
     *
     * <p>Satisfies, within a relative tolerance of 1e-8:
     * <ul>
     *   <li>Identity: {@code divide(a, 1) = a}</li>
     *   <li>Multiplicative inverse: {@code divide(a, a) = 1} for any non-zero {@code a}</li>
     * </ul>
     *
     * @param a dividend; must be finite (neither NaN nor infinite).
     * @param b divisor; must be finite (neither NaN nor infinite) and non-zero.
     * @return the arithmetic quotient {@code a / b}, always a finite number.
     * @throws IllegalArgumentException if either operand is NaN or infinite.
     * @throws ArithmeticException      if {@code b} is zero, or if the quotient exceeds the range of a
     *                                  {@code double}.
     */
    public double divide(double a, double b) {
        // Same reasoning as in add: NaN and ±Infinity are legal doubles, so we reject them by hand.
        if (!Double.isFinite(a)) {
            throw new IllegalArgumentException("a must be finite, got " + a);
        }
        if (!Double.isFinite(b)) {
            throw new IllegalArgumentException("b must be finite, got " + b);
        }
        // Unlike integer division, floating-point division by zero does NOT throw in Java:
        // 1.0 / 0.0 quietly returns Infinity and 0.0 / 0.0 returns NaN. We check it ourselves.
        // The check comes after the finiteness guards so a zero that reaches this line is a genuine
        // zero divisor. `b == 0.0` is also true for -0.0, which is what we want.
        if (b == 0.0) {
            throw new ArithmeticException("b must be non-zero");
        }
        double result = a / b;
        // Dividing a very large value by a very small one can overflow to Infinity without an
        // exception, so we detect it explicitly to keep the "always finite" contract.
        if (Double.isInfinite(result)) {
            throw new ArithmeticException("Result of " + a + " / " + b + " exceeds double range");
        }
        return result;
    }
}
