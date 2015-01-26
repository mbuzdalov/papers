package ru.ifmo.steady;

import java.util.Arrays;
import java.util.Locale;

import ru.ifmo.steady.problem.*;

public class Experiments {
    private static final int EXP_RUN = 100;
    private static final int ITERATIONS = 25000;
    private static final int GEN_SIZE = 100;

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

        int m1 = EXP_RUN / 2, m2 = EXP_RUN / 2 - 1;

        double hyperVolume = (hv[m1] + hv[m2]) / 2;
        double comparisons = (cmp[m1] + cmp[m2]) / 2;
        double time = (tm[m1] + tm[m2]) / 2;

        System.out.printf("      HV   = %.3f\n", hyperVolume);
        System.out.printf("      time = %.3f\n", time);
        System.out.printf("      cmps = %.1f\n", comparisons);
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
