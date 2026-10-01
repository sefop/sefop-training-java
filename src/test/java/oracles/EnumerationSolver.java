package oracles;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A pseudo-oracle for the cargo model: a second, independent way to find the best load.
 *
 * <p>{@code EnumerationSolver} has the same contract as {@link Optimization#run(Instance)}, but no solver behind
 * it: it tries every candidate load and keeps the best. It is slow, so it only makes sense on small instances,
 * but each step can be checked by reading it. It lives with the tests because nothing in the program uses it.
 */
class EnumerationSolver {

    private Instance instance;
    private int[] counts;
    private Solution best;

    /**
     * Returns the load with the most revenue, or {@code null} when no load respects both capacities.
     *
     * <p>Same contract as {@link Optimization#run(Instance)}. Product i can load at most
     * m_i = floor(min(W / w_i, V / v_i)) pallets, and at least its committed pallets, so the candidate loads are
     * every combination of counts between those two bounds.
     *
     * @throws IllegalArgumentException if {@code instance} is null.
     */
    Solution run(Instance instance) {
        if (instance == null) {
            throw new IllegalArgumentException("instance must not be null");
        }
        this.instance = instance;
        this.counts = new int[instance.products().size()];
        this.best = null;
        tryCounts(0);
        return best;
    }

    /** Tries every count for product {@code index} onward, the earlier counts being fixed. */
    private void tryCounts(int index) {
        List<Product> products = instance.products();
        if (index == products.size()) {
            keepIfBetter();
            return;
        }
        Product product = products.get(index);
        int most = (int) Math.floor(Math.min(
                instance.weightCapacity() / product.weight(), instance.volumeCapacity() / product.volume()));
        for (int count = product.committedQuantity(); count <= most; count++) {
            counts[index] = count;
            tryCounts(index + 1);
        }
    }

    /** Keeps the current load when it is feasible and earns more than the best so far. */
    private void keepIfBetter() {
        List<Product> products = instance.products();
        double totalWeight = 0;
        double totalVolume = 0;
        double revenue = 0;
        Map<String, Integer> picked = new LinkedHashMap<>();
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            totalWeight += product.weight() * counts[i];
            totalVolume += product.volume() * counts[i];
            revenue += product.revenue() * counts[i];
            picked.put(product.name(), counts[i]);
        }
        if (totalWeight > instance.weightCapacity() || totalVolume > instance.volumeCapacity()) {
            return;
        }
        if (best == null || revenue > best.objectiveValue()) {
            best = new Solution(picked, revenue, totalWeight, totalVolume);
        }
    }
}
