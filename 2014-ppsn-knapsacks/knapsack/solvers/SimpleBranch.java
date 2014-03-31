package knapsack.solvers;

import java.util.*;
import knapsack.*;

/**
 * This is a recursive solver with very simple bounds.
 * @author Maxim Buzdalov
 */
public final class SimpleBranch implements KnapsackSolver {
    private static SimpleBranch instance = new SimpleBranch();
    private SimpleBranch() {}

    public static SimpleBranch getInstance() {
        return instance;
    }

    @Override
    public KnapsackResultEx solve(ProblemInstance problem) {
        return new Implementation(problem).getResult();
    }

    @Override
    public String getName() {
        return "SimpleBranchAndBound";
    }

    private static final Comparator<Item> itemPricePerWeightComparator = new Comparator<Item>() {
        @Override
        public int compare(Item o1, Item o2) {
            return -(o1.value * o2.weight - o1.weight * o2.value);
        }
    };

    private static class Implementation {
        protected final KnapsackResultEx result;

        protected int n;
        protected Item[] items;
        protected int capacity;

        protected long opCount;
        protected boolean[] used;
        protected boolean[] optimal;
        protected int optimalAnswer = -1;
        protected int[] partSumW;
        protected int[] partSumV;

        void go(int index, int sumw, int sumv) {
            ++opCount;
            if (index == n) {
                if (sumv > optimalAnswer) {
                    optimalAnswer = sumv;
                    System.arraycopy(used, 0, optimal, 0, n);
                } else {
                    throw new AssertionError("Some cutoffs do not work");
                }
            }
            if (sumv + partSumV[index] <= optimalAnswer) {
                return;
            }
            used[index] = true;
            if (sumw + items[index].weight <= capacity) {
                go(index + 1, sumw + items[index].weight, sumv + items[index].value);
            }
            used[index] = false;
            if (sumw + partSumW[index] > capacity && sumv + partSumV[index + 1] > optimalAnswer) {
                go(index + 1, sumw, sumv);
            }
        }

        public Implementation(ProblemInstance problem) {
            n = problem.getItems().size();
            items = problem.getItems().toArray(new Item[n]);
            used = new boolean[n];
            optimal = new boolean[n];
            capacity = problem.getCapacity();
            Arrays.sort(items, itemPricePerWeightComparator);
            partSumW = new int[n + 1];
            partSumV = new int[n + 1];
            for (int i = n - 1; i >= 0; --i) {
                partSumW[i] = items[i].weight;
                partSumV[i] = items[i].value;
                partSumW[i] += partSumW[i + 1];
                partSumV[i] += partSumV[i + 1];
            }
            go(0, 0, 0);
            List<Item> ret = new ArrayList<>();
            for (int i = 0; i < n; ++i) {
                if (optimal[i]) {
                    ret.add(items[i]);
                }
            }
            result = new KnapsackResultEx(ret, opCount, null);
        }

        public KnapsackResultEx getResult() {
            return result;
        }
    }
}
