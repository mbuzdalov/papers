package knapsack.solvers;

import java.util.*;
import knapsack.*;

/**
 * This is a partially implemented Pisinger's algorithm HardKnap.
 *
 * @author Maxim Buzdalov
 */
public final class HardKnapPart implements KnapsackSolver {
    private static final HardKnapPart instance = new HardKnapPart();
    private HardKnapPart() {}

    public static HardKnapPart getInstance() {
        return instance;
    }

    @Override
    public KnapsackResultEx solve(ProblemInstance problem) {
        return new Implementation(problem).getResult();
    }

    @Override
    public String getName() {
        return "HardKnapPart";
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

        protected long stateCount;
        protected long operationCount;

        protected int maxSizeOfStates;

        protected static int detSgn(int a, int b, int c, int d) {
            return Long.signum((long) (a) * d - (long) (b) * c);
        }

        protected static final Comparator<Item> icmp = new Comparator<Item>() {
            @Override
            public int compare(Item lhs, Item rhs) {
                return detSgn(lhs.weight, lhs.value, rhs.weight, rhs.value);
            }
        };

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

            List<StateList> initialLists = new ArrayList<>();
            for (int i = 0; i < n; ++i) {
                List<State> list = new ArrayList<>();
                list.add(State.EMPTY);
                if (items[i].weight <= capacity) {
                    list.add(new State(items[i]));
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
            }

            State bestState = null;
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

            if (bestState == null) {
                throw new AssertionError("Algorithm is wrong");
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
