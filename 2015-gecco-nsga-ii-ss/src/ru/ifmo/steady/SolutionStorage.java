package ru.ifmo.steady;

public interface SolutionStorage {
    public void add(Solution solution);
    public QueryResult getRandom();
    public int size();
    public Solution removeWorst();
    public void clear();

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
