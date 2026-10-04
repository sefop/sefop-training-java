package oracles;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Finds the load of a flight that earns the most revenue: the cargo loading model of the book.
 *
 * <p>Any number of pallets of each product may be loaded, so only the two capacities limit the load. Every
 * pallet has a positive weight and volume, and a revenue of zero or more. Some pallets are committed: they
 * must fly.
 *
 * <p>{@code run} takes an {@link Instance} and returns a {@link Result}: a {@link Status}, and a
 * {@link Solution} when there is a load to return. Everything {@code run} does to find the load (building the model, calling the solver, reading the answer
 * back) is an implementation detail: the tests check only what {@code run} promises.
 *
 * <p>Contract of {@code run}, as in the book's chapters "Contracts" (design section) and "Testing an
 * optimization model" (testing section).
 *
 * <p>Software promises:
 * <ul>
 *   <li>{@code run} throws an {@link InvalidInstanceException} when the instance is malformed: it is null, a
 *       weight or a volume is not positive, a revenue, a committed quantity or a capacity is negative, or two
 *       products share a name.</li>
 *   <li>otherwise {@code run} returns a {@link Result}, which carries a {@link Solution} exactly when its
 *       status is {@code OPTIMAL} or {@code FEASIBLE}.</li>
 * </ul>
 *
 * <p>Mathematical promises, with z the objective value of a returned {@code Solution}. Promises 1 and 2 hold
 * for every status. Promises 3 to 7 are promises of the status {@code OPTIMAL}; this implementation solves the
 * model to proven optimality, so it returns {@code OPTIMAL} on every instance that has a feasible load, and
 * {@code INFEASIBLE} on every other one.
 * <ol>
 *   <li>Valid solution: whenever a {@code Solution} is returned, every quantity is a whole number of pallets,
 *       every committed pallet is loaded, both capacities are respected, and the reported revenue and totals
 *       match the load.</li>
 *   <li>No solution from an empty feasible set: if no load respects both capacities, no {@code Solution} is
 *       returned, and the status {@code INFEASIBLE} is returned only then.</li>
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
     * Returns the load with the most revenue, with a status saying what was established.
     *
     * @param instance the flight to load.
     * @return the status, and the load when the status is {@code OPTIMAL} or {@code FEASIBLE}.
     * @throws InvalidInstanceException if the instance is malformed.
     * @throws IllegalStateException    if the solver fails; a failure is an error, never a status.
     */
    public Result run(Instance instance) {
        checkInstance(instance);
        MPSolver solver = MPSolver.createSolver("SCIP");
        Map<String, MPVariable> pallets = buildModel(solver, instance);
        MPSolver.ResultStatus solverStatus = solver.solve();
        if (solverStatus == MPSolver.ResultStatus.OPTIMAL) {
            return new Result(Status.OPTIMAL, assembleSolution(instance, pallets));
        }
        if (solverStatus == MPSolver.ResultStatus.FEASIBLE) {
            return new Result(Status.FEASIBLE, assembleSolution(instance, pallets));
        }
        if (solverStatus == MPSolver.ResultStatus.INFEASIBLE) {
            return new Result(Status.INFEASIBLE, null);
        }
        if (solverStatus == MPSolver.ResultStatus.NOT_SOLVED) {
            return new Result(Status.NOT_FOUND, null);
        }
        throw new IllegalStateException("the solver failed with status " + solverStatus);
    }

    /** Throws InvalidInstanceException unless the instance respects what the contract requires. */
    private void checkInstance(Instance instance) {
        if (instance == null) {
            throw new InvalidInstanceException("instance must not be null");
        }
        if (instance.weightCapacity() < 0 || instance.volumeCapacity() < 0) {
            throw new InvalidInstanceException("capacities must not be negative");
        }
        Set<String> names = new HashSet<>();
        for (Product product : instance.products()) {
            if (!names.add(product.name())) {
                throw new InvalidInstanceException(product.name() + ": product names must be unique");
            }
            if (product.weight() <= 0 || product.volume() <= 0) {
                throw new InvalidInstanceException(product.name() + ": weight and volume must be positive");
            }
            if (product.revenue() < 0 || product.committedQuantity() < 0) {
                throw new InvalidInstanceException(
                        product.name() + ": revenue and committed quantity must not be negative");
            }
        }
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
