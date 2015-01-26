package ru.ifmo.steady;

public class Solution {
    public static long comparisons = 0;

    private final double x, y;
    private final double[] input;

    public Solution(double x, double y, double[] input) {
        this.x = Math.abs(x) < 1e-100 ? 0 : x;
        this.y = Math.abs(y) < 1e-100 ? 0 : y;
        this.input = input;
    }

    public Solution(double x, double y) {
        this.x = Math.abs(x) < 1e-100 ? 0 : x;
        this.y = Math.abs(y) < 1e-100 ? 0 : y;
        this.input = null;
    }

    public double crowdingDistance(Solution left, Solution right) {
        // Unsure if comparisons should be ++ed
        if (left == null || right == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            return Math.abs(left.x - right.x) * Math.abs(left.y - right.y);
        }
    }

    public double[] getInput() {
        return input;
    }

    public double getNormalizedX(double minX, double maxX) {
        // this doesn't count in comparisons as it is for hypervolume only
        return (x - minX) / (maxX - minX);
    }

    public double getNormalizedY(double minY, double maxY) {
        // this doesn't count in comparisons as it is for hypervolume only
        return (y - minY) / (maxY - minY);
    }

    public int compareX(Solution that) {
        ++comparisons;
        return Double.compare(x, that.x);
    }

    public int compareY(Solution that) {
        ++comparisons;
        return Double.compare(y, that.y);
    }

    public int hashCode() {
        comparisons += 2;
        long xx = x == 0 ? 0 : Double.doubleToLongBits(x);
        long yy = y == 0 ? 0 : Double.doubleToLongBits(y);
        return (int) (xx ^ (xx >>> 32) ^ yy ^ (yy >>> 32));
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (o.getClass() == Solution.class) {
            Solution that = (Solution) (o);
            comparisons += 2;
            return x == that.x && y == that.y;
        } else {
            return false;
        }
    }

    public String toString() {
        return "(" + x + "; " + y + ")";
    }
}
