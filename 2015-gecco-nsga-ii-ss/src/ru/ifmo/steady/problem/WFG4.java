package ru.ifmo.steady.problem;

import java.util.Arrays;

import ru.ifmo.steady.Problem;
import ru.ifmo.steady.Solution;

public class WFG4 extends WFG {
    private static final Problem instance = new WFG4(2, 4, 2);
    public static Problem instance() { return instance; }

    public double frontMinX() { return 0; }
    public double frontMaxX() { return 2; }
    public double frontMinY() { return 0; }
    public double frontMaxY() { return 4; }

    protected WFG4(int k, int l, int m) {
        super(4, k, l, m);
        S = new double[m];
        for (int i = 0; i < m; ++i) {
            S[i] = 2 * (i + 1);
        }
        A = new double[m - 1];
        Arrays.fill(A, 1);
    }

    protected double[] evaluateImpl(double[] y) {
        normalize(y);
        t1(y);
        y = t2(y);
        double[] x = calculateX(y);
        double[] rv = new double[m];
        for (int i = 1; i <= m; ++i) {
            rv[i - 1] = D * x[m - 1] + S[i - 1] * Common.concaveShape(x, i);
        }
        return rv;
    }

    private void t1(double[] z) {
        for (int i = 0; i < z.length; ++i) {
            z[i] = Common.sMulti(z[i], 30, 10, 0.35);
        }
    }
    private double[] t2(double[] z) {
        double[] w = new double[z.length];
        for (int i = 0; i < w.length; ++i) {
            w[i] = 1;
        }
        double[] r = new double[m];
        for (int i = 1; i < m; ++i) {
            int head = (i - 1) * k / (m - 1) + 1;
            int tail = i * k / (m - 1);
            r[i - 1] = Common.rSum(z, w, head - 1, tail - 1);
        }
        r[m - 1] = Common.rSum(z, w, k, z.length - 1);
        return r;
    }
}
