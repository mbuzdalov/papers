package ru.ifmo.steady.problem;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public class DTLZ6 implements Problem {
    public double frontMinX() { return 0; }
    public double frontMaxX() { return 1; }
    public double frontMinY() { return 0; }
    public double frontMaxY() { return 1; }

    public int inputDimension() { return 11; }

    private double g(double[] input, int start) {
        double sum = 0;
        for (int i = start, last = input.length; i < last; ++i) {
            sum += Math.pow(input[i], 0.1);
        }
        return sum;
    }

    public Solution evaluate(double[] input) {
        double gm = g(input, 1);
        double a = Math.PI / 2 * Math.PI * (1 + 2 * gm * input[0]) / (4 * (1 + gm));
        return new Solution(
            (1 + gm) * Math.cos(a),
            (1 + gm) * Math.sin(a),
            input
        );
    }
}
