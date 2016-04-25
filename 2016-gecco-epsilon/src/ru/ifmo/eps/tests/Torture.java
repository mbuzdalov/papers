package ru.ifmo.eps.tests;

import java.util.*;
import ru.ifmo.eps.*;
import ru.ifmo.eps.omq.*;

public class Torture {
    static final Random random = new Random();
    static final BinaryEpsilon[] algorithms = { new NaiveBinaryEpsilon(),
                                                new OMQBinaryEpsilon(TreeOMQ.INSTANCE),
                                                new OMQ2BinaryEpsilon() };

    static void randomPoints(int n, int d, int runs) {
        System.out.print("    Running torture test with random points for n = " + n + ", d = " + d + " for " + runs + " runs... ");
        long t0 = System.nanoTime();
        for (int run = 0; run < runs; ++run) {
            double[][] moving = new double[n - random.nextInt(n / 6 + 1)][d];
            double[][] fixed = new double[n - random.nextInt(n / 6 + 1)][d];
            if (random.nextBoolean()) {
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
            } else {
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
            }
            double[] algoResults = new double[algorithms.length];
            Arrays.fill(algoResults, Double.NaN);
            boolean failed = false;
            try {
                for (int i = 0; i < algorithms.length; ++i) {
                    algoResults[i] = algorithms[i].computeBinaryEpsilon(moving, fixed);
                }
            } catch (Throwable th) {
                failed = true;
            }
            double first = algoResults[0];
            for (int i = 1; i < algorithms.length; ++i) {
                if (failed || Math.abs(algoResults[i] - first) > 1e-9) {
                    System.out.println("\nTest found:");
                    System.out.println("        double[][] moving = {");
                    for (int j = 0; j < moving.length; ++j) {
                        System.out.print("          { ");
                        for (int k = 0; k < d; ++k) {
                            System.out.print(moving[j][k] + ", ");
                        }
                        System.out.println("},");
                    }
                    System.out.println("        };");
                    System.out.println("        double[][] fixed = {");
                    for (int j = 0; j < fixed.length; ++j) {
                        System.out.print("          { ");
                        for (int k = 0; k < d; ++k) {
                            System.out.print(fixed[j][k] + ", ");
                        }
                        System.out.println("},");
                    }
                    System.out.println("        };");
                    System.out.println("        assertEquals(" + first + ", runEpsilon(moving, fixed), 1e-9);");
                    System.out.println("Algorithm results:");
                    for (int j = 0; j < algorithms.length; ++j) {
                        System.out.println("    " + algorithms[j].getName() + " => " + algoResults[j]);
                    }
                    System.exit(1);
                }
            }
        }
        System.out.println("OK in " + (System.nanoTime() - t0) / 1e9 + " sec");
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
