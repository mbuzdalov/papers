package knapsack;

/**
 * An item for knapsack problem (a variation with calculation in ints).
 *
 * @author Maxim Buzdalov
 */
public final class Item {
    public final int weight;
    public final int value;

    public Item(int weight, int value) {
        this.weight = weight;
        this.value = value;
    }

    public String toString() {
        return "<w = " + weight + ", v = " + value + ">";
    }
}
