package ru.ifmo.steady;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import ru.ifmo.steady.problem.*;

public class Experiments {
    private static final int EXP_RUN = 100;
    private static final int EXP_Q50 = 50;
    private static final int EXP_Q25 = 25;
    private static final int EXP_Q75 = 75;

    private static final int BUDGET = 25000;
    private static final int GEN_SIZE = 100;

    private static final SolutionStorage[] storages = {
        new ru.ifmo.steady.inds.Storage(),
        new ru.ifmo.steady.enlu.Storage(),
        new ru.ifmo.steady.debNDS.Storage(),
        new ru.ifmo.steady.inds.Storage(),
        new ru.ifmo.steady.enlu.Storage(),
        new ru.ifmo.steady.debNDS.Storage()
    };
    private static final String[] steadiness = {
        "ss", "ss", "ss", "gen", "gen", "gen"
    };

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

    private static double count = 0;
    private static double sumC = 0;
    private static double sumT = 0;
    private static double sumCC = 0;
    private static double sumTT = 0;
    private static double sumTC = 0;

    private static void addTimeLog(double time, double comparisons) {
        count += 1;
        sumC += comparisons;
        sumT += time;
        sumCC += comparisons * comparisons;
        sumTT += time * time;
        sumTC += time * comparisons;
    }

    private static void computeTimeLog() {
        double det = count * sumCC - sumC * sumC;
        double detA = sumT * sumCC - sumTC * sumC;
        double detB = count * sumTC - sumC * sumT;
        double alpha = detA / det;
        double beta = detB / det;
        System.out.println("Linear regression: time = " + alpha + " + " + beta + " * comparisons");
        double error = alpha * alpha * count + beta * beta * sumCC + 2 * alpha * beta * sumC - 2 * alpha * sumT - 2 * beta * sumTC + sumTT;
        System.out.println("Mean square error: sqr(" + Math.sqrt(error) + ")");
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

        public RunResult(Problem problem, SolutionStorage storage, int iterationSize) {
            NSGA2 algo = new NSGA2(problem, storage, GEN_SIZE, iterationSize);

            for (int t = 0; t < EXP_RUN; ++t) {
                System.gc();
                System.gc();

                long startTime = System.nanoTime();
                Solution.comparisons = 0;
                algo.initialize();
                for (int i = GEN_SIZE; i < BUDGET; i += iterationSize) {
                    algo.performIteration();
                }
                long finishTime = System.nanoTime();
                hyperVolumes[t] = algo.currentHyperVolume();
                comparisons[t]  = Solution.comparisons;
                runningTimes[t] = (finishTime - startTime) / 1e9;
                addTimeLog(runningTimes[t], comparisons[t]);
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
            results[i] = new RunResult(problem, storages[i], steadiness[i].equals("ss") ? 1 : GEN_SIZE);
        }

        System.out.print("------+------");
        for (RunResult rr : results) {
            System.out.print("+---------------------");
        }
        System.out.println();
        System.out.print("      | HV   ");
        for (RunResult rr : results) {
            System.out.printf("| %.2e (%.2e) ", rr.hyperVolumeMed, rr.hyperVolumeIQR);
        }
        System.out.println();
        System.out.printf("%5s | time ", problem.getName());
        for (RunResult rr : results) {
            System.out.printf("| %.2e (%.2e) ", rr.runningTimeMed, rr.runningTimeIQR);
        }
        System.out.println();
        System.out.print("      | cmps ");
        for (RunResult rr : results) {
            System.out.printf("| %.2e (%.2e) ", rr.comparisonMed, rr.comparisonIQR);
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        System.out.print(" Prob | Stat ");
        for (int i = 0; i < storages.length; ++i) {
            System.out.printf("| %-19s ", storages[i].getName() + "(" + steadiness[i] + ")");
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

        computeTimeLog();
    }
}
