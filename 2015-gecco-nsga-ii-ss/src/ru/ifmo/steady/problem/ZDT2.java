package ru.ifmo.steady.problem;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public class ZDT2 implements Problem {
    private static final Problem instance = new ZDT2();
    public static Problem instance() { return instance; }

    public double frontMinX() { return 0; }
    public double frontMaxX() { return 1; }
    public double frontMinY() { return 0; }
    public double frontMaxY() { return 1; }

    public int inputDimension() { return 30; }
    public String getName() { return "ZDT2"; }

    private double g(double[] input) {
        double g = 0;
        for (int i = 1; i < input.length; ++i) {
            g += input[i];
        }
        return 1 + g * 9 / (input.length - 1);
    }

    public Solution evaluate(double[] input) {
        double g = g(input);
        double h = 1 - (input[0] / g) * (input[0] / g);
        return new Solution(input[0], h * g, input);
    }
}
