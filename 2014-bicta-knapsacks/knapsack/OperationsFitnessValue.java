package knapsack;

/**
 * This is a fitness value for evolutionary algorithms for knapsack problem.
 *
 * @author Maxim Buzdalov
 */
public final class OperationsFitnessValue implements Comparable<OperationsFitnessValue> {
    public final long operationCount;
    public final long timeConsumed;
    public final String comment;

    public OperationsFitnessValue(long operationCount, long timeConsumed, String comment) {
        this.operationCount = operationCount;
        this.timeConsumed = timeConsumed;
        this.comment = comment;
    }

    @Override
    public int compareTo(OperationsFitnessValue o) {
        return compareLongs(operationCount, o.operationCount);
    }

    @Override
    public String toString() {
        return "(" + operationCount + " ops, " + timeConsumed + " ms)";
    }

    private static int compareLongs(long a, long b) {
        return a == b ? 0 : a < b ? -1 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationsFitnessValue that = (OperationsFitnessValue) o;
        return operationCount == that.operationCount;
    }

    @Override
    public int hashCode() {
        return (int) (operationCount ^ (operationCount >>> 32));
    }
}
