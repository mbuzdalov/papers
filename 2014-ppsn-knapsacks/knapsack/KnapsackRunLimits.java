package knapsack;

/**
 * This class contains limits for a run, such as the maximum number of items and time limit.
 * @author Maxim Buzdalov
 */
public class KnapsackRunLimits {
    public final int maxN;
    public final int maxWeight;
    public final int maxValue;
    public final int evaluationLimit;

    /**
     * Creates a new run limits object, using the following values.
     *
     * @param maxN          maximum possible number of items.
     * @param maxWeight     maximum possible weight of one item (weight start with 1).
     * @param maxValue      maximum possible value of one item (values start with 1).
     * @param evaluationLimit maximum possible number of fitness evaluations.
     */
    public KnapsackRunLimits(int maxN, int maxWeight, int maxValue, int evaluationLimit) {
        this.maxN = maxN;
        this.maxWeight = maxWeight;
        this.maxValue = maxValue;
        this.evaluationLimit = evaluationLimit;
    }
}
