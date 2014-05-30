package knapsack.solvers;

import java.util.*;
import knapsack.*;

/**
 * This is a Pisinger's algorithm HardKnap.
 *
 * @author Maxim Buzdalov
 */
public final class HardKnap implements KnapsackSolver {
    private static final HardKnap instance = new HardKnap();
    private HardKnap() {}

    public static HardKnap getInstance() {
        return instance;
    }

    private static <T extends Comparable<? super T>> int binSearch(
            List<T> list, int fromInc, int toExc, T what
    ) {
        while (toExc - fromInc > 1) {
            int mid = (fromInc + toExc) >>> 1;
            if (list.get(mid).compareTo(what) <= 0) {
                fromInc = mid;
            } else {
                toExc = mid;
            }
        }
        return fromInc;
    }

    private static int binSearch(int[] array, int fromInc, int toExc, int what) {
        while (toExc - fromInc > 1) {
            int mid = (fromInc + toExc) >>> 1;
            if (array[mid] <= what) {
                fromInc = mid;
            } else {
                toExc = mid;
            }
        }
        return fromInc;
    }

    @Override
    public KnapsackResultEx solve(ProblemInstance problem) {
        return new Implementation(problem).getResult();
    }

    @Override
    public String getName() {
        return "HardKnap";
    }

    private static final class State {
        public static final State EMPTY = new State();

        public final int sumw;
        public final int sumv;
        public final State left;
        public final State right;

        public State(Item i) {
            sumw = i.weight;
            sumv = i.value;
            left = right = null;
        }

        public State() {
            sumw = sumv = 0;
            left = right = null;
        }

        public State(State left, State right) {
            this.left = left;
            this.right = right;
            this.sumv = left.sumv + right.sumv;
            this.sumw = left.sumw + right.sumw;
        }

        public void dumpItems(Collection<? super Item> coll) {
            if (left != null) {
                left.dumpItems(coll);
            }
            if (right != null) {
                right.dumpItems(coll);
            }
            if (left == null && right == null) {
                coll.add(new Item(sumw, sumv));
            }
        }

        public static State getCompound(State left, State right) {
            if (left == EMPTY) return right;
            if (right == EMPTY) return left;
            return new State(left, right);
        }
    }

    private static class StateList {
        public final List<State> states;
        public final int minIndex;
        public final int maxIndex;

        public StateList(List<State> states, int minIndex, int maxIndex) {
            this.states = states;
            this.minIndex = minIndex;
            this.maxIndex = maxIndex;
        }
    }

    private static class Implementation {
        protected final int n;
        protected final Item[] items;
        protected final int capacity;
        protected final KnapsackResultEx result;

        protected int lowerBound;
        protected State bestState;

        protected long stateCount;
        protected long operationCount;

        protected int maxSizeOfStates;

        protected State[] itemStates;

        protected static int detSgn(int a, int b, int c, int d) {
            return Long.signum((long) (a) * d - (long) (b) * c);
        }

        protected static final Comparator<Item> icmp = new Comparator<Item>() {
            @Override
            public int compare(Item lhs, Item rhs) {
                return detSgn(lhs.weight, lhs.value, rhs.weight, rhs.value);
            }
        };

        protected State getRangeSum(int fromInc, int toExc) {
            if (toExc - fromInc == 1) {
                return itemStates[fromInc];
            } else if (toExc == fromInc) {
                return State.EMPTY;
            } else {
                int half = (fromInc + toExc) / 2;
                return State.getCompound(getRangeSum(fromInc, half), getRangeSum(half, toExc));
            }
        }

