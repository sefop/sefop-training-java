package oracles;

import java.util.List;

/**
 * One flight to load.
 *
 * @param products       the booking list.
 * @param weightCapacity the most weight the aircraft may carry (the payload capacity).
 * @param volumeCapacity the most volume the hold takes (the hold capacity).
 */
public record Instance(List<Product> products, double weightCapacity, double volumeCapacity) {
}
