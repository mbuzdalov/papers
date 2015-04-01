package ru.ifmo.steady;

public class Solution {
    private static final boolean USE_EXPENSIVE_CHECKING = false;

    private final double x, y;
    private final double[] input;

    public Solution(double x, double y, double[] input) {
        this.x = Math.abs(x) < 1e-100 ? 0 : x;
        this.y = Math.abs(y) < 1e-100 ? 0 : y;
        this.input = USE_EXPENSIVE_CHECKING ? input.clone() : input;
    }

    public Solution(double x, double y) {
        this.x = Math.abs(x) < 1e-100 ? 0 : x;
        this.y = Math.abs(y) < 1e-100 ? 0 : y;
        this.input = null;
    }

    public double crowdingDistanceADX(Solution left, Solution right, ComparisonCounter cnt) {
        cnt.add(2);
        return left == null || right == null ? Double.POSITIVE_INFINITY : Math.abs(right.x - left.x);
    }

    public double crowdingDistanceADY(Solution left, Solution right, ComparisonCounter cnt) {
        cnt.add(2);
        return left == null || right == null ? Double.POSITIVE_INFINITY : Math.abs(right.y - left.y);
    }

    public double crowdingDistance(Solution left, Solution right, Solution leftmost, Solution rightmost, ComparisonCounter cnt) {
        cnt.add(4);
        double diffx = rightmost.x - leftmost.x;
        double diffy = leftmost.y - rightmost.y;
        if (USE_EXPENSIVE_CHECKING) {
            if (diffx < 0 || diffy < 0) {
                throw new AssertionError();
            }
        }
        if (left == null || right == null || diffx == 0 || diffy == 0) {
            return Double.POSITIVE_INFINITY;
        } else {
            if (USE_EXPENSIVE_CHECKING) {
                if (right.x < left.x || left.y < right.y) {
                    throw new AssertionError();
                }
                if ((right.x - left.x) > diffx || (left.y - right.y) > diffy) {
                    throw new AssertionError();
                }
            }
            return (right.x - left.x) / diffx
                 + (left.y - right.y) / diffy;
        }
    }

    public double[] getInput() {
        return USE_EXPENSIVE_CHECKING ? input.clone() : input;
    }

    public double getNormalizedX(double minX, double maxX) {
        // this doesn't count in comparisons as it is for hypervolume only
        return (x - minX) / (maxX - minX);
    }

    public double getNormalizedY(double minY, double maxY) {
        // this doesn't count in comparisons as it is for hypervolume only
        return (y - minY) / (maxY - minY);
    }

    public int compareX(Solution that, ComparisonCounter cnt) {
        cnt.add(1);
        return Double.compare(x, that.x);
    }

    public int compareY(Solution that, ComparisonCounter cnt) {
        cnt.add(1);
        return Double.compare(y, that.y);
    }

    public int hashCode() {
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
            return x == that.x && y == that.y;
        } else {
            return false;
        }
    }

    public String toString() {
        return "(" + x + "; " + y + ")";
    }
}
