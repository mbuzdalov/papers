package ru.ifmo.steady.problem;

/**
 * Common utilities for test problems.
 */
public class Common {
    public static double gDTLZ1(double[] input, int first) {
        int last = input.length;
        double length2 = 0;
        for (int i = first; i < last; ++i) {
            length2 += input[i] * input[i];
        }

        double sum = Math.sqrt(length2);
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
}
