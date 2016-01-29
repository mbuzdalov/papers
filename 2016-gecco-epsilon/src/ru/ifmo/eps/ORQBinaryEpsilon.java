package ru.ifmo.eps;

import java.util.*;

import ru.ifmo.eps.orq.*;

public class ORQBinaryEpsilon extends BinaryEpsilon {
    private OrthogonalRangeQuery driver;

    public ORQBinaryEpsilon(OrthogonalRangeQuery driver) {
        this.driver = driver;
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

        int[] movingIdx = new int[movingSet.length];
        int[] fixedIdx = new int[fixedSet.length];

        for (int k = 0; k < d; ++k) {
            for (int i = 0, ii = movingSet.length; i < ii; ++i) {
                encode(movingSet0[i], k, movingSet[i]);
            }
            for (int i = 0, ii = fixedSet.length; i < ii; ++i) {
                encode(fixedSet0[i], k, fixedSet[i]);
            }

            new ArrayWrapper(movingSet, movingIdx);
            new ArrayWrapper(fixedSet, fixedIdx);

            driver.init(movingSet);

            int mp = movingIdx.length - 1;

            for (int fp = fixedIdx.length - 1; fp >= 0; --fp) {
                int fi = fixedIdx[fp];
                while (mp >= 0 && lexCompare(movingSet[movingIdx[mp]], fixedSet[fi], d - 1) >= 0) {
                    driver.add(movingIdx[mp--]);
                }
                upperBounds[fi] = Math.min(upperBounds[fi], driver.getMin(fixedSet[fi]) - fixedSet[fi][d - 1]);
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

    private static class ArrayWrapper {
        double[][] contents;
        int[] idx;
        int[] swp;
        int dimension;

        public ArrayWrapper(double[][] contents, int[] idx) {
            this.contents = contents;
            this.idx = idx;
            this.dimension = contents[0].length;
            this.swp = new int[idx.length];
            for (int i = 0; i < idx.length; ++i) {
                idx[i] = i;
            }
            lexSort(0, contents.length, 0);
        }

        private void lexSort(int left, int right, int k) {
            mergeSort(left, right, k);
            if (k + 1 < dimension) {
                int prev = left;
                for (int i = left + 1; i < right; ++i) {
                    if (contents[idx[i - 1]][k] < contents[idx[i]][k]) {
                        lexSort(prev, i, k + 1);
                        prev = i;
                    }
                }
                lexSort(prev, right, k + 1);
            }
        }

        private void mergeSort(int left, int right, int k) {
            if (left + 1 < right) {
                int mid = (left + right) >>> 1;
                mergeSort(left, mid, k);
                mergeSort(mid, right, k);
                for (int i = left, j = mid, t = left; t < right; ++t) {
                    if (i == mid || j < right && contents[idx[j]][k] <= contents[idx[i]][k]) {
                        swp[t] = idx[j++];
                    } else {
                        swp[t] = idx[i++];
                    }
                }
                System.arraycopy(swp, left, idx, left, right - left);
            }
        }
    }

    @Override
    public String getName() {
        return "ORQBinaryEpsilon(" + driver.getName() + ")";
    }
}
