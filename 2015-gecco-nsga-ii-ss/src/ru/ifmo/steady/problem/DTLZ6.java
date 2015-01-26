package ru.ifmo.steady.problem;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public class DTLZ6 implements Problem {
    private static final Problem instance = new DTLZ6();
    public static Problem instance() { return instance; }

    public double frontMinX() { return 0; }
    public double frontMaxX() { return 1; }
    public double frontMinY() { return 0; }
    public double frontMaxY() { return 1; }

    public int inputDimension() { return 11; }
    public String getName() { return "DTLZ6"; }

    private double g(double[] input, int start) {
        double sum = 0;
        for (int i = start, last = input.length; i < last; ++i) {
            sum += Math.pow(input[i], 0.1);
        }
        return sum;
    }

    public Solution evaluate(double[] input) {
        double gm = g(input, 1);
        double a = Math.PI / 2 * input[0];
        return new Solution(
            (1 + gm) * Math.cos(a),
            (1 + gm) * Math.sin(a),
            input
        );
    }
}