        protected List<State> merge(List<State> left, List<State> right) {
            int leftIdx = 0, rightIdx = 0;
            List<State> result = new ArrayList<>(left.size() + right.size());
            while (leftIdx < left.size() || rightIdx < right.size()) {
                ++operationCount;
                State curr;
                if (rightIdx == right.size() || leftIdx < left.size() && left.get(leftIdx).sumw <= right.get(rightIdx).sumw) {
                    curr = left.get(leftIdx++);
                } else {
                    curr = right.get(rightIdx++);
                }
                if (result.isEmpty()) {
                    result.add(curr);
                } else {
                    State last = result.get(result.size() - 1);
                    if (curr.sumv > last.sumv) {
                        if (curr.sumw > last.sumw) {
                            result.add(curr);
                        } else {
                            result.set(result.size() - 1, curr);
                        }
                    }
                }
            }
            return result;
        }

        protected List<State> multiply(List<State> left, List<State> right) {
            if (left.size() == 1) {
                State x = left.get(0);
                List<State> result = new ArrayList<>(right.size());
                for (State t : right) {
                    ++operationCount;
                    if (x.sumw + t.sumw <= capacity) {
                        ++stateCount;
                        result.add(State.getCompound(x, t));
                    }
                }
                return result;
            } else if (left.size() > 1) {
                int half = left.size() / 2;
                List<State> below = left.subList(0, half);
                List<State> above = left.subList(half, left.size());
                below = multiply(below, right);
                above = multiply(above, right);
                return merge(below, above);
            } else {
                return Collections.emptyList();
            }
        }

        protected StateList multiply(StateList left, StateList right) {
            List<State> states = multiply(left.states, right.states);
            return new StateList(states, left.minIndex, right.maxIndex);
        }

        protected List<Integer> fromStateList(StateList stateList) {
            final List<State> list = stateList.states;
            return new AbstractList<Integer>() {
                @Override
                public Integer get(int index) {
                    return list.get(index).sumw;
                }

                @Override
                public int size() {
                    return list.size();
                }
            };
        }

