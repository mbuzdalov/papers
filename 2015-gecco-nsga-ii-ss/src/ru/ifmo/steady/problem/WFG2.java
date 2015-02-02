package ru.ifmo.steady.problem;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public class WFG2 extends WFG {
    private static final Problem instance = new WFG2(2, 4, 2);
    public static Problem instance() { return instance; }

    public double frontMinX() { return 0; }
    public double frontMaxX() { return 2; }
    public double frontMinY() { return 0; }
    public double frontMaxY() { return 4; }

    protected WFG2(int k, int l, int m) {
        super(2, k, l, m);
        S = new double[m];
        for (int i = 0; i < m; ++i) {
            S[i] = 2 * (i + 1);
        }
        A = new double[m - 1];
        for (int i = 0; i + 1 < m; ++i) {
            A[i] = 1;
        }
    }

    protected double[] evaluateImpl(double[] y) {
        normalize(y);
        t1(y);
        y = t2(y);
        double[] x = calculateX(y);
        double[] rv = new double[m];
        for (int i = 1; i < m; ++i) {
            rv[i - 1] = D * x[m - 1] + S[i - 1] * Common.convexShape(x, i);
        }
        rv[m - 1] = D * x[m - 1] + S[m - 1] * Common.discShape(x, 5, 1, 1);
        return rv;
    }

    private void t1(double[] z) {
        for (int i = k; i < z.length; ++i) {
            z[i] = Common.sLinear(z[i], 0.35);
        }
    }
    private double[] t2(double[] z) {
        double[] rv = new double[z.length];
        System.arraycopy(z, 0, rv, 0, k);
        int l = z.length - k;
        for (int i = k + 1; i <= k + l / 2; ++i) {
            int head = k + 2 * (i - k) - 1;
            int tail = k + 2 * (i - k);
            rv[i - 1] = Common.rNonSep(z, head - 1, tail - 1, 2);
        }
        return rv;
    }
    private double[] t3(double[] z) {
        double[] rv = new double[m];
        double[] w = new double[z.length];
        for (int i = 0; i < z.length; ++i) {
            w[i] = 1;
        }
        for (int i = 0; i < m; ++i) {
            int head = (i - 1) * k / (m - 1) + 1;
            int tail = i * k / (m - 1);
            rv[i - 1] = Common.rSum(z, w, head - 1, tail - 1);
        }
        int head = k + 1;
        int tail = k + (z.length - k) / 2;
        rv[m - 1] = Common.rSum(z, w, head - 1, tail - 1);
        return rv;
    }
}
