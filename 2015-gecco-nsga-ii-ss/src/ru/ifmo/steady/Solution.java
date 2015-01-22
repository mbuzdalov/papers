package ru.ifmo.steady;

public class Solution {
	public static long comparisons = 0;

	private final double x, y;

	public Solution(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double crowdingDistance(Solution left, Solution right) {
		// Unsure if comparisons should be ++ed
		if (left == null || right == null) {
			return Double.POSITIVE_INFINITY;
		} else {
			return Math.abs(left.x - right.x) * Math.abs(left.y - right.y);
		}
	}

	public int compareX(Solution that) {
		++comparisons;
		return Double.compare(x, that.x);
	}

	public int compareY(Solution that) {
		++comparisons;
		return Double.compare(y, that.y);
	}
}
