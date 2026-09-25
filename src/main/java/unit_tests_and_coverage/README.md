# Unit tests and coverage

The theory, and the reasoning behind this exercise, are in the training hub:
[Exercise: a calculator](https://github.com/sefop/sefop-training-hub/blob/main/book/05-testing/README.md#exercise-a-calculator).
This page only covers what's specific to Java.

The exercise has two parts:

- **[Part A](#part-a-test-divide):** write the tests for `divide` until `Calculator` is 100% covered.
- **[Part B](#part-b-refactor-without-touching-the-tests):** change how `Calculator` works inside, without
  changing what it promises, and see that the tests don't need to change.

## Part A: test `divide`

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

## The libraries

Two libraries do the work in this exercise. Both are already declared in `pom.xml`, so Maven downloads
them for you: there's nothing to install.

### JUnit 5: writing and running tests

[JUnit 5](https://junit.org/junit5/docs/current/user-guide/) is the standard test framework for Java, the counterpart of `pytest`.

| You want to… | JUnit 5 | pytest equivalent |
|---|---|---|
| Mark a method as a test | `@Test` | a function named `test_...` |
| Run one test with several inputs | `@ParameterizedTest` + `@ValueSource` / `@CsvSource` | `@pytest.mark.parametrize` |
| Skip a test for now | `@Disabled("reason")` | `@pytest.mark.skip` |
| Compare doubles | `assertEquals(expected, actual, delta)` | `pytest.approx` |
| Capture an exception | `Throwable thrown = assertThrows(Throwable.class, () -> ...)` | `with pytest.raises(Exception) as error:` |
| Check its type | `assertInstanceOf(SomeException.class, thrown)` | `assert isinstance(error.value, SomeError)` |

Two things catch people out:

- **`delta` is absolute.** `assertEquals(3.0, result, 1e-8)` accepts anything within 1e-8 of 3.0, whatever
  the size of the numbers. The worked example uses a *relative* tolerance by scaling the delta:
  `Math.abs(expected) * 1e-8`. That's what `pytest.approx(x, rel=1e-8)` does for you in Python.
- **Exception tests keep Arrange, Act and Assert apart too.** A call that throws never returns a result, so
  Act captures the exception and Assert checks its type:

  ```java
  // Act
  Throwable thrown = assertThrows(Throwable.class, () -> calculator.divide(1.0, 0.0));

  // Assert
  assertInstanceOf(ArithmeticException.class, thrown);
  ```

  `assertThrows` takes a lambda: write `() -> calculator.divide(1.0, 0.0)`, not `calculator.divide(1.0, 0.0)`.
  Without the `() ->`, the call runs *before* JUnit gets a chance to catch the exception, and the test crashes
  instead of passing.

### JaCoCo: measuring code coverage

[JaCoCo](https://www.jacoco.org/jacoco/) is the Java counterpart of `pytest-cov`. While the tests run, it
records which lines and branches of `src/main` were executed, then writes the result as an HTML report.
You never call JaCoCo yourself: `pom.xml` hooks it into the test run, so **every `mvnw test` also writes a
coverage report**.

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

## Reading the coverage report

After a test run, open this file in a browser:

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

## Part B: refactor without touching the tests

### Why

`Calculator` has two sides. The Javadoc of each method is its **contract**, or interface: what it
accepts, what it returns, and which exceptions it throws. The method bodies are **implementation details**:
one way, among many, of keeping that promise. Good tests check the contract only. So if you change the
implementation and keep the contract, the tests must stay green **without being edited**. That's what makes
it safe to clean up code: the tests tell you straight away whether you broke a promise.

### Before you start

Finish Part A first: no `@Disabled` left and `divide` fully covered. A test that is still disabled protects
nothing, so a refactor could break `divide` without any warning.

### Steps

1. **Run the tests** and write down the summary line (`Tests run: …`) and the coverage of `Calculator`.

2. **Refactor.** `add` and `divide` both start with the same few lines that reject NaN and infinite operands.
   Move that duplicated validation into **one private helper method** that both `add` and `divide` call.
   Don't open `CalculatorTest.java` while you do it.

   <details>
   <summary>Hint</summary>

   A signature that works well:

   ```java
   private static void requireFinite(double value, String name)
   ```

   It throws the same `IllegalArgumentException` as before when `value` is NaN or infinite, using `name` in
   the message. `add` and `divide` then call it once per operand: `requireFinite(a, "a")` and
   `requireFinite(b, "b")`. It can be `static` because it uses no field of `Calculator`.

   </details>

3. **Run the tests again.** You should see the same summary as in step 1, with every test green and
   `Calculator` still 100% covered. Run `git status`: `Calculator.java` is modified and `CalculatorTest.java`
   isn't. The implementation changed, the contract didn't, and the tests didn't notice. That's the point.

   Don't add a test for `requireFinite`. It's an implementation detail, not part of the contract. It already
   runs through the tests of `add` and `divide`, and the coverage report proves it: still 100% without any
   new test. A test that called the helper directly would break the day someone renames it or inlines it
   back, even though no promise changed. (`private` also stops you: the test class can't call it.)

4. **Break the contract on purpose.** In the helper, throw an `ArithmeticException` instead of an
   `IllegalArgumentException`, then run the tests. The non-finite tests of `add` fail, and so does yours
   for `divide`. This time the tests are right to complain: the exception type is written in the Javadoc,
   so it's part of the contract. Also notice that one change in one shared place broke two methods.
   **Undo the change** and check that everything is green again.

5. **Optional challenge.** In `divide`, call the helper for `a` only and remove the call for `b`. Run the
   tests. Did anything fail? If not, your Part A tests never tried a NaN or infinite *divisor*. The promise
   is broken and nobody noticed. Add that case to your test, watch it fail, then put the call back.
   Asking "would my tests catch this bug?" is exactly what the mutation testing exercise automates.

### What you learned

- Tests pin down the contract, not the code. The implementation is free to change underneath them.
- That's what makes refactoring safe: the same tests, still green, are your evidence that no promise broke.
- A test that goes red after a contract change is doing its job. The fix is in the code, or, if the new
  behavior is intended, in the contract and its tests together.