        protected void processLowerAndUpperBounds(List<StateList> stateLists) {
            int m = stateLists.size();

            /** Section 1. Calculation [partial also] sums of weights and values */
            int[] calcW = new int[m];
            int[] calcV = new int[m];
            int[] partW = new int[m + 1];
            int[] partV = new int[m + 1];

            for (int i = 0; i < m; ++i) {
                StateList list = stateLists.get(i);
                for (int j = list.minIndex; j <= list.maxIndex; ++j) {
                    calcW[i] += items[j].weight;
                    calcV[i] += items[j].value;
                }
            }
            for (int i = 1; i <= m; ++i) {
                partW[i] = calcW[i - 1] + partW[i - 1];
                partV[i] = calcV[i - 1] + partV[i - 1];
            }

            /** Section 2.1. Precalculating phi and psi needed in upper bound estimation */
            long[][] phiNum = new long[m][];
            long[][] psiNum = new long[m][];

            int[][] upperBound = new int[m][];

            for (int i = 0; i < m; ++i) {
                StateList curr = stateLists.get(i);
                int statesCount = curr.states.size();

                //IDEA 11.0.2 ignores the middle assignment and issues a warning.
                //noinspection MismatchedReadAndWriteOfArray
                long[] phiN = phiNum[i] = new long[statesCount + 1];
                //noinspection MismatchedReadAndWriteOfArray
                long[] psiN = psiNum[i] = new long[statesCount + 1];

                upperBound[i] = new int[statesCount];

                long vNext = curr.maxIndex == n - 1 ? 0 : items[curr.maxIndex + 1].value;
                long wNext = curr.maxIndex == n - 1 ? 1 : items[curr.maxIndex + 1].weight;
                long vPrev = curr.minIndex == 0 ? 1 : items[curr.minIndex - 1].value;
                long wPrev = curr.minIndex == 0 ? 0 : items[curr.minIndex - 1].weight;

                phiN[0] = 0;
                psiN[0] = 0;

                for (int j = 0; j < statesCount; j++) {
                    State s = curr.states.get(j);
                    phiN[j + 1] = s.sumv * wNext + (capacity - s.sumw) * vNext;
                    psiN[j + 1] = s.sumv * wPrev + (capacity - s.sumw) * vPrev;
                }
            }

            /** Section 2.1. Precalculating phiMax and psiMax needed in upper bound estimation */
            for (int i = 0; i < m; ++i) {
                long[] num = phiNum[i];
                for (int j = 1; j < num.length; ++j) {
                    if (num[j] < num[j - 1]) {
                        num[j] = num[j - 1];
                    }
                }
                num = psiNum[i];
                for (int j = num.length - 2; j >= 0; --j) {
                    if (num[j] < num[j + 1]) {
                        num[j] = num[j + 1];
                    }
                }
                System.arraycopy(num, 1, num, 0, num.length - 1);
                num[num.length - 1] = 0;
            }

            /** Section 3. Refining lower bound and calculation upper bounds */
            for (int i = 0; i < m; ++i) {
                StateList curr = stateLists.get(i);
                int statesCount = curr.states.size();

                for (int j = 0; j < statesCount; j++) {
                    State s = curr.states.get(j);
                    int localRemainingGap = -1;
                    int localGatheredSum = -1;
                    int breakIndex = -1;
                    if (partW[i] + s.sumw <= capacity) {
                        int searched = capacity - s.sumw + calcW[i];
                        if (i + 1 < m) {
                            breakIndex = binSearch(partW, i + 1, m, searched);
                            localRemainingGap = searched - partW[breakIndex];
                            localGatheredSum = s.sumv + partV[breakIndex] - calcV[i];
                        }
                    } else {
                        int searched = capacity - s.sumw;
                        if (0 < i) {
                            breakIndex = binSearch(partW, 0, i, searched);
                            localRemainingGap = searched - partW[breakIndex];
                            localGatheredSum = s.sumv + partV[breakIndex];
                        }
                    }
                    if (breakIndex != -1) {
                        StateList breakList = stateLists.get(breakIndex);
                        int breakState = binSearch(
                                fromStateList(breakList), -1, breakList.states.size(), localRemainingGap
                        );
                        if (breakState >= 0) {
                            int newZ = localGatheredSum + breakList.states.get(breakState).sumv;
                            if (newZ > lowerBound) {
                                lowerBound = newZ;
                                if (i < breakIndex) {
                                    State beforeMe = getRangeSum(0, curr.minIndex);
                                    State withMe = State.getCompound(beforeMe, s);
                                    State beforeBreak = getRangeSum(curr.maxIndex + 1, breakList.minIndex);
                                    State withBreak = State.getCompound(beforeBreak, breakList.states.get(breakState));
                                    bestState = State.getCompound(withMe, withBreak);
                                } else {
                                    State beforeBreak = getRangeSum(0, breakList.minIndex);
                                    State withBreak = State.getCompound(beforeBreak, breakList.states.get(breakState));
                                    bestState = State.getCompound(withBreak, s);
                                }
                            }
                        }

                        long vNext = breakList.maxIndex == n - 1 ? 0 : items[breakList.maxIndex + 1].value;
                        long wNext = breakList.maxIndex == n - 1 ? 1 : items[breakList.maxIndex + 1].weight;
                        long vPrev = breakList.minIndex == 0 ? 1 : items[breakList.minIndex - 1].value;
                        long wPrev = breakList.minIndex == 0 ? 0 : items[breakList.minIndex - 1].weight;

                        int vWaved = i < breakIndex ? s.sumv - calcV[i] : s.sumv;
                        int wWaved = i < breakIndex ? s.sumw - calcW[i] : s.sumw;

                        int curVal = partV[breakIndex] + vWaved;
                        int curWei = partW[breakIndex] + wWaved;

                        long z1Num = curVal * wNext - curWei * vNext + phiNum[breakIndex][breakState + 1];
                        long z2Num = curVal * wPrev - curWei * vPrev + psiNum[breakIndex][breakState + 1];

                        int z1 = (int) (z1Num / wNext);
                        upperBound[i][j] = Math.max(z1, upperBound[i][j]);
                        if (wPrev != 0) {
                            int z2 = (int) (z2Num / wPrev);
                            upperBound[i][j] = Math.max(z2, upperBound[i][j]);
                        }
                    } else {
                        if (localGatheredSum > lowerBound) {
                            lowerBound = localGatheredSum;
                            bestState = State.getCompound(getRangeSum(0, curr.minIndex), s);
                        }
                        upperBound[i][j] = Integer.MAX_VALUE;
                    }
                }
            }

            /** Removing states that have too small upper bound */
            for (int i = 0; i < m; ++i) {
                StateList curr = stateLists.get(i);
                int statesCount = curr.states.size();

                List<State> newList = new ArrayList<>();

                for (int j = 0; j < statesCount; ++j) {
                    State s = curr.states.get(j);
                    if (upperBound[i][j] > lowerBound) {
                        newList.add(s);
                    }
                }

                curr.states.clear();
                curr.states.addAll(newList);
            }
        }

