package ru.ifmo.steady;

public interface SolutionStorage {
	public void add(Solution solution);
	public QueryResult getRandom();
	public int size();
	public Solution removeWorst();

	public static class QueryResult {
		public final Solution solution;
		public final double crowdingDistance;
		public final int layer;

		public QueryResult(Solution solution, double crowdingDistance, int layer) {
			this.solution = solution;
			this.crowdingDistance = crowdingDistance;
			this.layer = layer;
		}
	}
}
