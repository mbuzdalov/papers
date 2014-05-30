package knapsack.solvers;

import java.util.*;
import knapsack.*;

/**
 * This is a knapsack solver that utilizes Pisinger's ExpBranch procedure of ExpKnap algorithm.
 *
 * This doesn't use any initial heuristic except for pure greedy solution.
 *
 * Also, the array is presorted (doesn't matter for our sizes) and no reducing is used.
 *
 * @author Maxim Buzdalov
 */
public final class ExpKnapPart implements KnapsackSolver {
    private static final ExpKnapPart INSTANCE = new ExpKnapPart();
    private ExpKnapPart() {}
    public static ExpKnapPart getInstance() {
        return INSTANCE;
    }

    private static Comparator<Item> itemPricePerWeightComparator = new Comparator<Item>() {
        @Override
        public int compare(Item o1, Item o2) {
            return -(o1.value * o2.weight - o1.weight * o2.value);
        }
    };

    @Override
    public KnapsackResultEx solve(ProblemInstance problem) {
        List<Item> zeroWeights = new ArrayList<>();
        List<Item> nonZero = new ArrayList<>();
        for (Item i : problem.getItems()) {
            if (i.value > 0) {
                if (i.weight == 0) {
                    zeroWeights.add(i);
                } else {
                    nonZero.add(i);
                }
            }
        }
        final Item[] is = nonZero.toArray(new Item[nonZero.size()]);
        Arrays.sort(is, itemPricePerWeightComparator);
        int sumw = 0;
        int sumv = 0;
        int breakItem = 0;
        final int capacity = problem.getCapacity();
        while (breakItem < is.length && sumw + is[breakItem].weight <= capacity) {
            sumw += is[breakItem].weight;
            sumv += is[breakItem].value;
            ++breakItem;
        }
        if (breakItem == is.length) {
            //nothing interesting.
            return new KnapsackResultEx(Arrays.asList(is), 0, null);
        }

        final List<Item> rv = new ArrayList<>(zeroWeights);
        final int B = breakItem;
        final int W = sumw;
        final int V = sumv;

        final long[] opCount = new long[1];

        new Runnable() {
            int foundSolution = V;
            Set<Integer> exceptions = new HashSet<>();

            int detComp(int a11, int a12, int a21, int a22) {
                return Long.signum((long)a11 * a22 - (long)a21 * a12);
            }

            boolean expBranch(int v, int w, int left, int right) {
                opCount[0]++;
                boolean improved = false;
                if (w <= capacity) {
                    if (v > foundSolution) {
                        improved = true;
                        foundSolution = v;
                        exceptions.clear();
                    }
                    while (right < is.length) {
                        if (detComp(v - foundSolution - 1, w - capacity, is[right].value, is[right].weight) < 0) {
                            return improved;
                        }
                        if (expBranch(v + is[right].value, w + is[right].weight, left, right + 1)) {
                            improved = true;
                            exceptions.add(right);
                        }
                        ++right;
                    }
                    return improved;
                } else {
                    while (left >= 0) {
                        if (detComp(v - foundSolution - 1, w - capacity, is[left].value, is[left].weight) < 0) {
                            return improved;
                        }
                        if (expBranch(v - is[left].value, w - is[left].weight, left - 1, right)) {
                            improved = true;
                            exceptions.add(left);
                        }
                        --left;
                    }
                    return improved;
                }
            }

            @Override
            public void run() {
                expBranch(V, W, B - 1, B);
                for (int i = 0; i < is.length; ++i) {
                    if ((i < B) != exceptions.contains(i)) {
                        rv.add(is[i]);
                    }
                }
            }
        }.run();

        return new KnapsackResultEx(rv, opCount[0], null);
    }

    @Override
    public String getName() {
        return "ExpKnapPart";
    }
}
