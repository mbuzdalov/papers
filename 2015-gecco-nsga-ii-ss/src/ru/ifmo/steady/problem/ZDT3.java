package ru.ifmo.steady.problem;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public class ZDT3 implements Problem {
    private static final Problem instance = new ZDT3();
    public static Problem instance() { return instance; }

    public double frontMinX() { return 0; }
    public double frontMaxX() { return 0.851833; }
    public double frontMinY() { return -0.773369; }
    public double frontMaxY() { return 1; }

    public int inputDimension() { return 30; }
    public String getName() { return "ZDT3"; }

    private double g(double[] input) {
        double g = 0;
        for (int i = 1; i < input.length; ++i) {
            g += input[i];
        }
        return 1 + g * 9 / (input.length - 1);
    }

    public Solution evaluate(double[] input) {
        double g = g(input);
        double f = input[0];
        double h = 1 - Math.sqrt(f / g) - (f / g) * Math.sin(10 * Math.PI * f);
        return new Solution(input[0], h * g, input);
    }
}
