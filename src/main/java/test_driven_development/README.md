# Test-driven development

The theory, and the worked example this exercise continues, are in the training hub:
[Test-driven development](https://github.com/sefop/sefop-training-hub/blob/main/book/05-testing/README.md#ch-tdd).
This page only covers what's specific to Java.

## The starting point

- [`LinearExpression.java`](LinearExpression.java) holds `LinearExpression`, a linear expression
  a0 + a1\*x1 + ... + an\*xn. Its Javadoc is the full specification.
- The scalar part is already built, test-first, exactly as in the book: `new LinearExpression()`,
  `new LinearExpression(3.0)`, `scalar()` and `addScalar(value)`.
- [`LinearExpressionTest.java`](../../../test/java/test_driven_development/LinearExpressionTest.java) holds the
  book's three tests, one per cycle.

Everything about variables is yours: construction with a term, adding a term (and accumulating it when the
variable is already there), merging two expressions, the set of variables, and the coefficient of a variable
(0.0 when it is absent). Names and parameters of the new methods are up to you.

## The steps

Repeat this cycle, one requirement of the Javadoc at a time:

1. **Red.** Add one test for the next requirement to `LinearExpressionTest`, under "your cycles start here".
   Run the tests and watch the new one fail. A test that calls a method that doesn't exist yet doesn't
   compile: in Java, that compile error is your red. If the test passes straight away, it tests nothing new:
   change the test, not the code.
2. **Green.** Write the minimum code in `LinearExpression` that makes it pass. Hard-coding an answer is
   allowed: the next test will force the general code.
3. **Refactor.** Improve the code (names, duplication, structure) and run the tests again. They must all
   still pass.
4. **Commit.** `git commit` after each green or refactor, so the history shows one cycle per commit.

Suggested order, from simplest to hardest: an empty expression has no variables (the first test that needs a
method for the set of variables), then a single term, a scalar and a term, adding a term for a new variable,
adding a term for a variable already present, the coefficient of an absent variable, and finally merging two
expressions.

You're done when every example in the Javadoc has a test and every test passes.

## Running the tests

From the root of the repository:

```bash
./mvnw test -Dtest='LinearExpressionTest'     # macOS / Linux / Git Bash
mvnw.cmd test -Dtest=LinearExpressionTest     # Windows (cmd or PowerShell: .\mvnw.cmd ...)
```

In IntelliJ IDEA, click the green arrow next to the test class. Before you start, the summary reads:

```
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

A red step shows up as `Failures: 1`, or as `COMPILATION ERROR` when the test calls a method that doesn't
exist yet. Read the failure message: it should say which requirement is missing, which is the test's name at
work.

Two Java details that come up in this exercise:

- **Compare doubles with a delta:** `assertEquals(expected, actual, TOLERANCE)`, never `assertEquals(expected,
  actual)` on computed values.
- **"Read-only" needs care.** If a method returns the `Map` or `Set` the expression keeps inside, the caller
  can change it and so change the expression. The Javadoc forbids that: write a test that tries it, watch it
  fail, then return an unmodifiable view or a copy (`Set.copyOf`, `Collections.unmodifiableSet`).