        public Implementation(ProblemInstance problem) {
            n = problem.getItems().size();
            items = problem.getItems().toArray(new Item[n]);
            capacity = problem.getCapacity();
            Arrays.sort(items, icmp);
            int sumcap = 0;
            for (Item i : items) {
                sumcap += i.weight;
            }
            if (sumcap <= capacity) {
                result = new KnapsackResultEx(problem.getItems(), 0, "Too easy");
                return;
            }

            itemStates = new State[n];

            List<StateList> initialLists = new ArrayList<>();
            for (int i = 0; i < n; ++i) {
                List<State> list = new ArrayList<>();
                list.add(State.EMPTY);
                if (items[i].weight <= capacity) {
                    list.add(itemStates[i] = new State(items[i]));
                }
                initialLists.add(new StateList(list, i, i));
            }
            List<StateList> stateLists = new ArrayList<>();
            int requiredSize = 1 << (31 - Integer.numberOfLeadingZeros(initialLists.size()));
            int diffBefore = (initialLists.size() - requiredSize) / 2;
            int diffAfter = (initialLists.size() - requiredSize) - diffBefore;
            for (int i = 0; i < initialLists.size(); ++i) {
                if (i < diffBefore * 2 || initialLists.size() - i <= diffAfter * 2) {
                    StateList one = initialLists.get(i);
                    StateList two = initialLists.get(++i);
                    stateLists.add(multiply(one, two));
                } else {
                    stateLists.add(initialLists.get(i));
                }
            }

            processLowerAndUpperBounds(stateLists);

            while (stateLists.size() > 2) {
                List<StateList> newStateLists = new ArrayList<>();
                for (int i = 0; i < stateLists.size(); i += 2) {
                    StateList one = stateLists.get(i);
                    StateList two = stateLists.get(i + 1);
                    StateList res = multiply(one, two);
                    maxSizeOfStates = Math.max(maxSizeOfStates, res.states.size());
                    newStateLists.add(res);
                }
                stateLists = newStateLists;

                processLowerAndUpperBounds(stateLists);
            }

            if (stateLists.size() >= 2) {
                List<State> left = stateLists.get(0).states;
                List<State> right = stateLists.get(1).states;
                for (int i = 0, j = right.size() - 1; i < left.size() && j >= 0; ) {
                    ++operationCount;
                    State ls = left.get(i);
                    State rs = right.get(j);
                    if (ls.sumw + rs.sumw <= capacity) {
                        if (bestState == null || ls.sumv + rs.sumv > bestState.sumv) {
                            bestState = State.getCompound(ls, rs);
                        }
                        ++i;
                    } else {
                        --j;
                    }
                }
            } else {
                List<State> theOnly = stateLists.get(0).states;
                for (State s : theOnly) {
                    if (s.sumw <= capacity) {
                        if (bestState == null || bestState.sumv < s.sumv) {
                            bestState = s;
                        }
                    }
                }
            }

            List<Item> res = new ArrayList<>();
            bestState.dumpItems(res);
            result = new KnapsackResultEx(res, operationCount, "maxStates = " + maxSizeOfStates + " states = " + stateCount);
        }

        public KnapsackResultEx getResult() {
            return result;
        }
    }
}
