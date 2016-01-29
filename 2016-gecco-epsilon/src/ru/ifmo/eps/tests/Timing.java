package ru.ifmo.eps.tests;

import java.util.*;
import ru.ifmo.eps.*;
import ru.ifmo.eps.orq.*;

public class Timing {
    static final Random random = new Random();
    static final BinaryEpsilon[] algorithms = { new NaiveBinaryEpsilon(), new BinsearchBinaryEpsilon(),
                                                new ORQBinaryEpsilon(NaiveORQ.BUILDER) };

    static void randomPoints(int n, int d, int runs) {
        System.out.println("    Running timing test with random points for n = " + n + ", d = " + d + " for " + runs + " runs... ");
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
            System.out.printf(Locale.US, "        %40s: %10.6f sec%n", algorithm.getName(), (double) (algoTimes) / runs / 1e9);
        }
    }

    public static void main(String[] args) {
        randomPoints(100, 2, 1000);
        randomPoints(1000, 2, 100);
        randomPoints(10000, 2, 10);
        randomPoints(100, 3, 1000);
        randomPoints(1000, 3, 100);
        randomPoints(10000, 3, 10);
        randomPoints(100, 4, 1000);
        randomPoints(1000, 4, 100);
        randomPoints(10000, 4, 10);
        randomPoints(100, 5, 1000);
        randomPoints(1000, 5, 100);
        randomPoints(10000, 5, 10);
    }
}
