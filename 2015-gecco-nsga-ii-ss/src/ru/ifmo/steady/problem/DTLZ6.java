package ru.ifmo.steady.problem;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public class DTLZ6 implements Problem {
    private double g(double[] input, int start) {
        double sum = 0;
        for (int i = start, last = input.length; i < last; ++i) {
            sum += Math.pow(input[i], 0.1);
        }
        return sum;
    }

    public int inputDimension() {
        return 11;
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
