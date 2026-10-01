package oracles;

import java.util.Map;

/**
 * The load that {@link Optimization#run(Instance)} found.
 *
 * @param picked         product name to whole number of pallets, 0 when the product is left behind.
 * @param objectiveValue the revenue of the load.
 * @param totalWeight    the weight of the load.
 * @param totalVolume    the volume of the load.
 */
public record Solution(Map<String, Integer> picked, double objectiveValue, double totalWeight, double totalVolume) {
}
