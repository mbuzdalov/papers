package ru.ifmo.eps.orq;

import java.util.*;
import ru.ifmo.eps.util.*;

public class TreeORQ extends ORQBuilder {
    /**
     * For 2D originally, and 0D after getting rid of first and last coordinate,
     * it is enough to track the minimum of index-1 coordinates of the added points.
     */
    private static class Tree0D extends OrthogonalRangeQuery {
        ArrayWrapper points;
        double minimum;

        public double getMin(double[] lowerBound) {
            return minimum;
        }

        public void init(ArrayWrapper points) {
            this.points = points;
            this.minimum = Double.POSITIVE_INFINITY;
        }

        public void add(int index) {
            minimum = Math.min(minimum, points.get(index, 1));
        }

        public void clear() {
            this.points = null;
        }
    }

    /**
     * For 3D originally, and 1D after getting rid of first and last coordinates,
     * we need to support 1D minimum-on-ray queries, and all coordinates are already known.
     * It is feasible and good enough to use a Fenwick tree.
     */
    private static class Tree1D extends OrthogonalRangeQuery {
        ArrayWrapper points;
        int[] indexToInternal;
        int[] swap;
        double[] internal;
        double[] dswap;
        double[] fenwick;
        int internalSize;

        public void init(ArrayWrapper points) {
            this.points = points;
            int size = points.size();
            if (indexToInternal == null || indexToInternal.length < size) {
                indexToInternal = new int[size];
                swap = new int[size];
                dswap = new double[size];
                fenwick = new double[size];
                internal = new double[size];
            }
            // Sorting coordinates
            for (int i = 0; i < size; ++i) {
                internal[i] = points.get(i, 1);
                indexToInternal[i] = i;
            }
            mergeSort(0, size);
            // indexToInternal contains an inverse of what it should. Fixing.
            System.arraycopy(indexToInternal, 0, swap, 0, size);
            for (int i = 0; i < size; ++i) {
                indexToInternal[swap[i]] = i;
            }
            // Evaluating new indices after compression. swap will contain remapping.
            internalSize = 1;
            swap[0] = 0;
            for (int i = 1; i < size; ++i) {
                if (internal[i] == internal[internalSize - 1]) {
                    swap[i] = internalSize - 1;
                } else {
                    internal[internalSize] = internal[i];
                    swap[i] = internalSize;
                    ++internalSize;
                }
            }
            // Fixing indexToInternal again
            for (int i = 0; i < size; ++i) {
                indexToInternal[i] = swap[indexToInternal[i]];
            }
            // Initially, Fenwick tree contains infinities.
            Arrays.fill(fenwick, 0, internalSize, Double.POSITIVE_INFINITY);
        }

        public void clear() {
            points = null;
        }

        public void add(int index) {
            double value = points.get(index, 2);
            int fwi = internalSize - 1 - indexToInternal[index];
            while (fwi < internalSize) {
                fenwick[fwi] = Math.min(fenwick[fwi], value);
                fwi |= fwi + 1;
            }
        }

        public double getMin(double[] lowerBound) {
            double coord = lowerBound[1];
            // First, find the internal element which is the closest from above to the query
            int left = 0, right = internalSize;
            if (internal[left] >= coord) {
                right = left;
            }
            while (right - left > 1) {
                int mid = (left + right) >>> 1;
                if (internal[mid] >= coord) {
                    right = mid;
                } else {
                    left = mid;
                }
            }
            int fwi = internalSize - 1 - right;
            // Second, run the Fenwick tree query.
            if (right == internalSize) {
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

        private void mergeSort(int from, int until) {
            if (from + 1 < until) {
                int mid = (from + until) >>> 1;
                mergeSort(from, mid);
                mergeSort(mid, until);
                for (int lp = from, rp = mid, t = from; t < until; ++t) {
                    if (rp == until || lp < mid && internal[lp] <= internal[rp]) {
                        dswap[t] = internal[lp];
                        swap[t] = indexToInternal[lp];
                        ++lp;
                    } else {
                        dswap[t] = internal[rp];
                        swap[t] = indexToInternal[rp];
                        ++rp;
                    }
                }
                System.arraycopy(dswap, from, internal, from, until - from);
                System.arraycopy(swap, from, indexToInternal, from, until - from);
            }
        }
    }

    public OrthogonalRangeQuery build(int internalDimension) {
        switch (internalDimension) {
            case 0:  return new Tree0D();
            case 1:  return new Tree1D();
            default: return NaiveORQ.INSTANCE.build(internalDimension);
        }
    }

    public String getName() {
        return "TreeORQ";
    }

    public static ORQBuilder INSTANCE = new TreeORQ();
}
