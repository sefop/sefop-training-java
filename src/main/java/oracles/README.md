# Oracles

The theory, and the three worked tests this exercise starts from, are in the training hub:
[Test oracles](https://github.com/sefop/sefop-training-hub/blob/main/book/05-testing/README.md#ch-oracles).
The promises you test are listed, with example tests for each, in
[The optimization contract](https://github.com/sefop/sefop-training-hub/blob/main/book/05-testing/README.md#model-contract).
This page only covers what's specific to Java.

## The starting point

- [`Optimization.java`](Optimization.java) is the code under test: the cargo loading model of the book, solved
  with OR-Tools. It's complete: you only write tests (and one pseudo-oracle). `new Optimization().run(instance)`
  returns the load with the most revenue, or `null` when no load respects both capacities. Its Javadoc is the
  contract, with the seven promises numbered as in the book.
- [`Product.java`](Product.java), [`Instance.java`](Instance.java) and [`Solution.java`](Solution.java) are the
  records it works with.
- [`EnumerationSolver.java`](../../../test/java/oracles/EnumerationSolver.java) is a pseudo-oracle waiting to be
  written: a second implementation of the same contract that tries every candidate load.
- [`OptimizationTest.java`](../../../test/java/oracles/OptimizationTest.java) holds:
  - the book's three tests, finished, one per oracle: the two-pallet test (known oracle), the differential
    sweep (pseudo-oracle) and the capacity relation test (metamorphic relation). The sweep is disabled until
    `EnumerationSolver` exists.
  - **Part 1:** one empty test for every promise, plus the helper `assertValidSolution`.
  - **Part 2:** one empty test for every remaining example test of the contract.

  Each empty test's Javadoc names the promise it checks and the oracle that fits it.

## Part 1: one test per promise

1. Write `assertValidSolution(instance, solution)`: every committed pallet is loaded, both capacities hold, and
   the reported revenue and totals match the load. Unlike Python, there is no "whole number of pallets" check
   to write: `picked` holds `Integer` values, so the type already guarantees it.
2. For each empty test in Part 1, write the body with the Arrange / Act / Assert layout of the finished tests,
   then delete its `@Disabled("Exercise: implement me")` line.
3. Implement `EnumerationSolver.run`. Enable the test that checks it against the known two-pallet answer, then
   delete the `@Disabled` line of the differential sweep. A pseudo-oracle is only useful if you trust it, so
   check it on a known answer first.
4. Run the tests and make sure every Part 1 test passes.

## Part 2: every remaining example test

Do the same for each empty test in Part 2. The helper `threeProducts(...)` builds the book's three-product
instance with the capacities and commitment you choose, which most relation tests need.

## Break the code on purpose

Each step below is a bug that one kind of oracle catches. Make the change in `Optimization.java`, run the tests,
then **undo the change.**

1. **Known oracle.** In `buildModel`, change `instance.weightCapacity(), "payload"` to
   `instance.weightCapacity() - 1, "payload"`, a strict `<` for whole-number weights. Your test of committed
   pallets that fill the payload exactly fails: a boundary case is where an off-by-one shows.
2. **Pseudo-oracle.** In `buildModel`, change `MPSolver.infinity()` to `1`, so the model treats every product as
   a 0/1 choice, as a knapsack would. The worked two-pallet test still passes, because its best load holds one
   pallet. The differential sweep fails, and so do your tests whose load needs two pallets of one product: a
   hand-picked instance catches a mistake only if someone thought of it, and 200 generated ones need no one to.
3. **Metamorphic relation.** In `buildModel`, change `for (Product product : instance.products())` to
   `for (Product product : instance.products().subList(1, instance.products().size()))`, so the model forgets
   the first product. Your reverse-order test fails, although it never knew the optimal revenue.

You're done when every test passes, `Skipped` is 0, and the three breakages above were caught. Stuck? The
[`solutions`](https://github.com/sefop/sefop-training-java/tree/solutions) branch holds every test of this
exercise finished, and `EnumerationSolver` too.

## OR-Tools in five minutes

[OR-Tools](https://developers.google.com/optimization) is Google's open-source optimization library. It's
already declared in `pom.xml`, with its solvers and native libraries; this exercise uses SCIP, a mixed-integer
solver. You only need to read `Optimization.java`, not change it, but these are the pieces it uses:

| The model | OR-Tools |
|---|---|
| a solver | `MPSolver solver = MPSolver.createSolver("SCIP");` |
| $x_i \in \mathbb{Z}$, $x_i \ge l_i$ | `solver.makeIntVar(lowerBound, MPSolver.infinity(), name)` |
| $\sum_i w_i x_i \le W$ | `solver.makeConstraint(Double.NEGATIVE_INFINITY, capacity, name)`, then `setCoefficient(x, w)` |
| $\max \sum_i r_i x_i$ | `solver.objective().setCoefficient(x, r)`, then `setMaximization()` |
| solve, and check a load was found | `solver.solve() == MPSolver.ResultStatus.OPTIMAL` |
| the value of $x_i$ | `variable.solutionValue()` |

Two things catch people out:

- **Load the native libraries first.** OR-Tools is written in C++. `Loader.loadNativeLibraries()` must run
  before any solver is created; `Optimization` does it once, in a static block.
- **Values come back as doubles.** A solver may report `1.9999999` for two pallets, so `Optimization` rounds each
  count, and the tests compare revenues with a tolerance, `assertEquals(expected, actual, DELTA)`.

## Running the tests

From the root of the repository:

```bash
./mvnw test -Dtest='OptimizationTest'     # macOS / Linux / Git Bash
mvnw.cmd test -Dtest=OptimizationTest     # Windows (cmd or PowerShell: .\mvnw.cmd ...)
```

Before you start, the summary reads:

```
[WARNING] Tests run: 28, Failures: 0, Errors: 0, Skipped: 26
[INFO] BUILD SUCCESS
```
