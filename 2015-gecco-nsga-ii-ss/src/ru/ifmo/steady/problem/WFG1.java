package ru.ifmo.steady.problem;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public class WFG1 extends WFG {
    private static final Problem instance = new WFG1(2, 4, 2);
    public static Problem instance() { return instance; }

    public double frontMinX() { return 0; }
    public double frontMaxX() { return 2; }
    public double frontMinY() { return 0; }
    public double frontMaxY() { return 4; }

    protected WFG1(int k, int l, int m) {
        super(1, k, l, m);
        S = new double[m];
        for (int i = 0; i < m; ++i) {
            S[i] = 2 * (i + 1);
        }
        A = new double[m - 1];
        for (int i = 0; i + 1 < m; ++i) {
            A[i] = 1;
        }
    }

    protected double[] evaluateImpl(double[] input) {
        double[] y = normalize(input);
        t1(y);
        t2(y);
        t3(y);
        y = t4(y);
        double[] x = calculateX(y);
        double[] rv = new double[m];
        for (int i = 1; i < m; ++i) {
            rv[i - 1] = D * x[m - 1] + S[i - 1] * Common.convexShape(x, i);
        }
        rv[m - 1] = D * x[m - 1] + S[m - 1] * Common.mixedShape(x, 5, 1);
        return rv;
    }

    private void t1(double[] z) {
        for (int i = k; i < z.length; ++i) {
            z[i] = Common.sLinear(z[i], 0.35);
        }
    }
    private void t2(double[] z) {
        for (int i = k; i < z.length; ++i) {
            z[i] = Common.bFlat(z[i], 0.8, 0.75, 0.85);
        }
    }
    private void t3(double[] z) {
        for (int i = 0; i < z.length; ++i) {
            z[i] = Common.bPoly(z[i], 0.02);
        }
    }
    private double[] t4(double[] z) {
        double[] w = new double[z.length];
        for (int i = 0; i < w.length; ++i) {
            w[i] = 2 * (i + 1);
        }
        double[] r = new double[m];
        for (int i = 1; i <= m - 1; ++i) {
            int head = (i - 1) * k / (m - 1) + 1;
            int tail = i * k / (m - 1);
            r[i - 1] = Common.rSum(z, w, head - 1, tail - 1);
        }
        r[m - 1] = Common.rSum(z, w, k, z.length - 1);
        return r;
    }
}
