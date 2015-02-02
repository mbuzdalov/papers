package ru.ifmo.steady.problem;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public abstract class WFG implements Problem {
    protected final int k, l, m;
    protected double[] A;
    protected double[] S;
    protected int D = 1;
    protected final int number;

    protected WFG(int number, int k, int l, int m) {
        this.number = number;
        this.k = k;
        this.l = l;
        this.m = m;
    }

    public int inputDimension() { return k + l; }
    public String getName() { return "WFG" + number; }

    protected abstract double[] evaluateImpl(double[] input);

    protected double[] calculateX(double[] t) {
        double[] x = new double[m];
        for (int i = 0; i < m - 1; ++i) {
            x[i] = Math.max(t[m - 1], A[i]) * (t[i] - 0.5) + 0.5;
        }
        x[m - 1] = t[m - 1];
        return x;
    }
    protected void normalize(double[] z) {
        for (int i = 0; i < z.length; ++i) {
            z[i] = Common.correct01(z[i] / (2 * (i + 1)));
        }
    }

    public Solution evaluate(double[] input) {
        double[] scaled = input.clone();
        for (int i = 0; i < scaled.length; ++i) {
            scaled[i] *= 2 * (i + 1);
        }
        double[] ei = evaluateImpl(scaled);
        return new Solution(ei[0], ei[1], input);
    }
}

