package knapsack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a result of knapsack problem solution.
 * It includes the set of items chosen and the number of operations consumed.
 *
 * @author Maxim Buzdalov
 */
public final class KnapsackResultEx {
    public final List<Item> answer;
    public final long operationCount;
    public final int weight;
    public final int value;
    public final String comment;

    public KnapsackResultEx(List<Item> answer, long operationCount, String comment) {
        this.comment = comment;
        this.answer = Collections.unmodifiableList(new ArrayList<>(answer));
        this.operationCount = operationCount;
        int w = 0, v = 0;
        for (Item i : this.answer) {
            w += i.weight;
            v += i.value;
        }
        this.weight = w;
        this.value = v;
    }
}
