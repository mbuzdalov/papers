package ru.ifmo.steady;

import java.io.*;
import java.util.*;

import java.util.function.Supplier;
import java.util.stream.IntStream;

import ru.ifmo.steady.problem.*;
import ru.ifmo.steady.NSGA2.Variant;

public class Experiments {
    private static final int EXP_RUN = 100;
    private static final int EXP_Q50 = 50;
    private static final int EXP_Q25 = 25;
    private static final int EXP_Q75 = 75;

    private static final int BUDGET = 25000;
    private static final int GEN_SIZE = 100;

    private static final List<Supplier<SolutionStorage>> suppliers = Arrays.asList(
        () -> new ru.ifmo.steady.inds.Storage(),
        () -> new ru.ifmo.steady.enlu.Storage(),
        () -> new ru.ifmo.steady.debNDS.Storage()
    );

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

    private static void writeToFile(double[] data, String filename) {
        try (PrintWriter out = new PrintWriter(filename)) {
            for (double v : data) {
                out.println(v);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
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

        public RunResult(Problem problem, Supplier<SolutionStorage> storageSupplier,
                         boolean debSelection, boolean jmetalComparison, Variant variant) {
            IntStream.range(0, EXP_RUN).parallel().forEach(t -> {
                NSGA2 algo = new NSGA2(problem, storageSupplier.get(), GEN_SIZE,
                                       debSelection, jmetalComparison, variant);
                long startTime = System.nanoTime();
                Solution.comparisons = 0;
                algo.initialize();
                for (int i = GEN_SIZE; i < BUDGET; i += GEN_SIZE) {
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

            String namePrefix = String.format("%s-%s-%d-%d-%s",
                problem.getName(),
                storageSupplier.get().getName(),
                debSelection ? 0 : 1,
                jmetalComparison ? 0 : 1,
                variant.shortName()
            );
            writeToFile(hyperVolumes, namePrefix + "-hv.txt");
            writeToFile(comparisons, namePrefix + "-cmp.txt");
            writeToFile(runningTimes, namePrefix + "-time.txt");

            hyperVolumeMed = med(hyperVolumes);
            hyperVolumeIQR = iqr(hyperVolumes);
            comparisonMed  = med(comparisons);
            comparisonIQR  = iqr(comparisons);
            runningTimeMed = med(runningTimes);
            runningTimeIQR = iqr(runningTimes);
        }
    }

    private static void run(Problem problem) {
        System.out.println("========");
        System.out.printf("| %-4s |\n", problem.getName());
        System.out.println("========");
        final boolean[] tf = { true, false };

        System.out.print(" DebSel | jMetal | Vari |      ");
        for (int i = 0; i < suppliers.size(); ++i) {
            System.out.printf("| %-20s ", suppliers.get(i).get().getName());
        }
        System.out.println();

        for (boolean debSelection : tf) {
            for (boolean jmetalComparison : tf) {
                for (Variant variant : Variant.all()) {
                    RunResult[] results = new RunResult[suppliers.size()];
                    System.out.print("--------+--------+------+------");
                    for (int i = 0; i < suppliers.size(); ++i) {
                        results[i] = new RunResult(problem, suppliers.get(i), debSelection, jmetalComparison, variant);
                        System.out.print("+----------------------");
                    }
                    System.out.println();
                    System.out.print("        |        |      | HV   ");
                    for (RunResult rr : results) {
                        System.out.printf("| %.3e (%.2e) ", rr.hyperVolumeMed, rr.hyperVolumeIQR);
                    }
                    System.out.println();
                    System.out.printf("    %s   |    %s   | %-4s | time ",
                            debSelection ? "+" : "-",
                            jmetalComparison ? "+" : "-",
                            variant.shortName()
                    );
                    for (RunResult rr : results) {
                        System.out.printf("| %.3e (%.2e) ", rr.runningTimeMed, rr.runningTimeIQR);
                    }
                    System.out.println();
                    System.out.print( "        |        |      | cmps ");
                    for (RunResult rr : results) {
                        System.out.printf("| %.3e (%.2e) ", rr.comparisonMed, rr.comparisonIQR);
                    }
                    System.out.println();
                }
            }
        }
        System.out.print("--------+-------+------+------");
        for (int i = 0; i < suppliers.size(); ++i) {
             System.out.print("+----------------------");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

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
