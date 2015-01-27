package ru.ifmo.steady;

import java.util.Iterator;
import java.util.Random;

import ru.ifmo.steady.util.FastRandom;

public class NSGA2 {
    private static final double mutationEta = 20;
    private static final double crossoverEta = 20;

    private final Problem problem;
    private final SolutionStorage storage;
    private final int storageSize;
    private final int iterationSize;
    private int evaluations;
    private final double mutationProbability;

    public NSGA2(Problem problem, SolutionStorage storage, int storageSize, int iterationSize) {
        this.problem = problem;
        this.storage = storage;
        this.storageSize = storageSize;
        this.iterationSize = iterationSize;
        this.mutationProbability = 1.0 / problem.inputDimension();
    }

    public void initialize() {
        storage.clear();
        evaluations = 0;
        for (int i = 0; i < storageSize; ++i) {
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
    private double[][] crossover(double[] a, double[] b, int howManyNeeded) {
        Random r = FastRandom.threadLocal();
        int n = a.length;
        if (b.length != n) {
            throw new IllegalArgumentException("Lengths are not equal");
        }
        if (r.nextDouble() < 0.1) {
            return new double[][] { a.clone(), b.clone() };
        }
        double[][] rv = new double[2][n];
        for (int i = 0; i < n; ++i) {
            if (r.nextBoolean()) {
                if (Math.abs(a[i] - b[i]) > 1e-14) {
                    double y1 = Math.min(a[i], b[i]);
                    double y2 = Math.max(a[i], b[i]);

                    boolean swap = r.nextBoolean();
                    for (int t = 0; t < howManyNeeded; ++t) {
                        boolean q = (t == 0) ^ swap;
                        double beta = 1 + 2 * (q ? y1 : 1 - y2) / (y2 - y1);
                        double alpha = 2 - Math.pow(beta, -crossoverEta - 1);
                        double rand = r.nextDouble();
                        double betaq;
                        if (rand <= 1 / alpha) {
                            betaq = Math.pow(rand * alpha, 1 / (crossoverEta + 1));
                        } else {
                            betaq = Math.pow(1 / (2 - rand * alpha), 1 / (crossoverEta + 1));
                        }
                        rv[t][i] = 0.5 * ((y1 + y2) + (q ? -1 : 1) * betaq * (y2 - y1));
                        rv[t][i] = Math.max(0, Math.min(1, rv[t][i]));
                    }

                } else {
                    rv[0][i] = a[i];
                    rv[1][i] = b[i];
                }
            } else {
                rv[0][i] = b[i];
                rv[1][i] = a[i];
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
        Solution[] sols = new Solution[iterationSize];
        for (int i = 0; i < iterationSize; i += 2) {
            int remain = Math.min(2, iterationSize - i);
            double[][] cross = crossover(select(), select(), remain);
            for (int t = 0; t < remain; ++t) {
                mutation(cross[t]);
                sols[i + t] = problem.evaluate(cross[t]);
                ++evaluations;
            }
        }
        storage.addAll(sols);
        storage.removeWorst(iterationSize);
    }
}
