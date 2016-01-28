package ru.ifmo.eps.tests;

import java.util.*;
import ru.ifmo.eps.*;

public class Timing {
    static final Random random = new Random();
    static final BinaryEpsilon[] algorithms = { new NaiveBinaryEpsilon(), new BinsearchBinaryEpsilon() };

    static void randomPoints(int n, int d, int runs) {
        System.out.println("    Running timing test with random points for n = " + n + ", d = " + d + " for " + runs + " runs... ");
        for (BinaryEpsilon algorithm : algorithms) {
            long algoTimes = 0;
            for (int run = 0; run < runs; ++run) {
                double[][] moving = new double[n - random.nextInt(n / 6 + 1)][d];
                double[][] fixed = new double[n - random.nextInt(n / 6 + 1)][d];
                for (double[] m : moving) {
                    for (int i = 0; i < d; ++i) {
                        m[i] = random.nextInt(100);
                    }
                }
                for (double[] f : fixed) {
                    for (int i = 0; i < d; ++i) {
                        f[i] = random.nextInt(100);
                    }
                }
                long t0 = System.nanoTime();
                algorithm.computeBinaryEpsilon(moving, fixed);
                long time = System.nanoTime() - t0;
                algoTimes += time;
            }
            System.out.println("        " + algorithm.getClass().getName() + ": " + ((double) (algoTimes) / runs / 1e9) + " sec");
        }
    }

    public static void main(String[] args) {
        randomPoints(10, 2, 10000);
        randomPoints(100, 2, 1000);
        randomPoints(1000, 2, 100);
        randomPoints(10, 3, 10000);
        randomPoints(100, 3, 1000);
        randomPoints(1000, 3, 100);
        randomPoints(10, 4, 10000);
        randomPoints(100, 4, 1000);
        randomPoints(1000, 4, 100);
    }
}
