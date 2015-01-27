package ru.ifmo.steady;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import ru.ifmo.steady.problem.*;

public class Experiments {
    private static final SolutionStorage[] storages = {
        new ru.ifmo.steady.inds.Storage(),
        new ru.ifmo.steady.enlu.Storage()
    };

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

    private static class RunResult {
        public final double[] hyperVolumes = new double[EXP_RUN];
        public final double[] comparisons  = new double[EXP_RUN];
        public final double[] runningTimes = new double[EXP_RUN];

        public final double hyperVolumeMed;
        public final double hyperVolumeIQR;
        public final double comparisonMed;
        public final double comparisonIQR;
        public final double runningTimeMed;
        public final double runningTimeIQR;

        public RunResult(Problem problem, SolutionStorage storage) {
            NSGA2ss algo = new NSGA2ss(problem, storage, GEN_SIZE);

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
                hyperVolumes[t] = algo.currentHyperVolume();
                comparisons[t]  = Solution.comparisons;
                runningTimes[t] = (finishTime - startTime) / 1e9;
            }

            Arrays.sort(hyperVolumes);
            Arrays.sort(comparisons);
            Arrays.sort(runningTimes);

            hyperVolumeMed = med(hyperVolumes);
            hyperVolumeIQR = iqr(hyperVolumes);
            comparisonMed  = med(comparisons);
            comparisonIQR  = iqr(comparisons);
            runningTimeMed = med(runningTimes);
            runningTimeIQR = iqr(runningTimes);
        }
    }

    private static void run(Problem problem) {
        RunResult[] results = new RunResult[storages.length];
        for (int i = 0; i < storages.length; ++i) {
            results[i] = new RunResult(problem, storages[i]);
        }

        System.out.print("------");
        for (RunResult rr : results) {
            System.out.print("+-----------------------------------");
        }
        System.out.println();
        System.out.print("      ");
        for (RunResult rr : results) {
            System.out.printf("| HV   = %.3e; IQR = %.3e ", rr.hyperVolumeMed, rr.hyperVolumeIQR);
        }
        System.out.println();
        System.out.printf("%6s", problem.getName());
        for (RunResult rr : results) {
            System.out.printf("| time = %.3e; IQR = %.3e ", rr.runningTimeMed, rr.runningTimeIQR);
        }
        System.out.println();
        System.out.print("      ");
        for (RunResult rr : results) {
            System.out.printf("| cmps = %.3e; IQR = %.3e ", rr.comparisonMed, rr.comparisonIQR);
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        System.out.print("      ");
        for (int i = 0; i < storages.length; ++i) {
            System.out.printf("| %33s ", storages[i].getName());
        }
        System.out.println();

        run(ZDT1.instance());
        run(ZDT2.instance());
        run(ZDT3.instance());
        run(ZDT4.instance());
        run(ZDT6.instance());

        run(DTLZ1.instance());
        run(DTLZ2.instance());
        run(DTLZ3.instance());
        run(DTLZ4.instance());
        run(DTLZ5.instance());
        run(DTLZ6.instance());
        run(DTLZ7.instance());

        run(WFG1.instance());
    }
}
