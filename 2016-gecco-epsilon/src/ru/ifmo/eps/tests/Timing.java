package ru.ifmo.eps.tests;

import java.util.*;
import ru.ifmo.eps.*;
import ru.ifmo.eps.orq.*;

public class Timing {
    static final Random random = new Random();
    static final BinaryEpsilon[] algorithms = { new NaiveBinaryEpsilon(),
                                                new ORQBinaryEpsilon(TreeORQ.INSTANCE),
                                                new ORQ2BinaryEpsilon() };

    static abstract class Generator {
        public abstract double[][] generate(int n, int d);
        public abstract String getName();
    }

    static Generator randomPoints = new Generator() {
        public double[][] generate(int n, int d) {
            double[][] rv = new double[n][d];
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < d; ++j) {
                    rv[i][j] = random.nextDouble();
                }
            }
            return rv;
        }
        public String getName() {
            return "randomPoints";
        }
    };

    static Generator flatPoints = new Generator() {
        public double[][] generate(int n, int d) {
            double[][] rv = new double[n][d];
            for (int i = 0; i < n; ++i) {
                double sum = 0;
                for (int j = 1; j < d; ++j) {
                    rv[i][j] = random.nextDouble();
                    sum += rv[i][j];
                }
                rv[i][0] = d - sum;
            }
            return rv;
        }
        public String getName() {
            return "flatPoints";
        }
    };

    static void checkPoints(int n, int d, int runs, boolean silent, Generator generator) {
        if (!silent) {
            System.out.println("    [" + generator.getName() + "] n = " + n + " d = " + d + " runs = " + runs);
        }
        for (BinaryEpsilon algorithm : algorithms) {
            long algoTimes = 0;
            for (int run = 0; run < runs; ++run) {
                double[][] moving = generator.generate(n, d);
                double[][] fixed = generator.generate(n, d);
                if (!silent) {
                    System.gc();
                    System.gc();
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
                checkPoints(size, dim, 10, true, randomPoints);
            }
        }
        System.out.println();
        for (Generator g : new Generator[] { randomPoints, flatPoints }) {
            for (int dim = 2; dim <= 6; ++dim) {
                for (int size : new int[] { 100, 310, 1000, 3100, 10000, 31000 }) {
                    checkPoints(size, dim, 20, false, g);
                }
            }
        }
    }
}
