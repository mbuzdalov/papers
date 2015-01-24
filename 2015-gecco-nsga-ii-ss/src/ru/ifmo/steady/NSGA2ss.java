package ru.ifmo.steady;

import java.util.Random;
import ru.ifmo.steady.util.FastRandom;

public class NSGA2ss {
    private static final double mutationEta = 20;
    private static final double crossoverEta = 20;

    private final Problem problem;
    private final SolutionStorage storage;
    private final int generationSize;
    private int evaluations;
    private final double mutationProbability;

    public NSGA2ss(Problem problem, SolutionStorage storage, int generationSize) {
        this.problem = problem;
        this.storage = storage;
        this.generationSize = generationSize;
        this.mutationProbability = 1.0 / problem.inputDimension();
    }

    public void initialize() {
        storage.clear();
        evaluations = 0;
        for (int i = 0; i < generationSize; ++i) {
            double[] ind = problem.generate();
            Solution sol = problem.evaluate(ind);
            ++evaluations;
            storage.add(sol);
        }
    }

    private double[] select() {
        SolutionStorage.QueryResult q1 = storage.getRandom();
        SolutionStorage.QueryResult q2 = storage.getRandom();
        if (q1.layer > q2.layer) {
            return q2.solution.getInput();
        } else if (q1.layer < q2.layer) {
            return q1.solution.getInput();
        } else if (q1.crowdingDistance > q2.crowdingDistance) {
            return q1.solution.getInput();
        } else {
            return q2.solution.getInput();
        }
    }

    // SBX crossover from Deb
    private double[] crossover(double[] a, double[] b) {
        throw new UnsupportedOperationException("Please implement this crossover!");
    }

    // polynomial mutation from Deb
    private void mutation(double[] ind) {
        Random r = FastRandom.threadLocal();
        for (int i = 0; i < ind.length; ++i) {
            if (r.nextDouble() < mutationProbability) {
                double v = ind[i];
                double d1 = v, d2 = 1 - v;
                double rnd = r.nextDouble();
                double mutPow = 1 / (mutationEta + 1);
                double deltaQ;
                if (rnd < 0.5) {
                    double val = 2 * rnd + (1 - 2 * rnd) * Math.pow(d2, 1 + mutationEta);
                    deltaQ = Math.pow(val, mutPow) - 1;
                } else {
                    double val = 2 * (1 - rnd) + (2 * rnd - 1) * Math.pow(d1, 1 + mutationEta);
                    deltaQ = 1 - Math.pow(val, mutPow);
                }
                ind[i] = Math.max(0, Math.min(1, v + deltaQ));
            }
        }
    }

    public void performIteration() {
        double[] ind = crossover(select(), select());
        mutation(ind);
        Solution s = problem.evaluate(ind);
        ++evaluations;
        storage.add(s);
        storage.removeWorst();
    }
}
