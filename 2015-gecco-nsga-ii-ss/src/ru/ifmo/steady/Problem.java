package ru.ifmo.steady;

import ru.ifmo.steady.util.FastRandom;

public interface Problem {
    public double frontMinX();
    public double frontMinY();
    public double frontMaxX();
    public double frontMaxY();
    public int inputDimension();
    public String getName();
    public Solution evaluate(double[] input);
    public default double[] generate() {
        double[] rv = new double[inputDimension()];
        for (int i = 0; i < rv.length; ++i) {
            rv[i] = FastRandom.geneticThreadLocal().nextDouble();
        }
        return rv;
    }
}
