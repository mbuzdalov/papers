package ru.ifmo.steady.problem;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public class ZDT6 implements Problem {
    private static final Problem instance = new ZDT6();
    public static Problem instance() { return instance; }

    public double frontMinX() { return 0.280775319; }
    public double frontMaxX() { return 1; }
    public double frontMinY() { return 0; }
    public double frontMaxY() { return 0.9211654; }

    public int inputDimension() { return 10; }
    public String getName() { return "ZDT6"; }

    private double g(double[] input) {
        double g = 0;
        for (int i = 1; i < input.length; ++i) {
            g += input[i];
        }
        return 1 + 9 * Math.pow(g / (input.length - 1), 0.25);
    }

    public Solution evaluate(double[] input) {
        double g = g(input);
        double x1 = input[0];
        double f = 1 - Math.exp(-4 * x1) * Math.pow(Math.sin(6 * Math.PI * x1), 6);
        double h = 1 - (f / g) * (f / g);
        return new Solution(f, h * g, input);
    }
}
