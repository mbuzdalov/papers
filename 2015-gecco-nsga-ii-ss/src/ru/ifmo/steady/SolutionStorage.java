package ru.ifmo.steady;

import java.util.Collections;
import java.util.Iterator;

public interface SolutionStorage {
    public void add(Solution solution);
    public QueryResult getRandom();
    public QueryResult getKth(int index);
    public int size();
    public Solution removeWorst();
    public void clear();
    public String getName();
    public void removeWorstDebCompatible(int count);
    public int getLayerCount();
    public Iterator<Solution> getLayer(int index);

    public default Iterator<Solution> nonDominatedSolutionsIncreasingX() {
        if (getLayerCount() == 0) {
            return Collections.emptyIterator();
        } else {
            return getLayer(0);
        }
    }

    public default void addAll(Solution... solutions) {
        for (Solution s : solutions) {
            add(s);
        }
    }

    public default void removeWorst(int count) {
        for (int i = 0; i < count; ++i) {
            removeWorst();
        }
    }

    public default double hyperVolume(double minX, double maxX, double minY, double maxY) {
        Iterator<Solution> front = nonDominatedSolutionsIncreasingX();
        double hv = 0;
        double lastX = 0, lastY = 1;
        while (front.hasNext()) {
            Solution s = front.next();
            double currX = s.getNormalizedX(minX, maxX);
            double currY = s.getNormalizedY(minY, maxY);
            if (0 <= currX && currX <= 1 && 0 <= currY && currY <= 1) {
                if (currX < lastX || currY > lastY) {
                    throw new AssertionError();
                }
                hv += (lastY - currY) * (1 - currX);
                lastX = currX;
                lastY = currY;
            }
        }
        return hv;
    }

    public static class QueryResult {
        public final Solution solution;
        public final double crowdingDistance;
        public final int layer;

        public QueryResult(Solution solution, double crowdingDistance, int layer) {
            this.solution = solution;
            this.crowdingDistance = crowdingDistance;
            this.layer = layer;
        }

        @Override
        public int hashCode() {
            int rv = solution.hashCode();
            long dbl = Double.doubleToLongBits(crowdingDistance);
            rv = 31 * rv + layer;
            rv = 31 * rv + (int) (dbl);
            rv = 31 * rv + (int) (dbl >>> 32);
            return rv;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (this == o) {
                return true;
            }
            if (o.getClass() == QueryResult.class) {
                QueryResult that = (QueryResult) (o);
                return solution.equals(that.solution)
                        && layer == that.layer
                        && crowdingDistance == that.crowdingDistance;
            } else {
                return false;
            }
        }
    }
}
