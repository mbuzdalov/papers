package ru.ifmo.eps.orq;

import java.util.*;
import ru.ifmo.eps.util.*;

public class TreeORQ extends ORQBuilder {
    /**
     * For 2D originally, and 0D after getting rid of first and last coordinate,
     * it is enough to track the minimum of index-1 coordinates of the added points.
     */
    private static class Tree0D extends OrthogonalRangeQuery {
        double minimum;

        public double getMin(double[] lowerBound) {
            return minimum;
        }

        public void init(ArrayWrapper points) {
            this.minimum = Double.POSITIVE_INFINITY;
        }

        public void add(double[] point) {
            minimum = Math.min(minimum, point[1]);
        }

        public void clear() {}
    }

    private static abstract class Tree extends OrthogonalRangeQuery {
        public abstract void init(ArrayWrapper points, int left, int right, double[] medianSwap);
    }

    /**
     * For 3D originally, and 1D after getting rid of first and last coordinates,
     * we need to support 1D minimum-on-ray queries, and all coordinates are already known.
     * It is feasible and good enough to use a Fenwick tree.
     */
    private static class Tree1D extends Tree {
        double[] internal;
        double[] fenwick;
        int internalSize;

        public void init(ArrayWrapper points) {
            init(points, 0, points.size(), new double[points.size()]);
        }

        public void init(ArrayWrapper points, int left, int right, double[] medianSwap) {
            if (points.smallestMeaningfulCoordinate() != 1) {
                throw new IllegalArgumentException("ArrayWrapper should be initialized with smallestMeaningfulCoordinate = 1");
            }
            int size = right - left;
            if (fenwick == null || fenwick.length < size) {
                fenwick = new double[size];
                internal = new double[size];
            }
            // Sorting coordinates
            for (int i = 0; i < size; ++i) {
                internal[i] = points.get(left + i, 1);
            }
            internalSize = 1;
            for (int i = 1; i < size; ++i) {
                if (internal[i] != internal[internalSize - 1]) {
                    if (internal[i] < internal[internalSize - 1]) {
                        throw new AssertionError();
                    }
                    internal[internalSize] = internal[i];
                    ++internalSize;
                }
            }
            // Initially, Fenwick tree contains infinities.
            Arrays.fill(fenwick, 0, internalSize, Double.POSITIVE_INFINITY);
        }

        public void clear() {}

        private int findIndex(double coord) {
            int left = -1, right = internalSize;
            while (right - left > 1) {
                int mid = (left + right) >>> 1;
                if (internal[mid] >= coord) {
                    right = mid;
                } else {
                    left = mid;
                }
            }
            return right;
        }

        public void add(double[] point) {
            double value = point[point.length - 1];
            int fwi = internalSize - 1 - findIndex(point[1]);
            while (fwi < internalSize) {
                fenwick[fwi] = Math.min(fenwick[fwi], value);
                fwi |= fwi + 1;
            }
        }

        public double getMin(double[] lowerBound) {
            double coord = lowerBound[1];
            // First, find the internal element which is the closest from above to the query
            int right = findIndex(coord);
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
    }

    /**
     * A general-case X-dimensional tree.
     */
    private static class TreeXD extends Tree {
        int internalDimension;
        double minimum;
        double pivot;
        TreeXD leftChild, rightChild;
        Tree below;
        boolean empty = true;

        private TreeXD(int internalDimension) {
            this.internalDimension = internalDimension;
        }

        public void init(ArrayWrapper points) {
            init(points, 0, points.size(), new double[points.size()]);
        }

        public void init(ArrayWrapper points, int left, int right, double[] medianSwap) {
            empty = true;
            // First, build a below-tree for all the points
            if (below == null) {
                below = buildTree(internalDimension - 1);
            }
            below.init(points, left, right, medianSwap);
            // Find the minimum and the median
            minimum = Double.POSITIVE_INFINITY;
            for (int i = left; i < right; ++i) {
                medianSwap[i] = points.get(i, internalDimension);
                minimum = Math.min(minimum, medianSwap[i]);
            }
            pivot = Miscellaneous.destructiveMedian(medianSwap, left, right);
            // Split points into "less" and "equal" and "greater"
            points.split(left, right, pivot, internalDimension);
            int midLeft = points.splitL;
            int midRight = points.splitR;
            // Call build procedures recursively
            if (left < midLeft) {
                if (leftChild == null) {
                    leftChild = new TreeXD(internalDimension);
                }
                leftChild.init(points, left, midLeft, medianSwap);
            }
            // if "left" is empty, don't add "equal" to "greater"
            // otherwise do it.
            if (left != midLeft) {
                points.merge(midLeft, midRight, right);
                midRight = midLeft;
            }
            if (midRight < right) {
                if (rightChild == null) {
                    rightChild = new TreeXD(internalDimension);
                }
                rightChild.init(points, midRight, right, medianSwap);
            }
            // also processes the left == midLeft case correctly
            points.merge(left, midRight, right);
        }

        public void clear() {
            if (leftChild != null) leftChild.clear();
            if (rightChild != null) rightChild.clear();
            below.clear();
            empty = true;
        }

        public void add(double[] point) {
            empty = false;
            below.add(point);
            double coordinate = point[internalDimension];
            if (coordinate < pivot) {
                leftChild.add(point);
            } else {
                if (rightChild != null && coordinate > minimum) {
                    rightChild.add(point);
                }
            }
        }

        public double getMin(double[] lowerBound) {
            if (empty) {
                return Double.POSITIVE_INFINITY;
            }
            double coordinate = lowerBound[internalDimension];
            if (coordinate <= minimum) {
                return below.getMin(lowerBound);
            } else {
                double rv = rightChild == null ? Double.POSITIVE_INFINITY : rightChild.getMin(lowerBound);
                if (coordinate < pivot) {
                    rv = Math.min(rv, leftChild.getMin(lowerBound));
                }
                return rv;
            }
        }
    }

    private static Tree buildTree(int internalDimension) {
        switch (internalDimension) {
            case 0:  throw new AssertionError();
            case 1:  return new Tree1D();
            default: return new TreeXD(internalDimension);
        }
    }

    public OrthogonalRangeQuery build(int internalDimension) {
        switch (internalDimension) {
            case 0:  return new Tree0D();
            default: return buildTree(internalDimension);
        }
    }

    public String getName() {
        return "TreeORQ";
    }

    public static ORQBuilder INSTANCE = new TreeORQ();
}
