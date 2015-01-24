package ru.ifmo.steady.problem;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public class DTLZ1 implements Problem {
    public int inputDimension() {
        return 11;
    }

    public Solution evaluate(double[] input) {
        double gm = Common.gDTLZ1(input, 1);
        return new Solution(
            0.5 * input[0] * (1 + gm),
            0.5 * (1 - input[0]) * (1 + gm),
            input
        );
    }
}
