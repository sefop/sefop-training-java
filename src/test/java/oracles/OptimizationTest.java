package oracles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Optimization#run(Instance)}, the cargo model, sorted by the test oracle each one uses.
 *
 * <p>A test oracle is whatever tells the test the expected behavior. Three kinds appear here:
 * <ul>
 *   <li>Known oracle: the expected answer, worked out by hand before the test runs.</li>
 *   <li>Pseudo-oracle: {@link EnumerationSolver}, a second implementation that tries every candidate load.</li>
 *   <li>Metamorphic relation: two related runs whose results must compare in a way you can prove, even when
 *       neither result is known.</li>
 * </ul>
 *
 * <p>The class has three parts:
 * <ul>
 *   <li>Worked examples: the book's three tests, one per oracle, finished.</li>
 *   <li>Part 1: one test for every promise of the contract (see {@link Optimization}), plus
 *       {@code EnumerationSolver}, which the differential sweep needs.</li>
 *   <li>Part 2: every remaining example test of the book's contract.</li>
 * </ul>
 *
 * <p>Test names follow {@code method_givenCondition_expectedOutcome}, so a failing test reports in plain words
 * which promise was broken. Revenues and totals are compared with a tolerance, {@code DELTA}, because the model
 * computes with floating-point numbers.
 */
class OptimizationTest {

    private static final double DELTA = 1e-6;

    /** Asserts behavior 1, a valid solution: the load is feasible and reported correctly. */
    private static void assertValidSolution(Instance instance, Solution solution) {
        double totalWeight = 0;
        double totalVolume = 0;
        double revenue = 0;
        for (Product product : instance.products()) {
            int count = solution.picked().get(product.name());
            assertTrue(count >= product.committedQuantity(), product.name() + ": committed pallets left behind");
            totalWeight += product.weight() * count;
            totalVolume += product.volume() * count;
            revenue += product.revenue() * count;
        }
        assertEquals(totalWeight, solution.totalWeight(), DELTA, "weight misreported");
        assertEquals(totalVolume, solution.totalVolume(), DELTA, "volume misreported");
        assertEquals(revenue, solution.objectiveValue(), DELTA, "revenue misreported");
        assertTrue(solution.totalWeight() <= instance.weightCapacity(), "payload exceeded");
        assertTrue(solution.totalVolume() <= instance.volumeCapacity(), "hold exceeded");
    }

    /** The book's instance for the relation tests: its best load is two pallets of A and one of B. */
    private static Instance threeProducts(double weightCapacity, double volumeCapacity, int committedC) {
        return new Instance(
                List.of(
                        new Product("A", 2, 1, 10),
                        new Product("B", 1, 2, 6),
                        new Product("C", 3, 1, 14, committedC)),
                weightCapacity,
                volumeCapacity);
    }

    /** The book's three-product instance with its usual capacities and nothing committed. */
    private static Instance threeProducts() {
        return threeProducts(5, 4, 0);
    }

    // =====================================================================================================
    // Worked examples: the book's three tests, one per oracle.
    // =====================================================================================================

    /** Known oracle: the four candidate loads, listed by hand, show the best one earns 10. */
    @Test
    void run_givenTheTwoPalletInstance_loadsTheHigherRevenuePallet() {
        // Arrange
        Product a = new Product("A", 2, 1, 10);
        Product b = new Product("B", 1, 2, 6);
        Instance instance = new Instance(List.of(a, b), 2, 2);

        // Act
        Solution solution = new Optimization().run(instance);

        // Assert
        assertNotNull(solution);
        assertEquals(10, solution.objectiveValue(), DELTA);
    }

