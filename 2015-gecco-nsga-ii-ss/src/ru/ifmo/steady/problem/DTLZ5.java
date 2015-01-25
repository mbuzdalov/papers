package ru.ifmo.steady.problem;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public class DTLZ5 implements Problem {
    public double frontMinX() { return 0; }
    public double frontMaxX() { return 1; }
    public double frontMinY() { return 0; }
    public double frontMaxY() { return 1; }

    public int inputDimension() { return 11; }

    public Solution evaluate(double[] input) {
        double gm = Common.gDTLZ2(input, 1);
        double a = Math.PI / 2 * Math.PI * (1 + 2 * gm * input[0]) / (4 * (1 + gm));
        return new Solution(
            (1 + gm) * Math.cos(a),
            (1 + gm) * Math.sin(a),
            input
        );
    }
}
