package ru.ifmo.eps;

import java.util.Objects;

public abstract class BinaryEpsilon {
    public final double computeBinaryEpsilon(double[][] movingSet, double[][] fixedSet) {
        Objects.requireNonNull(movingSet, "Moving set must not be null");
        Objects.requireNonNull(fixedSet,  "Fixed set must not be null");

        if (fixedSet.length == 0) {
            return Double.NEGATIVE_INFINITY;
        }
        if (movingSet.length == 0) {
            return Double.POSITIVE_INFINITY;
        }

        for (double[] point : movingSet) {
            Objects.requireNonNull(point, "Points in moving set must not be null");
        }
        for (double[] point : fixedSet) {
            Objects.requireNonNull(point, "Points in fixed set must not be null");
        }
        int dimension = movingSet[0].length;
        checkNonNullSet(movingSet, dimension);
        checkNonNullSet(fixedSet, dimension);
        return computeBinaryEpsilonImpl(movingSet, fixedSet);
    }

    protected abstract double computeBinaryEpsilonImpl(double[][] movingSet, double[][] fixedSet);

    private static void checkNonNullSet(double[][] set, int dimension) {
        for (double[] point : set) {
            if (dimension != point.length) {
                throw new IllegalArgumentException("All points must have equal dimension");
            }
            for (double x : point) {
                if (Double.isNaN(x)) {
                    throw new IllegalArgumentException("All points must have non-NaN coordinates");
                }
            }
        }
    }
}