    /** Pseudo-oracle: on 200 small random instances, run and EnumerationSolver agree. */
    @Test
    void run_givenRandomSmallInstances_agreesWithEnumeration() {
        Random rng = new Random(20260908);

        for (int round = 0; round < 200; round++) {
            // Arrange
            List<Product> products = new ArrayList<>();
            int productCount = rng.nextInt(1, 5);
            for (int i = 0; i < productCount; i++) {
                products.add(new Product("P" + i, rng.nextInt(1, 6), rng.nextInt(1, 6), rng.nextInt(0, 21),
                        rng.nextInt(0, 3)));
            }
            Instance instance = new Instance(products, rng.nextInt(0, 9), rng.nextInt(0, 9));

            // Act
            Solution reference = new EnumerationSolver().run(instance);
            Solution candidate = new Optimization().run(instance);

            // Assert
            assertEquals(reference == null, candidate == null, "feasibility differs on " + instance);
            if (reference != null) {
                assertEquals(reference.objectiveValue(), candidate.objectiveValue(), DELTA, "on " + instance);
            }
        }
    }

    /** Metamorphic relation: more payload can only keep or raise the best revenue. */
    @Test
    void run_givenAHigherPayloadCapacity_doesNotLowerTheRevenue() {
        // Arrange
        Instance beforeInstance = threeProducts(5, 4, 0);
        Instance afterInstance = threeProducts(8, 4, 0);

        // Act
        Solution before = new Optimization().run(beforeInstance);
        Solution after = new Optimization().run(afterInstance);

        // Assert
        assertTrue(after.objectiveValue() >= before.objectiveValue());
    }

    // =====================================================================================================
    // Part 1: one test per promise.
    // =====================================================================================================

    /** Behavior 1, valid solution. Oracle: the constraint definition (assertValidSolution). */
    @Test
    void run_givenAFeasibleInstance_returnsAValidSolution() {
        // Arrange
        Instance instance = threeProducts(8, 4, 1);

        // Act
        Solution solution = new Optimization().run(instance);

        // Assert
        assertNotNull(solution);
        assertValidSolution(instance, solution);
    }

    /** Behavior 2, no solution from an empty feasible set. Oracle: known (one sum). */
    @Test
    void run_givenCommittedPalletsHeavierThanThePayload_returnsNull() {
        // Arrange: two committed pallets of one tonne each, and the aircraft may carry one tonne.
        Product mail = new Product("M", 1, 1, 4, 2);
        Instance instance = new Instance(List.of(mail), 1, 5);

        // Act
        Solution solution = new Optimization().run(instance);

        // Assert
        assertNull(solution);
    }

    /** Behavior 3, existence &amp; optimality, on a boundary. Oracle: known. */
    @Test
    void run_givenCommittedPalletsThatFillThePayloadExactly_loadsOnlyTheCommittedPallets() {
        // Arrange: the committed mail fills the payload exactly, so no other pallet fits.
        Product mail = new Product("M", 1, 1, 4, 2);
        Product gold = new Product("G", 1, 1, 9);
        Instance instance = new Instance(List.of(mail, gold), 2, 5);

        // Act
        Solution solution = new Optimization().run(instance);

        // Assert
        assertNotNull(solution);
        assertEquals(Map.of("M", 2, "G", 0), solution.picked());
        assertEquals(8, solution.objectiveValue(), DELTA);
    }

    /** Behavior 4, permutation invariance. Oracle: metamorphic relation. */
    @Test
    void run_givenTheProductsInReverseOrder_earnsTheSameRevenue() {
        // Arrange
        Instance instance = threeProducts();
        Instance reversedInstance = new Instance(
                instance.products().reversed(), instance.weightCapacity(), instance.volumeCapacity());

        // Act
        Solution first = new Optimization().run(instance);
        Solution second = new Optimization().run(reversedInstance);

        // Assert
        assertEquals(first.objectiveValue(), second.objectiveValue(), DELTA);
    }

    /** Behavior 5, objective changed. Oracle: metamorphic relation. */
    @Test
    void run_givenEveryRevenueMultipliedByK_multipliesTheRevenueByK() {
        // Arrange
        double k = 3;
        Instance instance = threeProducts();
        List<Product> scaledProducts = new ArrayList<>();
        for (Product p : instance.products()) {
            scaledProducts.add(new Product(p.name(), p.weight(), p.volume(), p.revenue() * k, p.committedQuantity()));
        }
        Instance scaled = new Instance(scaledProducts, instance.weightCapacity(), instance.volumeCapacity());

        // Act
        Solution first = new Optimization().run(instance);
        Solution second = new Optimization().run(scaled);

        // Assert
        assertEquals(k * first.objectiveValue(), second.objectiveValue(), DELTA);
    }

