package ru.ifmo.steady.problem;

/**
 * Common utilities for test problems.
 */
public class Common {
    public static double gDTLZ1(double[] input, int first) {
        int last = input.length;
        double sum = last - first;
        for (int i = first; i < last; ++i) {
            double xi = input[i] - 0.5;
            sum += xi * xi;
            sum -= Math.cos(20 * Math.PI * xi);
        }
        return sum * 100;
    }

    public static double gDTLZ2(double[] input, int first) {
        int last = input.length;
        double sum = 0;
        for (int i = first; i < last; ++i) {
            double xi = input[i] - 0.5;
            sum += xi * xi;
        }
        return sum;
    }

    // Various functions supporting WFG

    public static final double wEPS = 1e-10;
    public static final double wEPSm = 0 - wEPS;
    public static final double wEPSp = 1 + wEPS;

    public static double correct01(double v) {
        if (v < 0 && v > wEPSm) return 0;
        if (v > 1 && v < wEPSp) return 1;
        if (v > 1 || v < 0) throw new AssertionError();
        return v;
    }

    public static double bPoly(double y, double alpha) {
        return correct01(Math.pow(y, alpha));
    }
    public static double bFlat(double y, double A, double B, double C) {
        double tmp1 = Math.min(0, Math.floor(y - B)) * A * (B - y) / B;
        double tmp2 = Math.min(0, Math.floor(C - y)) * (1 - A) * (y - C) / (1 - C);
        return correct01(A + tmp1 - tmp2);
    }
    public static double sLinear(double y, double A) {
        return correct01(Math.abs(y - A) / Math.abs(Math.floor(A - y) + A));
    }
    public static double sDecept(double y, double A, double B, double C) {
        double tmp1 = Math.floor(y - A + B) * (1 - C + (A - B) / B) / (A - B);
        double tmp2 = Math.floor(A + B - y) * (1 - C + (1 - A - B) / B) / (1 - A - B);
        double tmp = Math.abs(y - A) - B;
        return correct01(1 + tmp * (tmp1 + tmp2 + 1 / B));
    }
    public static double sMulti(double y, double A, double B, double C) {
        double tmp1 = (4 * A + 2) * Math.PI * (0.5 - Math.abs(y - C) / (2.0 * (Math.floor(C - y) + C)));
        double tmp2 = 4 * B * Math.pow(Math.abs(y - C) / (2 * (Math.floor(C - y) + C)), 2);
        return correct01((1 + Math.cos(tmp1) + tmp2) / (B + 2));
    }
    public static double rSum(double[] y, double[] w, int from, int to) {
        double tmp1 = 0, tmp2 = 0;
        for (int i = from; i <= to; ++i) {
            tmp1 += y[i] * w[i];
            tmp2 += w[i];
        }
        return correct01(tmp1 / tmp2);
    }
    public static double rNonSep(double[] y, int A) {
        double tmp = Math.ceil(A / 2.0);
        double den = y.length * tmp * (1.0 + 2 * A - 2 * tmp) / A;
        double num = 0;
        for (int j = 0; j < y.length; ++j) {
            num += y[j];
            for (int k = 0; k <= A - 2; ++k) {
                num += Math.abs(y[j] - y[(j + k + 1) % y.length]);
            }
        }
        return correct01(num / den);
    }
    public static double bParam(double y, double u, double A, double B, double C) {
        double v = A - (1 - 2 * u) * Math.abs(Math.floor(0.5 - u) + A);
        return correct01(Math.pow(y, B + (C - B) * v));
    }
    public static double linearShape(double[] x, int m) {
        double result = 1;
        int lim = x.length - m;
        for (int i = 1; i <= lim; ++i) {
            result *= x[i - 1];
        }
        if (m != 1) {
            result *= 1 - x[lim];
        }
        return result;
    }
    public static double convexShape(double[] x, int m) {
        double result = 1;
        int lim = x.length - m;
        for (int i = 1; i <= lim; ++i) {
            result *= 1 - Math.cos(x[i - 1] * Math.PI * 0.5);
        }
        if (m != 1) {
            result *= 1 - Math.sin(x[lim] * Math.PI * 0.5);
        }
        return result;
    }
    public static double concaveShape(double[] x, int m) {
        double result = 1;
        int lim = x.length - m;
        for (int i = 1; i <= lim; ++i) {
            result *= 1 - Math.sin(x[i - 1] * Math.PI * 0.5);
        }
        if (m != 1) {
            result *= 1 - Math.cos(x[lim] * Math.PI * 0.5);
        }
        return result;
    }
    public static double mixedShape(double[] x, double A, double alpha) {
        double tmp = Math.cos(2 * A * Math.PI * x[0] + Math.PI * 0.5) / (2 * A * Math.PI);
        return Math.pow(1 - x[0] - tmp, alpha);
    }
    public static double disc(double[] x, double A, double alpha, double beta) {
        double tmp = Math.cos(A * Math.pow(x[0], beta) * Math.PI);
        return 1 - Math.pow(x[0], alpha) * tmp * tmp;
    }
}
