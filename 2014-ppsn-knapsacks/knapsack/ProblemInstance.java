package knapsack;

import java.util.Collections;
import java.util.List;

/**
 * An instance of knapsack problem.
 * @author Maxim Buzdalov
 */
public final class ProblemInstance {
    private final List<Item> items;
    private final int capacity;
    private static final int MAX = 10000;

    /**
     * Creates an instance of knapsack problem with the specified items.
     * The capacity is chosen as half the sum of weights of items.
     * @param items the items.
     */
    public ProblemInstance(List<Item> items) {
        this.items = items;
        int sum = 0;
        for (Item i : items) {
            if (i.value <= 0 || i.weight <= 0 || i.value > MAX || i.weight > MAX) {
                throw new IllegalArgumentException("Problem limits exceeded");
            }
            sum += i.weight;
        }
        this.capacity = sum / 2;
    }

    /**
     * Creates an instance of knapsack problem with the specified items and capacity.
     * @param items the items.
     * @param capacity the capacity.
     */
    public ProblemInstance(List<Item> items, int capacity) {
        this.items = items;
        for (Item i : items) {
            if (i.value <= 0 || i.weight <= 0 || i.value > MAX || i.weight > MAX) {
                throw new IllegalArgumentException("Problem limits exceeded");
            }
        }
        this.capacity = capacity;
    }

    /**
     * Returns the list of items.
     * @return the list of items.
     */
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Returns the capacity of the knapsack.
     * @return the capacity of the knapsack.
     */
    public int getCapacity() {
        return capacity;
    }
}
