package ru.ifmo.steady.problem;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public class DTLZ4 implements Problem {
    public int inputDimension() {
        return 11;
    }

    public Solution evaluate(double[] input) {
        double gm = Common.gDTLZ2(input, 1);
        double a = Math.pow(input[0], 100) * Math.PI / 2;
        return new Solution(
            (1 + gm) * Math.cos(a),
            (1 + gm) * Math.sin(a),
            input
        );
    }
}