    /** Behavior 6, feasible set expanded. Oracle: metamorphic relation. */
    @Test
    void run_givenANewUncommittedProduct_doesNotLowerTheRevenue() {
        // Arrange
        Instance instance = threeProducts();
        List<Product> extendedProducts = new ArrayList<>(instance.products());
        extendedProducts.add(new Product("D", 1, 1, 5));
        Instance extended = new Instance(extendedProducts, instance.weightCapacity(), instance.volumeCapacity());

        // Act
        Solution first = new Optimization().run(instance);
        Solution second = new Optimization().run(extended);

        // Assert
        assertTrue(second.objectiveValue() >= first.objectiveValue());
    }

    /** Behavior 7, feasible set reduced, keeping the first load. Oracle: metamorphic relation. */
    @Test
    void run_givenTheFirstRunsPalletsCommitted_earnsTheSameRevenue() {
        // Arrange
        Instance instance = threeProducts();
        Solution first = new Optimization().run(instance);
        List<Product> committedProducts = new ArrayList<>();
        for (Product p : instance.products()) {
            committedProducts.add(new Product(p.name(), p.weight(), p.volume(), p.revenue(), first.picked().get(p.name())));
        }
        Instance committed = new Instance(committedProducts, instance.weightCapacity(), instance.volumeCapacity());

        // Act
        Solution second = new Optimization().run(committed);

        // Assert
        assertNotNull(second);
        assertEquals(first.objectiveValue(), second.objectiveValue(), DELTA);
    }

    /** Checks the pseudo-oracle itself against a known answer before trusting it in the sweep. */
    @Test
    void enumerationSolver_givenTheTwoPalletInstance_findsTheKnownOptimum() {
        // Arrange
        Product a = new Product("A", 2, 1, 10);
        Product b = new Product("B", 1, 2, 6);
        Instance instance = new Instance(List.of(a, b), 2, 2);

        // Act
        Solution solution = new EnumerationSolver().run(instance);

        // Assert
        assertNotNull(solution);
        assertEquals(10, solution.objectiveValue(), DELTA);
    }

    // =====================================================================================================
    // Part 2: every remaining example test of the contract.
    // =====================================================================================================

    /** Behavior 2. Oracle: known (one sum). */
    @Test
    void run_givenCommittedPalletsBulkierThanTheHold_returnsNull() {
        // Arrange: two committed pallets of 3 cubic meters each, and the hold takes 5.
        Product mail = new Product("M", 1, 3, 4, 2);
        Instance instance = new Instance(List.of(mail), 10, 5);

        // Act
        Solution solution = new Optimization().run(instance);

        // Assert
        assertNull(solution);
    }

    /** Behavior 3. Oracle: known. */
    @Test
    void run_givenNoProducts_returnsAnEmptyLoadWorthZero() {
        // Arrange
        Instance instance = new Instance(List.of(), 5, 4);

        // Act
        Solution solution = new Optimization().run(instance);

        // Assert
        assertNotNull(solution);
        assertEquals(Map.of(), solution.picked());
        assertEquals(0, solution.objectiveValue(), DELTA);
    }

    /** Behavior 3. Oracle: known. */
    @Test
    void run_givenNoProductFitsOnItsOwn_returnsAnEmptyLoadWorthZero() {
        // Arrange: A is too heavy for the payload, B too bulky for the hold.
        Product a = new Product("A", 3, 1, 10);
        Product b = new Product("B", 1, 5, 6);
        Instance instance = new Instance(List.of(a, b), 2, 4);

        // Act
        Solution solution = new Optimization().run(instance);

        // Assert
        assertNotNull(solution);
        assertEquals(Map.of("A", 0, "B", 0), solution.picked());
        assertEquals(0, solution.objectiveValue(), DELTA);
    }

