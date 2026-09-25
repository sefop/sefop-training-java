# Integration testing

The theory, and the worked example this exercise starts from, are in the training hub:
[Integration testing](https://github.com/sefop/sefop-training-hub/blob/main/book/05-testing/README.md#ch-integration).
This page only covers what's specific to Java.

## The starting point

Every night, a solve job saves tomorrow's result to a file, and the nightly planner reads it back to decide whether
to wake the planner on call. The code is complete: you only write tests.

- [`ResultsWriter.java`](ResultsWriter.java): `new ResultsWriter(folder).write(result)` saves `<instanceId>.csv`,
  owned by the solve job's team.
- [`ResultsReader.java`](ResultsReader.java): `new ResultsReader(folder).read(instanceId)` reads it back, owned by
  the planner's team. It throws `MissingResultException` when tonight's file doesn't exist.
- [`NightlyPlanner.java`](NightlyPlanner.java): `new NightlyPlanner(reader, notifier).reviewTonight(instanceId)`
  reads the result and notifies when there is no plan.

The writer and the reader share no code on purpose: each encodes its own idea of the file's format, and the Javadoc
states it. [`NightlyPlannerIntegrationTest.java`](../../../test/java/integration_testing/NightlyPlannerIntegrationTest.java)
holds:

- the book's test, finished. It writes a real file with the real writer, runs the planner with the real reader, and
  keeps only the notifier a mock.
- three empty tests for you, each marked `@Disabled`. All of them touch the real file system, so all of them are
  integration tests.

## The steps

For each empty test:

1. Write the body with the Arrange / Act / Assert layout of the finished test, using its `@TempDir Path folder`.
   - **Missing file:** read an instance that was never written, and check that `MissingResultException` is thrown
     (`assertThrows(MissingResultException.class, () -> ...)`).
   - **The writer's own test:** write an infeasible result, then compare the file's text
     (`Files.readString(folder.resolve("2026-09-26.csv"))`) with the exact text you expect:
     `"instance_id,status\n2026-09-26,infeasible\n"`.
   - **The reader's own test:** write that text into the file yourself with `Files.writeString`, without
     `ResultsWriter`, then check that `read` returns the right `SolveResult`.
2. Delete the `@Disabled("Exercise: implement me")` line.
3. Run the tests and make sure the new one passes.

Then play the solve job's team changing its format, to see what only the joined test catches:

4. In `ResultsWriter.write`, write the status capitalized ("Infeasible"). Update the writer's own test to expect
   `Infeasible`, as that team would. Run the tests: the writer's test passes, the reader's test passes, and only the
   finished test, which joins the two, fails with `Unknown status: Infeasible`. **Undo both changes.**

You're done when every test passes, `Skipped` is 0, and step 4 behaved as described.

## @TempDir in one minute

[`@TempDir`](https://junit.org/junit5/docs/current/user-guide/#writing-tests-built-in-extensions-TempDirectory) is a
JUnit 5 annotation: put it on a `Path` parameter of a test method, and JUnit passes in a new, empty temporary folder.
Each test gets its own folder, and JUnit deletes it afterwards, so tests never see each other's files.

```java
@Test
void example(@TempDir Path folder) throws IOException {
    new ResultsWriter(folder).write(...);
    String text = Files.readString(folder.resolve("2026-09-26.csv"));
}
```

`Files.readString` and `Files.writeString` throw the checked `IOException`: add `throws IOException` to the test
method, as above.

## Running the tests

From the root of the repository:

```bash
./mvnw test -Dtest='NightlyPlannerIntegrationTest'     # macOS / Linux / Git Bash
mvnw.cmd test -Dtest=NightlyPlannerIntegrationTest     # Windows (cmd or PowerShell: .\mvnw.cmd ...)
```

Before you start, the summary reads:

```
[WARNING] Tests run: 4, Failures: 0, Errors: 0, Skipped: 3
[INFO] BUILD SUCCESS
```
