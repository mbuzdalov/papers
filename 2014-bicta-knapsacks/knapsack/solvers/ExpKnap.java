package knapsack.solvers;

import java.util.*;
import knapsack.*;


/**
 * This is a fully implemented Pisinger's ExpKnap.
 *
 * @author Maxim Buzdalov
 */
public final class ExpKnap implements KnapsackSolver {
    private static final ExpKnap instance = new ExpKnap();
    private ExpKnap() {}

    public static KnapsackSolver getInstance() {
        return instance;
    }

    @Override
    public KnapsackResultEx solve(ProblemInstance problem) {
        return new Implementation(problem).getResult();
    }

    @Override
    public String getName() {
        return "ExpKnap";
    }

    private static class Interval implements Comparable<Interval> {
        public final int min, max;

        public Interval(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public String toString() {
            return "[" + min + ".." + max + "]";
        }

        @Override
        public int compareTo(Interval o) {
            if (min == o.min && max == o.max) {
                return 0;
            }
            if (max <= o.min || o.max <= min) {
                return min < o.max ? -1 : 1;
            }
            throw new AssertionError("Intervals " + this + " and " + o + " are incomparable");
        }
    }

    private static class Implementation {
        protected final Item[] items;
        protected final int n;
        protected final int capacity;
        protected final int breakItem;
        
        protected int foundSolution;

        protected final int costSumBeforeBreakItem;
        protected int remainingCapacity;

        protected int coreMin; /** sigma from the paper */
        protected int coreMax; /** tau from the paper */
        protected int partSortMin; /** phi from the paper */
        protected int partSortMax; /** psi from the paper */
        protected int partSortSum; /** nu from the paper */

        protected final TreeSet<Interval> hiIntervals = new TreeSet<>();
        protected final TreeSet<Interval> loIntervals = new TreeSet<>();
        protected final Set<Item> exceptions = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>());

        protected long operationCount;

        protected final KnapsackResultEx solution;

        protected static int detSgn(int a, int b, int c, int d) {
            return Long.signum((long) (a) * d - (long) (b) * c);
        }

        protected static int compare(Item lhs, Item rhs) {
            return detSgn(lhs.weight, lhs.value, rhs.weight, rhs.value);
        }

        protected void swap(int i, int j) {
            Item tmp = items[i];
            items[i] = items[j];
            items[j] = tmp;
        }

        protected void partSort(int left, int right, int sumLeft) {
            Item splitter = null;
            if (right - left > 0) {
                int mid = (left + right) >>> 1;
                if (compare(items[left], items[mid]) > 0) {
                    swap(left, mid);
                }
                if (compare(items[mid], items[right]) > 0) {
                    swap(mid, right);
                }
                if (compare(items[left], items[mid]) > 0) {
                    swap(left, mid);
                }
                splitter = items[mid];
            }
            if (right - left < 3) {
                partSortSum = sumLeft;
                partSortMin = left;
                partSortMax = right;
            } else {
                int newSumLeft = sumLeft;
                int from = left, to = right;
                do {
                    do {
                        newSumLeft += items[from].weight;
                        ++from;
                    } while (compare(items[from], splitter) < 0);
                    do {
                        --to;
                    } while (compare(items[to], splitter) > 0);
                    if (from < to) {
                        swap(from, to);
                    }
                } while (from <= to);
                if (newSumLeft > capacity) {
                    loIntervals.add(new Interval(from, right));
                    partSort(left, from - 1, sumLeft);
                } else {
                    hiIntervals.add(new Interval(left, from - 1));
                    partSort(from, right, newSumLeft);
                }
            }
        }

        protected Interval reduce(Interval interval) {
            int min = interval.min;
            int max = interval.max;

            if (max < breakItem) {
                int k = coreMin - 1;
                while (min <= max) {
                    if (detSgn(
                            foundSolution + 1 - costSumBeforeBreakItem + items[max].value,
                            remainingCapacity + items[max].weight,
                            items[breakItem].value,
                            items[breakItem].weight
                    ) > 0) {
                        swap(min++, max);
                    } else {
                        swap(max--, k--);
                    }
                }
                if (k == coreMin - 1) {
                    swap(k--, max);
                }
                min = k + 1;
                max = coreMin - 1;
            } else {
                int k = coreMax + 1;
                while (min <= max) {
                    if (detSgn(
                            foundSolution + 1 - costSumBeforeBreakItem - items[min].value,
                            remainingCapacity - items[min].weight,
                            items[breakItem].value,
                            items[breakItem].weight
                    ) > 0) {
                        swap(min, max--);
                    } else {
                        swap(min++, k++);
                    }
                }
                if (k == coreMax + 1) {
                    swap(k++, min);
                }
                min = coreMax + 1;
                max = k - 1;
            }

            return new Interval(min, max);
        }

