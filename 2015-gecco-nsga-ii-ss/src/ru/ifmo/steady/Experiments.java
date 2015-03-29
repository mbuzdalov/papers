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
                SolutionStorage storage = storageSupplier.get();
                NSGA2 algo = new NSGA2(problem, storage, GEN_SIZE,
                                       debSelection, jmetalComparison, variant);
                long startTime = System.nanoTime();
                algo.initialize();
                for (int i = GEN_SIZE; i < BUDGET; i += GEN_SIZE) {
                    algo.performIteration();
                }
                long finishTime = System.nanoTime();
                hyperVolumes[t] = algo.currentHyperVolume();
                comparisons[t]  = storage.getComparisonCounter().get();
                runningTimes[t] = (finishTime - startTime) / 1e9;
            });

            Arrays.sort(hyperVolumes);
            Arrays.sort(comparisons);
            Arrays.sort(runningTimes);

            String namePrefix = String.format("runs/%s-%s-%d-%d-%s",
                problem.getName(),
                storageSupplier.get().getName(),
                debSelection ? 1 : 0,
                jmetalComparison ? 1 : 0,
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

    private static class Config {
        private final List<Supplier<SolutionStorage>> suppliers;
        private final List<Variant> variants;
        private final List<Boolean> debSelectionOptions;
        private final List<Boolean> jmetalComparisonOptions;

        public Config(List<Supplier<SolutionStorage>> suppliers,
                      List<Variant> variants,
                      List<Boolean> debSelectionOptions,
                      List<Boolean> jmetalComparisonOptions) {
            this.suppliers = suppliers;
            this.variants = variants;
            this.debSelectionOptions = debSelectionOptions;
            this.jmetalComparisonOptions = jmetalComparisonOptions;
        }

        public void run(Problem problem) {
            System.out.println("========");
            System.out.printf("| %-4s |\n", problem.getName());
            System.out.println("========");

            System.out.print(" DebSel | jMetal | Vari |      ");
            for (int i = 0; i < suppliers.size(); ++i) {
                System.out.printf("| %-20s ", suppliers.get(i).get().getName());
            }
            System.out.println();

            for (boolean debSelection : debSelectionOptions) {
                for (boolean jmetalComparison : jmetalComparisonOptions) {
                    for (Variant variant : variants) {
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
            System.out.print("--------+--------+------+------");
            for (int i = 0; i < suppliers.size(); ++i) {
                 System.out.print("+----------------------");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        List<Supplier<SolutionStorage>> suppliers = new ArrayList<>();
        List<Variant> variants = new ArrayList<>();
        List<Boolean> debSelection = new ArrayList<>();
        List<Boolean> jmetalComparison = new ArrayList<>();

        Set<String> usedOptions = new HashSet<>();
        Map<String, Runnable> actions = new HashMap<>();

        actions.put("-S:inds", () -> suppliers.add(() -> new ru.ifmo.steady.inds.Storage()));
        actions.put("-S:enlu", () -> suppliers.add(() -> new ru.ifmo.steady.enlu.Storage()));
        actions.put("-S:deb",  () -> suppliers.add(() -> new ru.ifmo.steady.debNDS.Storage()));
        actions.put("-V:pss",  () -> variants.add(Variant.PureSteadyState));
        actions.put("-V:sisr", () -> variants.add(Variant.SteadyInsertionSteadyRemoval));
        actions.put("-V:bisr", () -> variants.add(Variant.BulkInsertionSteadyRemoval));
        actions.put("-V:bibr", () -> variants.add(Variant.BulkInsertionBulkRemoval));
        actions.put("-O:debsel=true",   () -> debSelection.add(true));
        actions.put("-O:debsel=false",  () -> debSelection.add(false));
        actions.put("-O:jmetal=true",   () -> jmetalComparison.add(true));
        actions.put("-O:jmetal=false",  () -> jmetalComparison.add(false));

        Set<String> knownOptions = new TreeSet<>(actions.keySet());

        for (String s : args) {
            if (usedOptions.contains(s)) {
                System.out.println("Error: Option " + s + " is specified at least twice!");
                System.exit(1);
                throw new RuntimeException();
            }
            Runnable a = actions.get(s);
            if (a == null) {
                System.out.println("Error: Option " + s + " is unknown!");
                System.out.println("Known options are:\n    " + knownOptions);
                System.exit(1);
                throw new RuntimeException();
            }
            a.run();
        }

        if (suppliers.isEmpty() || variants.isEmpty() || debSelection.isEmpty() || jmetalComparison.isEmpty()) {
            System.out.println("Error: Empty set of tested configurations!");
            System.out.println("Known options are:\n    " + knownOptions);
            System.exit(1);
            throw new RuntimeException();
        }
        Config config = new Config(suppliers, variants, debSelection, jmetalComparison);

        new File("runs").mkdirs();

        config.run(ZDT1.instance());
        config.run(ZDT2.instance());
        config.run(ZDT3.instance());
        config.run(ZDT4.instance());
        config.run(ZDT6.instance());

        config.run(DTLZ1.instance());
        config.run(DTLZ2.instance());
        config.run(DTLZ3.instance());
        config.run(DTLZ4.instance());
        config.run(DTLZ5.instance());
        config.run(DTLZ6.instance());
        config.run(DTLZ7.instance());

        config.run(WFG1.instance());
        config.run(WFG2.instance());
        config.run(WFG3.instance());
        config.run(WFG4.instance());
        config.run(WFG5.instance());
        config.run(WFG6.instance());
        config.run(WFG7.instance());
        config.run(WFG8.instance());
        config.run(WFG9.instance());
    }
}
