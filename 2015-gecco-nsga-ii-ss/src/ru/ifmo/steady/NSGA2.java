package ru.ifmo.steady;

import java.util.*;
import java.io.*;

import ru.ifmo.steady.util.FastRandom;

public class NSGA2 {
    public static enum Variant {
        PureSteadyState("PSS"),
        SteadyInsertionSteadyRemoval("SISR"),
        BulkInsertionSteadyRemoval("BISR"),
        BulkInsertionBulkRemoval("BIBR");

        private final String shortName;
        private Variant(String shortName) {
            this.shortName = shortName;
        }
        public String shortName() {
            return shortName;
        }

        private static final List<Variant> all = Arrays.asList(values());
        public static List<Variant> all() {
            return all;
        }
    }

    private static final double mutationEta = 20;
    private static final double crossoverEta = 20;
    private static final double EPS = 1e-15;

    private final Problem problem;
    private final SolutionStorage storage;
    private final int storageSize;
    private final boolean jmetalComparison;
    private final Variant variant;

    private int evaluations;
    private final double mutationProbability;
    private int[] permutation;
    private int index;

    public NSGA2(Problem problem, SolutionStorage storage, int storageSize,
                 boolean debSelection, boolean jmetalComparison, Variant variant) {
        this.problem = problem;
        this.storage = storage;
        this.storageSize = storageSize;
        this.mutationProbability = 1.0 / problem.inputDimension();
        this.jmetalComparison = jmetalComparison;
        this.variant = variant;
        if (debSelection) {
            permutation = new int[storageSize];
            for (int i = 0; i < storageSize; ++i) {
                permutation[i] = i;
            }
        }
    }

    public void dump(String fileName) {
        try (PrintWriter out = new PrintWriter(fileName)) {
            int layers = storage.getLayerCount();
            for (int layer = 0; layer < layers; ++layer) {
                Iterator<Solution> f = storage.getLayer(layer);
                out.println(layer + ":");
                while (f.hasNext()) {
                    Solution s = f.next();
                    out.println(s.getNormalizedX(0, 1) + " " + s.getNormalizedY(0, 1));
                }
                out.println();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
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

    private SolutionStorage.QueryResult selectOne() {
        if (permutation == null) {
            return storage.getRandom();
        } else {
            if (index == 0) {
                Random r = FastRandom.geneticThreadLocal();
                for (int i = 1; i < storageSize; ++i) {
                    int j = r.nextInt(i + 1);
                    if (j != i) {
                        int tmp = permutation[i];
                        permutation[i] = permutation[j];
                        permutation[j] = tmp;
                    }
                }
            }
            SolutionStorage.QueryResult q = storage.getKth(permutation[index]);
            index = (index + 1) % storageSize;
            return q;
        }
    }

    private double[] select() {
        SolutionStorage.QueryResult q1 = selectOne();
        SolutionStorage.QueryResult q2 = selectOne();
        if (jmetalComparison) {
            ComparisonCounter cc = storage.getComparisonCounter();
            int cmpx = q1.solution.compareX(q2.solution, cc);
            int cmpy = q1.solution.compareY(q2.solution, cc);
            if (cmpx <= 0 && cmpy < 0 || cmpx < 0 && cmpy <= 0) {
                return q1.solution.getInput();
            } else if (cmpx >= 0 && cmpy > 0 || cmpx > 0 && cmpy >= 0) {
                return q2.solution.getInput();
            } else if (q1.crowdingDistance > q2.crowdingDistance) {
                return q1.solution.getInput();
            } else {
                return q2.solution.getInput();
            }
        } else {
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
    }

    // SBX crossover from Deb
    private double[][] crossover(double[] a, double[] b, int howManyNeeded) {
        Random r = FastRandom.geneticThreadLocal();
        int n = a.length;
        if (b.length != n) {
            throw new IllegalArgumentException("Lengths are not equal");
        }
        if (r.nextDouble() < 0.1) {
            return new double[][] {
                a.clone(),
                howManyNeeded == 1 ? null : b.clone()
            };
        }
        double[][] rv = new double[2][];
        for (int i = 0; i < howManyNeeded; ++i) {
            rv[i] = new double[n];
        }
        for (int i = 0; i < n; ++i) {
            if (r.nextBoolean()) {
                if (Math.abs(a[i] - b[i]) > EPS) {
                    double y1 = Math.min(a[i], b[i]);
                    double y2 = Math.max(a[i], b[i]);

                    double rand = r.nextDouble();
                    boolean swap = r.nextBoolean();
                    for (int t = 0; t < howManyNeeded; ++t) {
                        boolean q = (t == 0) ^ swap;
                        double beta = 1 + 2 * (q ? y1 : 1 - y2) / (y2 - y1);
                        double alpha = 2 - Math.pow(beta, -crossoverEta - 1);
                        double betaq;
                        if (rand <= 1 / alpha) {
                            betaq = Math.pow(rand * alpha, 1 / (crossoverEta + 1));
                        } else {
                            betaq = Math.pow(1 / (2 - rand * alpha), 1 / (crossoverEta + 1));
                        }
                        double res = 0.5 * ((y1 + y2) + (q ? -1 : 1) * betaq * (y2 - y1));
                        rv[t][i] = Math.max(0, Math.min(1, res));
                    }
                } else {
                    rv[0][i] = a[i];
                    if (howManyNeeded > 1) {
                        rv[1][i] = b[i];
                    }
                }
            } else {
                rv[0][i] = b[i];
                if (howManyNeeded > 1) {
                    rv[1][i] = a[i];
                }
            }
        }
        return rv;
    }

    // polynomial mutation from Deb
    private void mutation(double[] ind) {
        Random r = FastRandom.geneticThreadLocal();
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
        if (variant == Variant.PureSteadyState) {
            for (int i = 0; i < storageSize; ++i) {
                double[][] cross = crossover(select(), select(), 1);
                mutation(cross[0]);
                Solution solution = problem.evaluate(cross[0]);
                ++evaluations;
                storage.add(solution);
                storage.removeWorst(1);
            }
        } else {
            Solution[] sols = new Solution[storageSize];
            for (int i = 0; i < storageSize; i += 2) {
                int remain = Math.min(2, storageSize - i);
                double[][] cross = crossover(select(), select(), remain);
                for (int t = 0; t < remain; ++t) {
                    mutation(cross[t]);
                    sols[i + t] = problem.evaluate(cross[t]);
                    ++evaluations;
                }
            }
            switch (variant) {
                case SteadyInsertionSteadyRemoval: {
                    for (Solution s : sols) {
                        storage.add(s);
                        storage.removeWorst(1);
                    }
                } break;
                case BulkInsertionSteadyRemoval: {
                    storage.addAll(sols);
                    storage.removeWorst(storageSize);
                } break;
                case BulkInsertionBulkRemoval: {
                    storage.addAll(sols);
                    storage.removeWorstDebCompatible(storageSize);
                } break;
            }
        }
    }

    public List<Solution> paretoFront() {
        List<Solution> rv = new ArrayList<>();
        Iterator<Solution> it = storage.nonDominatedSolutionsIncreasingX();
        while (it.hasNext()) {
            rv.add(it.next());
        }
        return rv;
    }
}
