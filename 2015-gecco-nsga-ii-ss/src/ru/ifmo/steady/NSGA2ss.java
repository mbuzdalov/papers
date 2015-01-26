package ru.ifmo.steady;

import java.util.Iterator;
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
        Random r = FastRandom.threadLocal();
        int n = a.length;
        if (b.length != n) {
            throw new IllegalArgumentException("Lengths are not equal");
        }
        if (r.nextDouble() < 0.1) {
            return a;
        }
        double[] rv = new double[n]; // the first offspring
        for (int i = 0; i < n; ++i) {
            if (r.nextBoolean()) {
                if (Math.abs(a[i] - b[i]) > 1e-14) {
                    double y1 = Math.min(a[i], b[i]);
                    double y2 = Math.max(a[i], b[i]);
                    boolean q = r.nextBoolean();
                    double beta = 1 + 2 * (q ? y1 : 1 - y2) / (y2 - y1);
                    double alpha = 2 - Math.pow(beta, -crossoverEta - 1);
                    double rand = r.nextDouble();
                    double betaq;
                    if (rand <= 1 / alpha) {
                        betaq = Math.pow(rand * alpha, 1 / (crossoverEta + 1));
                    } else {
                        betaq = Math.pow(1 / (2 - rand * alpha), 1 / (crossoverEta + 1));
                    }
                    rv[i] = 0.5 * ((y1 + y2) + (q ? -1 : 1) * betaq * (y2 - y1));
                    rv[i] = Math.max(0, Math.min(1, rv[i]));
                } else {
                    rv[i] = a[i];
                }
            } else {
                rv[i] = b[i];
            }
        }
        return rv;
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

    public double currentHyperVolume() {
        return storage.hyperVolume(
                problem.frontMinX(), problem.frontMaxX(),
                problem.frontMinY(), problem.frontMaxY()
        );
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
