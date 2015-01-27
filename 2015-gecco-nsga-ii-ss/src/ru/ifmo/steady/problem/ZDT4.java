package ru.ifmo.steady.problem;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public class ZDT4 implements Problem {
    private static final Problem instance = new ZDT4();
    public static Problem instance() { return instance; }

    public double frontMinX() { return 0; }
    public double frontMaxX() { return 1; }
    public double frontMinY() { return 0; }
    public double frontMaxY() { return 1; }

    public int inputDimension() { return 10; }
    public String getName() { return "ZDT4"; }

    private double g(double[] input) {
        double g = 0;
        for (int i = 1; i < input.length; ++i) {
            double x = input[i] * 10 - 5;
            g += x * x + 10 * (1 - Math.cos(4 * Math.PI * x));
        }
        return 1 + g;
    }

    public Solution evaluate(double[] input) {
        double g = g(input);
        double f = input[0];
        double h = 1 - Math.sqrt(f / g);
        return new Solution(input[0], h * g, input);
    }
}
