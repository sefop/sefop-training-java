# Exercise 1: unit tests and coverage (Java)

The theory, and the reasoning behind this exercise, are in the training hub:
[Exercise 1: a calculator](https://github.com/sefop/sefop-training-hub/blob/main/book/05-testing/README.md#exercise-1-a-calculator).
This page only covers what's specific to Java.

## The task

- [`Calculator.java`](Calculator.java) is the code under test.
- [`CalculatorTest.java`](../../../test/java/unit_tests_and_coverage/CalculatorTest.java) holds all the tests for
  `Calculator`, in two parts:
  - the `add` tests are a finished worked example. Read them first.
  - the `divide` tests are yours: six empty tests, each marked `@Disabled`.

For each `divide` test in `CalculatorTest`:

1. Write the body using the Arrange / Act / Assert layout from the `add` tests.
2. Delete the `@Disabled("Exercise: implement me")` line so JUnit runs the test.
3. Run the tests and make sure the new test passes.
4. Check the coverage report to see which lines of `divide` are now covered.

When you're done, every test should pass, `Skipped` should be 0, and `divide` should be fully covered.

## JUnit 5 in five minutes

[JUnit 5](https://junit.org/junit5/docs/current/user-guide/) is the standard test framework for Java, the counterpart of `pytest`.

| You want to… | JUnit 5 | pytest equivalent |
|---|---|---|
| Mark a method as a test | `@Test` | a function named `test_...` |
| Run one test with several inputs | `@ParameterizedTest` + `@ValueSource` / `@CsvSource` | `@pytest.mark.parametrize` |
| Skip a test for now | `@Disabled("reason")` | `@pytest.mark.skip` |
| Compare doubles | `assertEquals(expected, actual, delta)` | `pytest.approx` |
| Expect an exception | `assertThrows(SomeException.class, () -> ...)` | `with pytest.raises(...)` |

Two things catch people out:

- **`delta` is absolute.** `assertEquals(3.0, result, 1e-8)` accepts anything within 1e-8 of 3.0, whatever
  the size of the numbers. The worked example uses a *relative* tolerance by scaling the delta:
  `Math.abs(expected) * 1e-8`. That's what `pytest.approx(x, rel=1e-8)` does for you in Python.
- **`assertThrows` takes a lambda.** Write `() -> calc.divide(1.0, 0.0)`, not `calc.divide(1.0, 0.0)`.
  Without the `() ->`, the call runs *before* JUnit gets a chance to catch the exception, and the test crashes
  instead of passing.

## Running the tests

From the root of the repository:

```bash
./mvnw test          # macOS / Linux / Git Bash
mvnw.cmd test        # Windows (cmd or PowerShell: .\mvnw.cmd test)
```

To run only the `divide` tests (the quotes stop the shell from expanding `*`):

```bash
./mvnw test -Dtest='CalculatorTest#divide*'
```

In IntelliJ IDEA, click the green arrow next to a test class or method.

The output ends with a summary like this:

```
[WARNING] Tests run: 18, Failures: 0, Errors: 0, Skipped: 6
[INFO] BUILD SUCCESS
```

`Skipped` counts the tests that still have `@Disabled`. Each input of a parameterized test counts as a
separate test, so the total goes up as you add cases.

## Code coverage with JaCoCo

[JaCoCo](https://www.jacoco.org/jacoco/) is the Java counterpart of `pytest-cov`. It records which lines
and branches of `src/main` ran while the tests ran. It's already set up in `pom.xml`, so
**every `mvnw test` also writes a coverage report**. Open this file in a browser:

```
target/site/jacoco/index.html
```

Click through `unit_tests_and_coverage` → `Calculator` → `Calculator.java` to see the source code colored:

- **Green:** the line ran during the tests.
- **Red:** the line never ran. No test covers it.
- **Yellow:** the line ran, but only some of its branches did. For example, an `if` whose condition was
  only ever `false`. Hover over the diamond in the margin to see how many branches were missed.

Before you start, `add` is fully green and `divide` is red. Watch `divide` turn green as you enable your
tests. The report's `Missed Branches` column is the most useful number here: a line can be green while one
side of its `if` has never been tested.

**IntelliJ alternative:** right-click a test class → *More Run/Debug* → *Run with Coverage*. IntelliJ
colors the editor margin directly. It uses its own coverage engine, so numbers can differ a little from
JaCoCo's. JaCoCo is the one a CI server would use.
