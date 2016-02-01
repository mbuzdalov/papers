package ru.ifmo.eps;

import java.util.*;

import ru.ifmo.eps.orq.*;
import ru.ifmo.eps.util.*;

public class ORQ2BinaryEpsilon extends BinaryEpsilon {
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

    private static class ArrayWrapper2 extends ArrayWrapper {
        double[] bound;
        double[] medianSwap;

        int fenwickSize;
        double[] fenwick;
        double[] fwPivots;

        int numberOfQueries;

        public ArrayWrapper2(double[][] contents, int numberOfQueries) {
            super(contents, 0, contents[0].length - 2);
            this.numberOfQueries = numberOfQueries;
            bound = new double[contents.length];
            medianSwap = new double[contents.length];
            fenwick = new double[contents.length];
            fwPivots = new double[contents.length];
            Arrays.fill(bound, Double.POSITIVE_INFINITY);
        }

        public void reloadContents() {
            super.reloadContents();
            Arrays.fill(bound, Double.POSITIVE_INFINITY);
        }

        public double getBound(int index) {
            return bound[idx[index]];
        }

        public void run() {
            if (dimension == 2) {
                double minimum = Double.POSITIVE_INFINITY;
                for (int i = contents.length - 1; i >= 0; --i) {
                    bound[idx[i]] = Math.min(bound[idx[i]], minimum);
                    if (idx[i] >= numberOfQueries) {
                        minimum = Math.min(minimum, get(i, 1));
                    }
                }
            } else {
                // Simulating split into queries and non-queries
                int l = 0, r = contents.length;
                for (int i = 0; i < contents.length; ++i) {
                    if (idx[i] < numberOfQueries) {
                        swp[l++] = idx[i];
                    } else {
                        swp[--r] = idx[i];
                    }
                }
                for (int li = r, ri = contents.length - 1; li < ri; ++li, --ri) {
                    int tmp = swp[li];
                    swp[li] = swp[ri];
                    swp[ri] = tmp;
                }
                System.arraycopy(swp, 0, idx, 0, contents.length);
                // Running recursion
                updateLeftByRight(0, numberOfQueries, numberOfQueries, contents.length, dimension - 2);
            }
        }

        private void update(int target, int source) {
            target = idx[target];
            source = idx[source];
            bound[target] = Math.min(bound[target], Math.min(contents[source][dimension - 1], bound[source]));
        }

        private void conditionallyUpdate(int target, int source, int d) {
            double[] t = get(target), s = get(source);
            for (int i = 0; i <= d; ++i) {
                if (t[i] > s[i]) {
                    return;
                }
            }
            update(target, source);
        }

        public void updateLeftByRight(int minLeft, int maxLeft, int minRight, int maxRight, int d) {
            if (minLeft == maxLeft || minRight == maxRight) {
                return;
            }
            if (minLeft + 1 == maxLeft || minRight + 1 == maxRight) {
                for (int l = minLeft; l < maxLeft; ++l) {
                    for (int r = minRight; r < maxRight; ++r) {
                        conditionallyUpdate(l, r, d);
                    }
                }
            } else if (d == 1) {
                updateLeftByRight2D(minLeft, maxLeft, minRight, maxRight);
            } else {
                double minMax = Double.NEGATIVE_INFINITY;
                double maxMin = Double.POSITIVE_INFINITY;
                int mc = 0;
                for (int l = minLeft; l < maxLeft; ++l, ++mc) {
                    medianSwap[mc] = get(l, d);
                    minMax = Math.max(minMax, medianSwap[mc]);
                }
                for (int r = minRight; r < maxRight; ++r, ++mc) {
                    medianSwap[mc] = get(r, d);
                    maxMin = Math.min(maxMin, medianSwap[mc]);
                }
                if (minMax <= maxMin) {
                    updateLeftByRight(minLeft, maxLeft, minRight, maxRight, d - 1);
                } else {
                    double median = Miscellaneous.destructiveMedian(medianSwap, 0, mc);
                    split(minLeft, maxLeft, median, d);
                    int midMinLeft = splitL, midMaxLeft = splitR;
                    split(minRight, maxRight, median, d);
                    int midMinRight = splitL, midMaxRight = splitR;

                    updateLeftByRight(midMaxLeft, maxLeft, midMaxRight, maxRight, d);
                    updateLeftByRight(minLeft, midMinLeft, minRight, midMinRight, d);
                    merge(midMinRight, midMaxRight, maxRight);
                    merge(minLeft, midMinLeft, midMaxLeft);
                    updateLeftByRight(minLeft, midMaxLeft, midMinRight, maxRight, d - 1);
                    merge(minRight, midMinRight, maxRight);
                    merge(minLeft, midMaxLeft, maxLeft);
                }
            }
        }

