package ru.ifmo.steady;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import java.util.function.Supplier;
import java.util.stream.IntStream;

import ru.ifmo.steady.problem.*;

public class Experiments {
    private static final int EXP_RUN = 100;
    private static final int EXP_Q50 = 50;
    private static final int EXP_Q25 = 25;
    private static final int EXP_Q75 = 75;

    private static final int BUDGET = 25000;
    private static final int GEN_SIZE = 100;

    private static class Configuration {
        public final Supplier<SolutionStorage> storageSupplier;
        public final boolean isSteady;
        public final String name;
        public Configuration(Supplier<SolutionStorage> storageSupplier, boolean isSteady) {
            this.storageSupplier = storageSupplier;
            this.isSteady = isSteady;
            this.name = storageSupplier.get().getName() + "(" + (isSteady ? "ss" : "gen") + ")";
        }
    }
    private static final Configuration[] configs = {
        new Configuration(() -> new ru.ifmo.steady.inds.Storage(),   true),
        new Configuration(() -> new ru.ifmo.steady.enlu.Storage(),   true),
//        new Configuration(() -> new ru.ifmo.steady.debNDS.Storage(), true),
        new Configuration(() -> new ru.ifmo.steady.inds.Storage(),   false),
        new Configuration(() -> new ru.ifmo.steady.enlu.Storage(),   false),
//        new Configuration(new ru.ifmo.steady.debNDS.Storage(), false),
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

        public RunResult(Problem problem, Supplier<SolutionStorage> storageSupplier, int iterationSize,
                         boolean debSelection, boolean jmetalComparison, boolean debRemoval) {
            IntStream.range(0, EXP_RUN).parallel().forEach(t -> {
                NSGA2 algo = new NSGA2(problem, storageSupplier.get(), GEN_SIZE, iterationSize,
                                       debSelection, jmetalComparison, debRemoval);
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
            });

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

    private static void run(Problem problem, boolean debSelection, boolean jmetalComparison, boolean debRemoval) {
        RunResult[] results = new RunResult[configs.length];
        for (int i = 0; i < configs.length; ++i) {
            results[i] = new RunResult(
                problem, configs[i].storageSupplier,
                configs[i].isSteady ? 1 : GEN_SIZE,
                debSelection, jmetalComparison, debRemoval
            );
        }

        System.out.print("------+--------+--------+--------+------");
        for (RunResult rr : results) {
            System.out.print("+----------------------");
        }
        System.out.println();
        System.out.print("      |        |        |        | HV   ");
        for (RunResult rr : results) {
            System.out.printf("| %.3e (%.2e) ", rr.hyperVolumeMed, rr.hyperVolumeIQR);
        }
        System.out.println();
        System.out.printf("%5s |   %s    |   %s    |   %s    | time ",
            problem.getName(),
            debSelection ? "+" : "-",
            debRemoval ? "+" : "-",
            jmetalComparison ? "+" : "-"
        );
        for (RunResult rr : results) {
            System.out.printf("| %.3e (%.2e) ", rr.runningTimeMed, rr.runningTimeIQR);
        }
        System.out.println();
        System.out.print("      |        |        |        | cmps ");
        for (RunResult rr : results) {
            System.out.printf("| %.3e (%.2e) ", rr.comparisonMed, rr.comparisonIQR);
        }
        System.out.println();
    }

    private static void run(Problem problem) {
//        run(problem, true, false);
        run(problem, true, true, true);
        run(problem, true, true, false);
//        run(problem, false, false);
//        run(problem, false, true);
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        System.out.print(" Prob | DebSel | DebRem | jMetal | Stat ");
        for (int i = 0; i < configs.length; ++i) {
            System.out.printf("| %-20s ", configs[i].name);
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
        run(WFG2.instance());
        run(WFG3.instance());
        run(WFG4.instance());
        run(WFG5.instance());
        run(WFG6.instance());
        run(WFG7.instance());
        run(WFG8.instance());
        run(WFG9.instance());
    }
}
