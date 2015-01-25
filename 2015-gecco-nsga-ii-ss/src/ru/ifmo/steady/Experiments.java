package ru.ifmo.steady;

import ru.ifmo.steady.problem.*;

public class Experiments {
    private static void run(SolutionStorage storage, Problem problem) {
        System.out.println("   for " + problem.getName());
        NSGA2ss algo = new NSGA2ss(problem, storage, 100);
        Solution.comparisons = 0;

        long startTime = System.currentTimeMillis();
        algo.initialize();
        for (int i = 0; i < 25000; ++i) {
            algo.performIteration();
        }
        long finishTime = System.currentTimeMillis();

        double hyperVolume = algo.currentHyperVolume();
        long time = finishTime - startTime;
        long comparisons = Solution.comparisons;
        System.out.println("      HV   = " + hyperVolume);
        System.out.println("      time = " + time);
        System.out.println("      cmps = " + comparisons);
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
        run(new ru.ifmo.steady.enlu.Storage());
        run(new ru.ifmo.steady.inds.Storage());
    }
}