        protected void expand(Interval interval) {
            interval = reduce(interval);
            partSort(interval.min, interval.max, interval.max < breakItem ? 0 : capacity);

            coreMin = Math.min(coreMin, partSortMin);
            coreMax = Math.max(coreMax, partSortMax);
        }

        protected boolean expBranch(int sumv, int sumw, int left, int right) {
            ++operationCount;
            boolean improved = false;
            if (sumw <= capacity) {
                if (sumv > foundSolution) {
                    improved = true;
                    foundSolution = sumv;
                    exceptions.clear();
                }
                while (true) {
                    if (right > coreMax) {
                        Interval minLo = loIntervals.pollFirst();
                        expand(minLo);
                    }
                    Item it = items[right];
                    if (detSgn(sumv - foundSolution - 1, sumw - capacity, it.value, it.weight) < 0) {
                        return improved;
                    }
                    if (expBranch(sumv + it.value, sumw + it.weight, left, right + 1)) {
                        improved = true;
                        exceptions.add(it);
                    }
                    ++right;
                }
            } else {
                while (true) {
                    if (left < coreMin) {
                        Interval maxHi = hiIntervals.pollLast();
                        expand(maxHi);
                    }
                    Item it = items[left];
                    if (detSgn(sumv - foundSolution - 1, sumw - capacity, it.value, it.weight) < 0) {
                        return improved;
                    }
                    if (expBranch(sumv - it.value, sumw - it.weight, left - 1, right)) {
                        improved = true;
                        exceptions.add(it);
                    }
                    --left;
                }
            }
        }

        public KnapsackResultEx getResult() {
            return solution;
        }

        public Implementation(ProblemInstance problem) {
            List<Item> is = new ArrayList<>();
            List<Item> zeroWeights = new ArrayList<>();
            for (Item i : problem.getItems()) {
                if (i.value > 0) {
                    if (i.weight == 0) {
                        zeroWeights.add(i);
                    } else {
                        is.add(i);
                    }
                }
            }
            this.n = is.size();
            items = new Item[n + 2];
            for (int i = 0; i < n; ++i) {
                Item z = is.get(i);
                //we are making these items non-equal.
                items[i + 1] = new Item(z.weight, z.value);
            }
            Item leftmost = items[0] = new Item(0, 1);
            Item rightmost = items[n + 1] = new Item(1, 0);
            capacity = problem.getCapacity();

            hiIntervals.add(new Interval(0, 0));
            loIntervals.add(new Interval(n + 1, n + 1));
            partSort(1, n, 0);                        
            
            coreMin = partSortMin;
            coreMax = partSortMax;

            int b = coreMin;
            remainingCapacity = capacity - partSortSum;

            while (items[b].weight <= remainingCapacity && b <= n) {
                remainingCapacity -= items[b++].weight;
            }

            if (b > n) {
                //nothing interesting, returning.
                solution = new KnapsackResultEx(Arrays.asList(items).subList(1, n + 1), 0, null);
                breakItem = n + 1;
                costSumBeforeBreakItem = solution.value;
                return;
            }

            breakItem = b;

            int sumw = 0;
            int sumv = 0;

            int bestExtraItem = -1;
            for (int i = 1; i < breakItem; ++i) {
                sumv += items[i].value;
                sumw += items[i].weight;
            }

            costSumBeforeBreakItem = sumv;
            foundSolution = -1;

            for (int extraItem = 1; extraItem <= n; ++extraItem) {
                int myValue = -1;
                if (extraItem > breakItem) {
                    if (sumw + items[extraItem].weight <= capacity) {
                        myValue = sumv + items[extraItem].value;
                    }
                } else {
                    if (sumw + items[breakItem].weight - items[extraItem].weight <= capacity) {
                        myValue = sumv + items[breakItem].value - items[extraItem].value;
                    }
                }
                if (myValue > foundSolution) {
                    foundSolution = myValue;
                    bestExtraItem = extraItem;
                }
            }

            if (bestExtraItem < breakItem) {
                exceptions.add(items[bestExtraItem]);
                exceptions.add(items[breakItem]);
            } else if (bestExtraItem > breakItem) {
                exceptions.add(items[bestExtraItem]);
            }

            expBranch(sumv, sumw, breakItem - 1, breakItem);

            List<Item> answer = new ArrayList<>(zeroWeights);
            for (int i = 0; i <= n + 1; ++i) {
                if (i < breakItem != exceptions.contains(items[i])) {
                    //expand may move leftmost and rightmost from their initial positions
                    //this is legal for the algorithm.
                    if (items[i] != leftmost && items[i] != rightmost) {
                        answer.add(items[i]);
                    }
                }
            }

            solution = new KnapsackResultEx(answer, operationCount, null);
        }
    }
}
