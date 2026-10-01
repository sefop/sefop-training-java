package oracles;

/**
 * One product on the booking list.
 *
 * @param name              identifies the product; unique within an instance.
 * @param weight            the weight of one pallet, positive.
 * @param volume            the volume of one pallet, positive.
 * @param revenue           the revenue one pallet earns, zero or more.
 * @param committedQuantity how many pallets must fly.
 */
public record Product(String name, double weight, double volume, double revenue, int committedQuantity) {

    /** Creates a product with no committed pallets. */
    public Product(String name, double weight, double volume, double revenue) {
        this(name, weight, volume, revenue, 0);
    }
}
