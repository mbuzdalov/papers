package ru.ifmo.eps.tests;

import java.util.*;
import ru.ifmo.eps.*;
import ru.ifmo.eps.orq.*;

public class Timing {
    static final Random random = new Random();
    static final BinaryEpsilon[] algorithms = { new NaiveBinaryEpsilon(),
                                                new ORQBinaryEpsilon(TreeORQ.INSTANCE),
                                                new ORQ2BinaryEpsilon() };

    static void randomPoints(int n, int d, int runs, boolean silent) {
        if (!silent) {
            System.out.println("    [randomPoints] n = " + n + " d = " + d + " runs = " + runs);
        }
        for (BinaryEpsilon algorithm : algorithms) {
            long algoTimes = 0;
            for (int run = 0; run < runs; ++run) {
                double[][] moving = new double[n - random.nextInt(n / 6 + 1)][d];
                double[][] fixed = new double[n - random.nextInt(n / 6 + 1)][d];
                for (double[] m : moving) {
                    for (int i = 0; i < d; ++i) {
                        m[i] = random.nextDouble();
                    }
                }
                for (double[] f : fixed) {
                    for (int i = 0; i < d; ++i) {
                        f[i] = random.nextDouble();
                    }
                }
                long t0 = System.nanoTime();
                algorithm.computeBinaryEpsilon(moving, fixed);
                long time = System.nanoTime() - t0;
                algoTimes += time;
            }
            if (!silent) {
                System.out.printf(Locale.US, "        %40s: %10.6f sec%n", algorithm.getName(), (double) (algoTimes) / runs / 1e9);
            }
        }
    }

    public static void main(String[] args) {
        System.out.print("    Warming up... ");
        for (int dim = 2; dim <= 4; ++dim) {
            for (int size : new int[] { 100, 310, 1000, 3100 }) {
                randomPoints(size, dim, 10, true);
            }
        }
        System.out.println();
        for (int dim = 2; dim <= 6; ++dim) {
            for (int size : new int[] { 100, 310, 1000, 3100, 10000, 31000 }) {
                randomPoints(size, dim, 20, false);
            }
        }
    }
}
