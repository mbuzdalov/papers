package ru.ifmo.steady;

import ru.ifmo.steady.util.FastRandom;

public interface Problem {
    public int inputDimension();
    public Solution evaluate(double[] input);
    public default double[] generate() {
        double[] rv = new double[inputDimension()];
        for (int i = 0; i < rv.length; ++i) {
            rv[i] = FastRandom.threadLocal().nextDouble();
        }
        return rv;
    }
}
