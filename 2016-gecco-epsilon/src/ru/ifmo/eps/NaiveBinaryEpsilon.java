package ru.ifmo.eps;

public class NaiveBinaryEpsilon extends BinaryEpsilon {
    @Override
    protected double computeBinaryEpsilonImpl(double[][] movingSet, double[][] fixedSet) {
        int dimension = movingSet[0].length;
        double rv = Double.NEGATIVE_INFINITY;
        for (double[] fixedPoint : fixedSet) {
            double minEpsilon = Double.POSITIVE_INFINITY;
            for (double[] movingPoint : movingSet) {
                double epsilon = Double.NEGATIVE_INFINITY;
                for (int i = 0; i < dimension; ++i) {
                    epsilon = Math.max(epsilon, movingPoint[i] - fixedPoint[i]);
                }
                minEpsilon = Math.min(minEpsilon, epsilon);
            }
            rv = Math.max(rv, minEpsilon);
        }
        return rv;
    }
}
