package oracles;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Finds the load of a flight that earns the most revenue: the cargo loading model of the book.
 *
 * <p>This is the running example in its "unlimited tender" variant: the shipper tenders as many pallets of
 * each product as the aircraft takes, so only the two capacities limit the load. Every pallet has a positive
 * weight and volume, and a revenue of zero or more. Some pallets are committed: they must fly.
 *
 * <p>Everything {@code run} does to find the load (building the model, calling the solver, reading the answer
 * back) is an implementation detail: the tests check only what {@code run} promises.
 *
 * <p>Contract of {@code run}, as in the book's chapter "Testing an optimization model".
 *
 * <p>Software promises:
 * <ul>
 *   <li>{@code run} throws an {@link IllegalArgumentException} when {@code instance} is null.</li>
 *   <li>otherwise {@code run} returns a {@link Solution}, or {@code null} when it could not provide a feasible
 *       solution.</li>
 * </ul>
 *
 * <p>Mathematical promises, with z the objective value of a returned {@code Solution}:
 * <ol>
 *   <li>Valid solution: every quantity is a whole number of pallets, every committed pallet is loaded, both
 *       capacities are respected, and the reported revenue and totals match the load.</li>
 *   <li>No solution from an empty feasible set: if no load respects both capacities, {@code run} returns
 *       {@code null}.</li>
 *   <li>Existence &amp; optimality: if a feasible load exists, {@code run} returns one with the optimal
 *       revenue.</li>
 *   <li>Permutation invariance: listing the products in another order leaves z unchanged.</li>
 *   <li>Objective changed, feasible set unchanged: multiplying every revenue by k &gt; 0 multiplies z by k;
 *       raising revenues never lowers z.</li>
 *   <li>Feasible set expanded: z never falls when the set of feasible loads grows.</li>
 *   <li>Feasible set reduced: z never rises when the set of feasible loads shrinks, and stays the same when
 *       the first load is still feasible.</li>
 * </ol>
 */
public class Optimization {

    static {
        // OR-Tools is written in C++: this loads its native libraries, bundled in the jar, once.
        Loader.loadNativeLibraries();
    }

    /**
     * Returns the load with the most revenue, or {@code null} when no load respects both capacities.
     *
     * @param instance the flight to load; must not be null.
     * @return the best load, or {@code null}.
     * @throws IllegalArgumentException if {@code instance} is null.
     */
    public Solution run(Instance instance) {
        if (instance == null) {
            throw new IllegalArgumentException("instance must not be null");
        }
        MPSolver solver = MPSolver.createSolver("SCIP");
        Map<String, MPVariable> pallets = buildModel(solver, instance);
        MPSolver.ResultStatus status = solver.solve();
        if (status != MPSolver.ResultStatus.OPTIMAL) {
            return null;
        }
        return assembleSolution(instance, pallets);
    }

    /** Adds one integer variable per product, the two capacity constraints and the objective. */
    private Map<String, MPVariable> buildModel(MPSolver solver, Instance instance) {
        Map<String, MPVariable> pallets = new LinkedHashMap<>();
        MPConstraint payload = solver.makeConstraint(Double.NEGATIVE_INFINITY, instance.weightCapacity(), "payload");
        MPConstraint hold = solver.makeConstraint(Double.NEGATIVE_INFINITY, instance.volumeCapacity(), "hold");
        MPObjective revenue = solver.objective();
        for (Product product : instance.products()) {
            // The committed pallets are the variable's lower bound: they must fly.
            MPVariable count = solver.makeIntVar(product.committedQuantity(), MPSolver.infinity(), product.name());
            pallets.put(product.name(), count);
            payload.setCoefficient(count, product.weight());
            hold.setCoefficient(count, product.volume());
            revenue.setCoefficient(count, product.revenue());
        }
        revenue.setMaximization();
        return pallets;
    }

    /** Reads the pallets back from the solver and computes the load's revenue and totals. */
    private Solution assembleSolution(Instance instance, Map<String, MPVariable> pallets) {
        Map<String, Product> byName = new LinkedHashMap<>();
        for (Product product : instance.products()) {
            byName.put(product.name(), product);
        }
        Map<String, Integer> picked = new LinkedHashMap<>();
        double objectiveValue = 0;
        double totalWeight = 0;
        double totalVolume = 0;
        for (Map.Entry<String, MPVariable> entry : pallets.entrySet()) {
            // The solver returns floating-point values such as 1.9999999: round them to whole pallets.
            int count = (int) Math.round(entry.getValue().solutionValue());
            Product product = byName.get(entry.getKey());
            picked.put(entry.getKey(), count);
            objectiveValue += product.revenue() * count;
            totalWeight += product.weight() * count;
            totalVolume += product.volume() * count;
        }
        return new Solution(picked, objectiveValue, totalWeight, totalVolume);
    }
}