        private void buildFenwick(int from, int until) {
            int inSize = until - from;
            for (int i = 0; i < inSize; ++i) {
                fwPivots[i] = get(from + i, 1);
            }
            Arrays.sort(fwPivots, 0, inSize);
            fenwickSize = 1;
            for (int i = 1; i < inSize; ++i) {
                if (fwPivots[i] != fwPivots[fenwickSize - 1]) {
                    fwPivots[fenwickSize++] = fwPivots[i];
                }
            }
            Arrays.fill(fenwick, 0, fenwickSize, Double.POSITIVE_INFINITY);
        }

        private int indexFenwick(double key) {
            int left = -1, right = fenwickSize;
            while (right - left > 1) {
                int mid = (left + right) >>> 1;
                if (fwPivots[mid] >= key) {
                    right = mid;
                } else {
                    left = mid;
                }
            }
            return fenwickSize - 1 - right;
        }

        private void setFenwick(double key, double value) {
            int fwi = indexFenwick(key);
            while (fwi < fenwickSize) {
                fenwick[fwi] = Math.min(fenwick[fwi], value);
                fwi |= fwi + 1;
            }
        }

        private double queryFenwick(double key) {
            int fwi = indexFenwick(key);
            if (fwi == -1) {
                return Double.POSITIVE_INFINITY;
            } else {
                double rv = Double.POSITIVE_INFINITY;
                while (fwi >= 0) {
                    rv = Math.min(rv, fenwick[fwi]);
                    fwi = (fwi & (fwi + 1)) - 1;
                }
                return rv;
            }
        }

        public void updateLeftByRight2D(int minLeft, int maxLeft, int minRight, int maxRight) {
            buildFenwick(minRight, maxRight);
            for (int li = maxLeft - 1, ri = maxRight - 1; li >= minLeft; --li) {
                while (ri >= minRight && get(ri, 0) >= get(li, 0)) {
                    setFenwick(get(ri, 1), get(ri, dimension - 1));
                    --ri;
                }
                double qf = queryFenwick(get(li, 1));
                bound[idx[li]] = Math.min(bound[idx[li]], qf);
            }
        }
    }

    @Override
    protected double computeBinaryEpsilonImpl(double[][] movingSet, double[][] fixedSet) {
        int d = movingSet[0].length;
        double[][] joinedSet = new double[movingSet.length + fixedSet.length][d];
        double[] bounds = new double[fixedSet.length];
        Arrays.fill(bounds, Double.POSITIVE_INFINITY);

        ArrayWrapper2 wrapper = null;

        for (int k = 0; k < d; ++k) {
            for (int i = 0, ii = fixedSet.length; i < ii; ++i) {
                encode(fixedSet[i], k, joinedSet[i]);
            }
            for (int i = 0, ii = movingSet.length; i < ii; ++i) {
                encode(movingSet[i], k, joinedSet[i + fixedSet.length]);
            }

            if (wrapper == null) {
                wrapper = new ArrayWrapper2(joinedSet, fixedSet.length);
            } else {
                wrapper.reloadContents();
            }

            wrapper.run();

            for (int i = 0; i < fixedSet.length; ++i) {
                bounds[i] = Math.min(bounds[i], wrapper.bound[i] - joinedSet[i][d - 1]);
            }
        }

        double rv = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < fixedSet.length; ++i) {
            rv = Math.max(rv, bounds[i]);
        }
        return rv;
    }

    @Override
    public String getName() {
        return "ORQ2BinaryEpsilon";
    }
}
