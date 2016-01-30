package ru.ifmo.eps;

import java.util.*;

import ru.ifmo.eps.orq.*;
import ru.ifmo.eps.util.*;

public class ORQBinaryEpsilon extends BinaryEpsilon {
    private ORQBuilder builder;

    public ORQBinaryEpsilon(ORQBuilder builder) {
        this.builder = builder;
    }

    // Translates a point (x_1, x_2, ..., x_d) to (x_k - x_1, ..., x_k - x_d, x_k)
    private static void encode(double[] src, int k, double[] trg) {
        int d = src.length;
        double xk = src[k];
        for (int i = 0; i < k; ++i) {
            trg[i] = xk - src[i];
        }
        for (int i = k + 1; i < d; ++i) {
            trg[i - 1] = xk - src[i];
        }
        trg[d - 1] = xk;
    }

    @Override
    protected double computeBinaryEpsilonImpl(double[][] movingSet0, double[][] fixedSet0) {
        int d = movingSet0[0].length;
        double[][] movingSet = new double[movingSet0.length][d];
        double[][] fixedSet = new double[fixedSet0.length][d];

        double[] upperBounds = new double[fixedSet.length];
        Arrays.fill(upperBounds, Double.POSITIVE_INFINITY);

        ArrayWrapper movingW = null;
        ArrayWrapper fixedW = null;

        OrthogonalRangeQuery driver = builder.build(d - 2);

        for (int k = 0; k < d; ++k) {
            for (int i = 0, ii = movingSet.length; i < ii; ++i) {
                encode(movingSet0[i], k, movingSet[i]);
            }
            for (int i = 0, ii = fixedSet.length; i < ii; ++i) {
                encode(fixedSet0[i], k, fixedSet[i]);
            }

            if (movingW == null) {
                movingW = new ArrayWrapper(movingSet);
                fixedW = new ArrayWrapper(fixedSet);
            } else {
                movingW.reloadContents();
                fixedW.reloadContents();
            }

            driver.init(movingW);

            int mp = movingW.size() - 1;

            for (int fp = fixedW.size() - 1; fp >= 0; --fp) {
                int fi = fixedW.getIndex(fp);
                double[] fs = fixedW.get(fp);
                while (mp >= 0 && lexCompare(movingW.get(mp), fs, d - 1) >= 0) {
                    driver.add(movingW.get(mp--));
                }
                double boundUpdate = driver.getMin(fs) - fs[d - 1];
                upperBounds[fi] = Math.min(upperBounds[fi], boundUpdate);
            }

            driver.clear();
        }

        double rv = upperBounds[0];
        for (int i = 1; i < upperBounds.length; ++i) {
            rv = Math.max(rv, upperBounds[i]);
        }
        return rv;
    }

    private static int lexCompare(double[] lhs, double[] rhs, int limit) {
        for (int i = 0; i < limit; ++i) {
            if (lhs[i] != rhs[i]) {
                return lhs[i] < rhs[i] ? -1 : 1;
            }
        }
        return 0;
    }

    @Override
    public String getName() {
        return "ORQBinaryEpsilon(" + builder.getName() + ")";
    }
}