    /** Behavior 3. Oracle: known, m = floor(min(W / w, V / v)) = floor(min(7 / 2, 10 / 1)) = 3. */
    @Test
    void run_givenOneProductThatFitsSeveralTimes_loadsAsManyPalletsAsFit() {
        // Arrange
        Product p = new Product("P", 2, 1, 5);
        Instance instance = new Instance(List.of(p), 7, 10);

        // Act
        Solution solution = new Optimization().run(instance);

        // Assert
        assertNotNull(solution);
        assertEquals(Map.of("P", 3), solution.picked());
        assertEquals(15, solution.objectiveValue(), DELTA);
    }

    /** Behavior 3. Oracle: known; the unique best load is two pallets of A, worth 20. */
    @Test
    void run_givenAnOptimumThatFillsOnlyThePayload_returnsThatLoad() {
        // Arrange
        Product a = new Product("A", 2, 1, 10);
        Product b = new Product("B", 1, 3, 3);
        Instance instance = new Instance(List.of(a, b), 4, 5);

        // Act
        Solution solution = new Optimization().run(instance);

        // Assert
        assertNotNull(solution);
        assertEquals(Map.of("A", 2, "B", 0), solution.picked());
        assertEquals(4, solution.totalWeight(), DELTA);
        assertTrue(solution.totalVolume() < 5);
    }

    /** Behavior 3. Oracle: known; the unique best load is two pallets of A, worth 20. */
    @Test
    void run_givenAnOptimumThatFillsOnlyTheHold_returnsThatLoad() {
        // Arrange
        Product a = new Product("A", 1, 2, 10);
        Product b = new Product("B", 3, 1, 3);
        Instance instance = new Instance(List.of(a, b), 5, 4);

        // Act
        Solution solution = new Optimization().run(instance);

        // Assert
        assertNotNull(solution);
        assertEquals(Map.of("A", 2, "B", 0), solution.picked());
        assertEquals(4, solution.totalVolume(), DELTA);
        assertTrue(solution.totalWeight() < 5);
    }

    /** Behavior 3. Oracle: known; the unique best load is one A and two B, worth 22. */
    @Test
    void run_givenAnOptimumThatFillsBothCapacities_returnsThatLoad() {
        // Arrange
        Product a = new Product("A", 2, 1, 10);
        Product b = new Product("B", 1, 2, 6);
        Instance instance = new Instance(List.of(a, b), 4, 5);

        // Act
        Solution solution = new Optimization().run(instance);

        // Assert
        assertNotNull(solution);
        assertEquals(Map.of("A", 1, "B", 2), solution.picked());
        assertEquals(4, solution.totalWeight(), DELTA);
        assertEquals(5, solution.totalVolume(), DELTA);
    }

    /** Behavior 3. Oracle: known. Either pallet is optimal, so assert the revenue, never picked. */
    @Test
    void run_givenTwoTiedPalletsThatDoNotFitTogether_earnsTheTiedRevenue() {
        // Arrange
        Product a = new Product("A", 2, 1, 5);
        Product b = new Product("B", 1, 2, 5);
        Instance instance = new Instance(List.of(a, b), 2, 2);

        // Act
        Solution solution = new Optimization().run(instance);

        // Assert
        assertNotNull(solution);
        assertEquals(5, solution.objectiveValue(), DELTA);
    }

    /** Behavior 3. Oracle: known; the rest of the load is the two-pallet answer, one A. */
    @Test
    void run_givenAnUncommittedProductTooHeavyToFly_leavesItBehind() {
        // Arrange
        Product a = new Product("A", 2, 1, 10);
        Product b = new Product("B", 1, 2, 6);
        Product heavy = new Product("H", 5, 1, 100);
        Instance instance = new Instance(List.of(a, b, heavy), 2, 2);

        // Act
        Solution solution = new Optimization().run(instance);

        // Assert
        assertNotNull(solution);
        assertEquals(Map.of("A", 1, "B", 0, "H", 0), solution.picked());
        assertEquals(10, solution.objectiveValue(), DELTA);
    }

