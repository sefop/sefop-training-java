# Exercise 3: mocks

The theory, and the worked example this exercise starts from, are in the training hub:
[Mocks](https://github.com/sefop/sefop-training-hub/blob/main/book/05-testing/README.md#ch-mocks).
This page only covers what's specific to Java.

## The starting point

- [`NightlyPlanner.java`](NightlyPlanner.java) is the code under test. It's complete: you only write tests.
  `review(result)` pages the planner on call when the nightly solve found no plan. Its Javadoc is the
  contract:
  - `INFEASIBLE`: one page, `"Instance <id>: no feasible plan exists."`
  - `TIME_LIMIT_NO_SOLUTION`: one page, `"Instance <id>: no plan found within the time limit."`
  - `OPTIMAL` or `FEASIBLE`: no page.
- [`Pager.java`](Pager.java), [`SolveResult.java`](SolveResult.java) and [`SolveStatus.java`](SolveStatus.java)
  are the types it works with.
- [`NightlyPlannerTest.java`](../../../test/java/mocks/NightlyPlannerTest.java) holds:
  - the book's two tests, finished. The first uses `RecordingPager`, a mock written by hand: a class whose
    `page` adds the message to a list. The second uses Mockito, which builds such an object for you. Read
    both: they do the same job.
  - three empty tests for you, each marked `@Disabled`.

## The steps

For each empty test:

1. Write the body with the Arrange / Act / Assert layout of the finished tests, using `mock(Pager.class)`.
2. Delete the `@Disabled("Exercise: implement me")` line.
3. Run the tests and make sure the new one passes.

Then break the code on purpose, to see what the mock protects:

4. In `review`, call `pager.page(...)` twice for `INFEASIBLE`. Run the tests: the "once" checks fail, because
   a planner woken up twice for one problem is a bug. **Undo the change.**
5. In `review`, make the `TIME_LIMIT_NO_SOLUTION` case do nothing. Run the tests: your time-limit test
   fails. Without a mock, nothing in the program would show that a page was missing. **Undo the change.**

You're done when every test passes, `Skipped` is 0, and both breakages above were caught.

## Mockito in five minutes

[Mockito](https://site.mockito.org/) is the standard mocking library for Java. It's already declared in
`pom.xml`, so Maven downloads it for you. `mock(Pager.class)` creates an object that implements `Pager`,
does nothing, and records each call.

| The book's pseudocode | Mockito |
|---|---|
| `pager = mock(Pager)` | `Pager pager = mock(Pager.class);` |
| `expect pager.page called once with "…"` | `verify(pager, times(1)).page("…");` |
| `expect pager.page never called` | `verify(pager, never()).page(anyString());` or `verifyNoInteractions(pager);` |

Two things catch people out:

- **`verify(pager).page("…")` already means "exactly once".** `times(1)` is the default; writing it out makes
  the promise explicit. To also check that no *other* call happened, add `verifyNoMoreInteractions(pager)`.
- **Verify after acting.** Mockito checks the calls recorded so far. A `verify` placed before
  `planner.review(result)` sees no calls and fails, or, worse, `never()` passes for the wrong reason.

The static imports you need: `import static org.mockito.Mockito.*;`.

## Running the tests

From the root of the repository:

```bash
./mvnw test -Dtest='NightlyPlannerTest'     # macOS / Linux / Git Bash
mvnw.cmd test -Dtest=NightlyPlannerTest     # Windows (cmd or PowerShell: .\mvnw.cmd ...)
```

Before you start, the summary reads:

```
[WARNING] Tests run: 5, Failures: 0, Errors: 0, Skipped: 3
[INFO] BUILD SUCCESS
```
