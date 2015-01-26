package ru.ifmo.steady;

import java.util.Arrays;
import java.util.Locale;

import ru.ifmo.steady.problem.*;

public class Experiments {
    private static final int EXP_RUN = 100;
    private static final int EXP_Q50 = 50;
    private static final int EXP_Q25 = 25;
    private static final int EXP_Q75 = 75;

    private static final int ITERATIONS = 25000;
    private static final int GEN_SIZE = 100;

    private static final double med(double[] a) {
        if (a.length == EXP_RUN) {
            return (a[EXP_Q50] + a[EXP_Q50 - 1]) / 2;
        }
        throw new IllegalArgumentException();
    }

    private static final double iqr(double[] a) {
        if (a.length == EXP_RUN) {
            return (a[EXP_Q75] + a[EXP_Q75 - 1]) / 2 - (a[EXP_Q25] + a[EXP_Q25 - 1]) / 2;
        }
        throw new IllegalArgumentException();
    }

    private static void run(SolutionStorage storage, Problem problem) {
        System.out.println("   for " + problem.getName());
        NSGA2ss algo = new NSGA2ss(problem, storage, GEN_SIZE);

        double[] hv = new double[EXP_RUN];
        double[] cmp = new double[EXP_RUN];
        double[] tm = new double[EXP_RUN];

        for (int t = 0; t < EXP_RUN; ++t) {
            System.gc();
            System.gc();

            long startTime = System.nanoTime();
            Solution.comparisons = 0;
            algo.initialize();
            for (int i = GEN_SIZE; i < ITERATIONS; ++i) {
                algo.performIteration();
            }
            long finishTime = System.nanoTime();
            hv[t] = algo.currentHyperVolume();
            cmp[t] = Solution.comparisons;
            tm[t] = (finishTime - startTime) / 1e9;
        }

        Arrays.sort(hv);
        Arrays.sort(cmp);
        Arrays.sort(tm);

        System.out.printf("      HV   = %.2e; IQR = %.2e\n", med(hv), iqr(hv));
        System.out.printf("      time = %.2e; IQR = %.2e\n", med(tm), iqr(tm));
        System.out.printf("      cmps = %.2e; IQR = %.2e\n", med(cmp), iqr(cmp));
    }

    private static void run(SolutionStorage storage) {
        System.out.println("Running experiments for " + storage.getName());
        run(storage, DTLZ1.instance());
        run(storage, DTLZ2.instance());
        run(storage, DTLZ3.instance());
        run(storage, DTLZ4.instance());
        run(storage, DTLZ5.instance());
        run(storage, DTLZ6.instance());
        run(storage, DTLZ7.instance());
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        run(new ru.ifmo.steady.inds.Storage());
        run(new ru.ifmo.steady.enlu.Storage());
    }
}
