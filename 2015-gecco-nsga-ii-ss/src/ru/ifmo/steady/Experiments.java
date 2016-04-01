package ru.ifmo.steady;

import java.io.*;
import java.util.*;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import ru.ifmo.steady.problem.*;
import ru.ifmo.steady.util.FastRandom;
import ru.ifmo.steady.NSGA2.Variant;

public class Experiments {
    private static final double orderStat(double[] a, double ratio) {
        double idx0 = (a.length - 1) * ratio;
        int idxLo = (int) Math.floor(idx0);
        int idxHi = (int) Math.ceil(idx0);
        return a[idxLo] * (idxHi - idx0) + a[idxHi] * (idx0 - idxLo);
    }

    private static final double med(double[] a) {
        return orderStat(a, 0.5);
    }

    private static final double iqr(double[] a) {
        return orderStat(a, 0.75) - orderStat(a, 0.25);
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
        public final double[] hyperVolumes;
        public final double[] comparisons;
        public final double[] runningTimes;

        public final double hyperVolumeMed;
        public final double hyperVolumeIQR;
        public final double comparisonMed;
        public final double comparisonIQR;
        public final double runningTimeMed;
        public final double runningTimeIQR;

        public RunResult(Problem problem, Supplier<SolutionStorage> storageSupplier,
                         boolean debSelection, boolean jmetalComparison, Variant variant,
                         String runDir, int budget, int generationSize, int runs) {
            hyperVolumes = new double[runs];
            comparisons = new double[runs];
            runningTimes = new double[runs];
            double[] compensationTimes = new double[runs];

            IntStream.range(0, runs).parallel().forEach(t -> {
                FastRandom.geneticThreadLocal().setSeed(t + 41117);
                SolutionStorage storage = storageSupplier.get();
                NSGA2 algo = new NSGA2(problem, storage, generationSize,
                                       debSelection, jmetalComparison, variant);
                long startTime = System.nanoTime();
                algo.initialize();
                for (int i = generationSize; i < budget; i += generationSize) {
                    algo.performIteration();
                }
                long finishTime = System.nanoTime();

                hyperVolumes[t] = algo.currentHyperVolume();
                comparisons[t]  = storage.getComparisonCounter().get();
                runningTimes[t] = (finishTime - startTime) / 1e9;

                long startSimTime = System.nanoTime();
                for (int i = generationSize; i < budget; i += generationSize) {
                    algo.simulateIteration();
                }
                long finishSimTime = System.nanoTime();
                compensationTimes[t] = (finishSimTime - startSimTime) / 1e9;
            });

            Arrays.sort(hyperVolumes);
            Arrays.sort(comparisons);
            Arrays.sort(runningTimes);
            Arrays.sort(compensationTimes);

            for (int i = 0; i < runs; ++i) {
                runningTimes[i] -= compensationTimes[i];
            }
            Arrays.sort(runningTimes);

            String namePrefix = String.format("%s/%d-%d/%s-%s-%d-%d-%s",
                runDir,
                budget, generationSize,
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
        private final List<Integer> budgets;
        private final List<Integer> generationSizes;
        private final String runDir;
        private final int runs;

        public Config(List<Supplier<SolutionStorage>> suppliers,
                      List<Variant> variants,
                      List<Boolean> debSelectionOptions,
                      List<Boolean> jmetalComparisonOptions,
                      List<Integer> budgets,
                      List<Integer> generationSizes,
                      String runDir,
                      int runs) {
            this.suppliers = suppliers;
            this.variants = variants;
            this.debSelectionOptions = debSelectionOptions;
            this.jmetalComparisonOptions = jmetalComparisonOptions;
            this.budgets = budgets;
            this.generationSizes = generationSizes;
            this.runDir = runDir;
            this.runs = runs;
        }

        public void run(Problem problem) {
            for (int bgs = 0; bgs < budgets.size(); ++bgs) {
                int budget = budgets.get(bgs);
                int generationSize = generationSizes.get(bgs);
                System.out.println("====================================================");
                System.out.printf("| %-4s | Budget %-9d | Generation size %-6d |\n", problem.getName(), budget, generationSize);
                System.out.println("====================================================");

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
                                results[i] = new RunResult(problem, suppliers.get(i), debSelection,
                                                           jmetalComparison, variant, runDir, budget,
                                                           generationSize, runs);
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
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        List<Supplier<SolutionStorage>> suppliers = new ArrayList<>();
        List<Variant> variants = new ArrayList<>();
        List<Boolean> debSelection = new ArrayList<>();
        List<Boolean> jmetalComparison = new ArrayList<>();
        List<String> runDir = new ArrayList<>();
        List<Integer> runs = new ArrayList<>();
        List<Integer> budgets = new ArrayList<>();
        List<Integer> generationSizes = new ArrayList<>();

        Set<String> usedOptions = new HashSet<>();
        Map<String, Runnable> actions = new HashMap<>();
        Map<String, Consumer<String>> setters = new HashMap<>();

        actions.put("-S:inds-lasthull", () -> suppliers.add(() -> new ru.ifmo.steady.inds.StorageLastHull()));
        actions.put("-S:inds-allhulls", () -> suppliers.add(() -> new ru.ifmo.steady.inds.StorageAllHulls()));

        actions.put("-S:inds", () -> suppliers.add(() -> new ru.ifmo.steady.inds.Storage()));
        actions.put("-S:enlu", () -> suppliers.add(() -> new ru.ifmo.steady.enlu.Storage()));
        actions.put("-S:deb",  () -> suppliers.add(() -> new ru.ifmo.steady.debNDS.Storage()));
        actions.put("-V:pss",  () -> variants.add(Variant.PureSteadyState));
        actions.put("-V:sisr", () -> variants.add(Variant.SteadyInsertionSteadyRemoval));
        actions.put("-V:bisr", () -> variants.add(Variant.BulkInsertionSteadyRemoval));
        actions.put("-V:bibr", () -> variants.add(Variant.BulkInsertionBulkRemoval));
        actions.put("-O:debselTrue",   () -> debSelection.add(true));
        actions.put("-O:debselFalse",  () -> debSelection.add(false));
        actions.put("-O:jmetalTrue",   () -> jmetalComparison.add(true));
        actions.put("-O:jmetalFalse",  () -> jmetalComparison.add(false));

        setters.put("-D", (dir) -> runDir.add(dir));
        setters.put("-R", (r) -> { try {
            runs.add(Integer.parseInt(r));
        } catch (NumberFormatException ex) {
            System.out.println("Error: " + r + " is not a number!");
            runs.clear();
        }});
        setters.put("-N", (s) -> { try {
            int colon = s.indexOf(':');
            if (colon == -1) {
                throw new NumberFormatException();
            }
            int budget = Integer.parseInt(s.substring(0, colon));
            int generationSize = Integer.parseInt(s.substring(colon + 1));
            budgets.add(budget);
            generationSizes.add(generationSize);
        } catch (NumberFormatException ex) {
            System.out.println("Error: option -N expects the argument <budget>:<generationSize>, '" + s + "' found");
            runs.clear();
        }});

        Set<String> knownOptions = new TreeSet<>(actions.keySet());
        knownOptions.add("-D=<run-dir>");
        knownOptions.add("-R=<run-count>");
        knownOptions.add("-N=<budget>:<generationSize>");

        for (String s : args) {
            int eq = s.indexOf('=');
            String value = null;
            if (eq != -1) {
                value = s.substring(eq + 1);
                s = s.substring(0, eq);
            }
            if (usedOptions.contains(s)) {
                System.out.println("Error: Option " + s + " is specified at least twice!");
                System.exit(1);
                throw new RuntimeException();
            }
            if (value == null) {
                Runnable a = actions.get(s);
                if (a == null) {
                    System.out.println("Error: Option " + s + " is unknown!");
                    System.out.println("Known options are:\n    " + knownOptions);
                    System.exit(1);
                    throw new RuntimeException();
                }
                a.run();
            } else {
                Consumer<String> a = setters.get(s);
                if (a == null) {
                    System.out.println("Error: Option " + s + " is unknown!");
                    System.out.println("Known options are:\n    " + knownOptions);
                    System.exit(1);
                    throw new RuntimeException();
                }
                a.accept(value);
            }
        }

        if (suppliers.isEmpty() || variants.isEmpty() || debSelection.isEmpty() || jmetalComparison.isEmpty()
            || runDir.isEmpty() || runs.isEmpty()) {
            System.out.println("Error: Empty set of tested configurations!");
            System.out.println("Known options are:\n    " + knownOptions);
            System.exit(1);
            throw new RuntimeException();
        }

        Config config = new Config(suppliers, variants, debSelection, jmetalComparison, budgets, generationSizes, runDir.get(0), runs.get(0));
        new File(runDir.get(0)).mkdirs();
        for (int bgs = 0; bgs < budgets.size(); ++bgs) {
            new File(runDir.get(0), budgets.get(bgs) + "-" + generationSizes.get(bgs)).mkdir();
        }

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