    /** Behavior 5. Oracle: metamorphic relation. */
    @Test
    void run_givenOneRevenueRaised_doesNotLowerTheRevenue() {
        // Arrange
        Instance instance = threeProducts();
        Product c = instance.products().get(2);
        Instance raised = new Instance(
                List.of(instance.products().get(0), instance.products().get(1),
                        new Product(c.name(), c.weight(), c.volume(), c.revenue() + 6, c.committedQuantity())),
                instance.weightCapacity(),
                instance.volumeCapacity());

        // Act
        Solution first = new Optimization().run(instance);
        Solution second = new Optimization().run(raised);

        // Assert
        assertTrue(second.objectiveValue() >= first.objectiveValue());
    }

    /** Behavior 5. Oracle: metamorphic relation. */
    @Test
    void run_givenSeveralRevenuesRaised_doesNotLowerTheRevenue() {
        // Arrange
        Instance instance = threeProducts();
        Product a = instance.products().get(0);
        Product b = instance.products().get(1);
        Instance raised = new Instance(
                List.of(new Product(a.name(), a.weight(), a.volume(), a.revenue() + 1, a.committedQuantity()),
                        new Product(b.name(), b.weight(), b.volume(), b.revenue() + 2, b.committedQuantity()),
                        instance.products().get(2)),
                instance.weightCapacity(),
                instance.volumeCapacity());

        // Act
        Solution first = new Optimization().run(instance);
        Solution second = new Optimization().run(raised);

        // Assert
        assertTrue(second.objectiveValue() >= first.objectiveValue());
    }

    /** Behavior 6. Oracle: metamorphic relation. */
    @Test
    void run_givenAHigherHoldCapacity_doesNotLowerTheRevenue() {
        // Act
        Solution first = new Optimization().run(threeProducts(5, 4, 0));
        Solution second = new Optimization().run(threeProducts(5, 6, 0));

        // Assert
        assertTrue(second.objectiveValue() >= first.objectiveValue());
    }

    /** Behavior 6. Oracle: metamorphic relation. */
    @Test
    void run_givenACommittedPalletReleased_doesNotLowerTheRevenue() {
        // Act
        Solution first = new Optimization().run(threeProducts(5, 4, 1));
        Solution second = new Optimization().run(threeProducts(5, 4, 0));

        // Assert
        assertTrue(second.objectiveValue() >= first.objectiveValue());
    }

    /** Behavior 7. Oracle: metamorphic relation. */
    @Test
    void run_givenALowerPayloadCapacity_doesNotRaiseTheRevenue() {
        // Act
        Solution first = new Optimization().run(threeProducts(5, 4, 0));
        Solution second = new Optimization().run(threeProducts(4, 4, 0));

        // Assert
        assertNotNull(second);
        assertTrue(second.objectiveValue() <= first.objectiveValue());
    }

    /** Behavior 7. Oracle: metamorphic relation. */
    @Test
    void run_givenALowerHoldCapacity_doesNotRaiseTheRevenue() {
        // Act
        Solution first = new Optimization().run(threeProducts(5, 4, 0));
        Solution second = new Optimization().run(threeProducts(5, 3, 0));

        // Assert
        assertNotNull(second);
        assertTrue(second.objectiveValue() <= first.objectiveValue());
    }

    /** Behavior 7. Oracle: metamorphic relation. */
    @Test
    void run_givenAnUncommittedProductRemoved_doesNotRaiseTheRevenue() {
        // Arrange
        Instance instance = threeProducts();
        Instance withoutB = new Instance(
                List.of(instance.products().get(0), instance.products().get(2)),
                instance.weightCapacity(),
                instance.volumeCapacity());

        // Act
        Solution first = new Optimization().run(instance);
        Solution second = new Optimization().run(withoutB);

        // Assert
        assertTrue(second.objectiveValue() <= first.objectiveValue());
    }

    /** Behavior 7. Oracle: metamorphic relation. */
    @Test
    void run_givenOneMoreCommittedPallet_doesNotRaiseTheRevenue() {
        // Act
        Solution first = new Optimization().run(threeProducts(5, 4, 0));
        Solution second = new Optimization().run(threeProducts(5, 4, 1));

        // Assert
        assertNotNull(second);
        assertTrue(second.objectiveValue() <= first.objectiveValue());
    }
}
