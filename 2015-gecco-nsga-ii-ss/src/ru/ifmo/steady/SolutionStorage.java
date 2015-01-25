package ru.ifmo.steady;

import java.util.Iterator;

public interface SolutionStorage {
    public void add(Solution solution);
    public QueryResult getRandom();
    public int size();
    public Solution removeWorst();
    public void clear();
    public Iterator<Solution> nonDominatedSolutionsIncreasingX();
    public String getName();

    public default double hyperVolume(double minX, double maxX, double minY, double maxY) {
        Iterator<Solution> front = nonDominatedSolutionsIncreasingX();
        double hv = 0;
        double lastX = 0, lastY = 1;
        while (front.hasNext()) {
            Solution s = front.next();
            double currX = s.getNormalizedX(minX, maxX);
            double currY = s.getNormalizedY(minY, maxY);
            if (0 <= currX && currX <= 1 && 0 <= currY && currY <= 1) {
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
