package test_driven_development;

/**
 * Models a linear expression of the form {@code a0 + a1*x1 + a2*x2 + ... + an*xn}, where {@code a0} is a
 * scalar, {@code a1, a2, ..., an} are non-zero coefficients, and {@code x1..xn} are non-empty variable names.
 *
 * <p>Types: the scalar and the coefficients are {@code double}s; each variable is identified by its name, a
 * {@code String}.
 *
 * <p>Examples:
 * <ul>
 *   <li>{@code 0}: scalar 0.0, no variables.</li>
 *   <li>{@code 1 + 2x}: scalar 1.0, coefficient 2.0 for "x".</li>
 *   <li>{@code -1 - x + 2y}: scalar -1.0, coefficient -1.0 for "x", coefficient 2.0 for "y".</li>
 * </ul>
 *
 * <p><b>Test-driven development exercise.</b> See README.md in this folder for the instructions. The
 * scalar part is already built, test-first, exactly as in the book's worked example. Everything about
 * variables is yours to build, one red-green-refactor cycle at a time. For the new methods, names,
 * parameters and internal design are up to you: only the observable behavior described below is required.
 *
 * <h2>Construction</h2>
 * The expression must be constructible in each of these forms:
 * <ul>
 *   <li>Empty: no scalar and no terms given. The scalar defaults to 0.0 and there are no variables.
 *       Example: a freshly constructed empty expression has scalar 0.0 and an empty set of variables.</li>
 *   <li>Scalar only: constructed with just a scalar value. Example: constructed with scalar 3.0, the
 *       scalar is 3.0 and the set of variables is empty.</li>
 *   <li>Single term only: constructed with a coefficient and a variable name. Example: constructed with
 *       coefficient 2.0 for variable "x1", the scalar is 0.0 and the coefficient for "x1" is 2.0.</li>
 *   <li>Scalar and a single term together: constructed with a scalar, coefficient, and variable name.
 *       Example: constructed with scalar 1.0 and coefficient 2.0 for variable "x1", the scalar is 1.0 and
 *       the coefficient for "x1" is 2.0.</li>
 * </ul>
 *
 * <h2>Modifying operations</h2>
 * <ul>
 *   <li>Add a scalar value to the expression. This changes the expression's own scalar by adding the given
 *       amount to it. Example: an expression with scalar 1.0, after adding scalar 3.0, has scalar 4.0.</li>
 *   <li>Add a term (a coefficient for a variable) to the expression. If the variable is not yet present, it
 *       is added with the given coefficient. If the variable is already present, the given coefficient is
 *       added to (accumulated with) its existing coefficient rather than replacing it.
 *       Example: an expression with an existing coefficient of 3.0 for "x1", after adding term (2.0, "x1"),
 *       has coefficient 5.0 for "x1".
 *       Example: an expression with no term for "x2", after adding term (4.0, "x2"), has coefficient 4.0
 *       for "x2".</li>
 *   <li>Merge another {@code LinearExpression} into this one. The other expression's scalar is added to
 *       this expression's scalar. Each of the other expression's terms is added to this expression following
 *       the same accumulation rule as adding a single term (matching variables have their coefficients
 *       summed; new variables are added).
 *       Example: an expression with scalar 1.0 and coefficient 2.0 for "x1", after merging in an expression
 *       with scalar 3.0, coefficient 4.0 for "x1", and coefficient 5.0 for "x2", ends up with scalar 4.0,
 *       coefficient 6.0 for "x1", and coefficient 5.0 for "x2".</li>
 * </ul>
 *
 * <h2>Inspection operations</h2>
 * <ul>
 *   <li>Retrieve the scalar part (a0) of the expression.</li>
 *   <li>Retrieve the set of variable names currently present in the expression.</li>
 *   <li>Retrieve the coefficient of a given variable name. If that variable is not present in the
 *       expression, this returns 0.0 rather than throwing an exception.</li>
 *   <li>All these operations are read-only, thus, can't modify the expression.</li>
 * </ul>
 */
public class LinearExpression {

    private double scalar;

    /** Creates an empty expression: scalar 0.0 and no variables. */
    public LinearExpression() {
        this(0.0);
    }

    /**
     * Creates an expression with the given scalar and no variables.
     *
     * @param scalar the scalar part (a0).
     */
    public LinearExpression(double scalar) {
        this.scalar = scalar;
    }

    /** @return the scalar part (a0) of the expression. */
    public double scalar() {
        return scalar;
    }

    /**
     * Adds {@code value} to the scalar part of the expression.
     *
     * @param value the amount to add.
     */
    public void addScalar(double value) {
        scalar += value;
    }
}
