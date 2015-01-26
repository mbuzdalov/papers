package ru.ifmo.steady.problem;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public class DTLZ7 implements Problem {
    private static final Problem instance = new DTLZ7();
    public static Problem instance() { return instance; }

    public double frontMinX() { return 0; }
    public double frontMaxX() { return 1; }
    public double frontMinY() { return 0; }
    public double frontMaxY() { return 4; }

    public int inputDimension() { return 21; }
    public String getName() { return "DTLZ7"; }

    private double g(double[] input, int first) {
        int last = input.length;
        double sum = 0;
        for (int i = first; i < last; ++i) {
            sum += input[i];
        }
        return 1 + 9.0 / (last - first) * sum;
    }

    public Solution evaluate(double[] input) {
        double gm = g(input, 1);
        double h = 2 - input[0] / (1 + gm) * (1 + Math.sin(3 * Math.PI * input[0]));
        return new Solution(
            input[0],
            (1 + gm) * h,
            input
        );
    }
}
