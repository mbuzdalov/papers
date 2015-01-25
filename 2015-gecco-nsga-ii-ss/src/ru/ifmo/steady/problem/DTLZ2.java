package ru.ifmo.steady.problem;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public class DTLZ2 implements Problem {
    private static final Problem instance = new DTLZ2();
    public static Problem instance() { return instance; }

    public double frontMinX() { return 0; }
    public double frontMaxX() { return 1; }
    public double frontMinY() { return 0; }
    public double frontMaxY() { return 1; }

    public int inputDimension() { return 11; }
    public String getName() { return "DTLZ2"; }

    public Solution evaluate(double[] input) {
        double gm = Common.gDTLZ2(input, 1);
        double a = input[0] * Math.PI / 2;
        return new Solution(
            (1 + gm) * Math.cos(a),
            (1 + gm) * Math.sin(a),
            input
        );
    }
}
