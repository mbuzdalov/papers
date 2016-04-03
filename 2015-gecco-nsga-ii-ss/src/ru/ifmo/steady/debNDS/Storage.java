package ru.ifmo.steady.debNDS;

import java.util.*;

import ru.ifmo.steady.*;
import ru.ifmo.steady.util.FastRandom;

/**
 * An implementation of the Deb's fast non-dominated sorting
 * with crowding distance support.
 */
public class Storage extends SolutionStorage {
    public void add(Solution solution) {
        List<Solution> cr = collectRemove();
        cr.add(solution);
        sort(cr);
    }

    public void addAll(Solution... solutions) {
        List<Solution> cr = collectRemove();
        cr.addAll(Arrays.asList(solutions));
        sort(cr);
    }

    public int getLayerCount() {
        return layers.size();
    }

    public Iterator<Solution> getLayer(int index) {
        if (index < 0 || index >= getLayerCount()) {
            throw new IllegalArgumentException("No such layer: " + index);
        }
        return layers.get(index).iterator();
    }

    public String getName() {
        return "debNDS";
    }

    public QueryResult getRandom() {
        int sz = size();
        if (sz == 0) {
            throw new IllegalStateException("empty data structure");
        }
        return getKth(FastRandom.geneticThreadLocal().nextInt(sz));
    }

    public QueryResult getKth(int index) {
        int sz = size();
        if (index < 0 || index >= sz) {
            throw new IllegalArgumentException("index = " + index + " size = " + sz);
        }
        int layer = 0;
        while (layers.get(layer).size() <= index) {
            index -= layers.get(layer).size();
            ++layer;
        }
        List<Solution> curr = layers.get(layer);
        int cs = curr.size();
        Solution s = curr.get(index);
        double crowding;
        if (index == 0 || index + 1 == cs) {
            crowding = Double.POSITIVE_INFINITY;
        } else {
            crowding = Solution.crowdingDistance(curr.get(index - 1), curr.get(index + 1), curr.get(0), curr.get(cs - 1), counter);
        }
        return new QueryResult(s, crowding, layer);
    }

    public int size() {
        return size;
    }

    public Solution removeWorst() {
        return removeWorstImpl(1);
    }

    public void removeWorst(int count) {
        removeWorstImpl(count);
    }

    public void clear() {
        layers.clear();
        size = 0;
    }

    /* Implementation */

    private final List<List<Solution>> layers = new ArrayList<>();
    private int size = 0;

    private List<Solution> collectRemove() {
        if (size == 0) {
            return new ArrayList<>();
        } else {
            List<Solution> rv = layers.remove(layers.size() - 1);
            while (!layers.isEmpty()) {
                rv.addAll(layers.remove(layers.size() - 1));
            }
            return rv;
        }
    }

    private void sort(List<Solution> solutions) {
        int sz = solutions.size();
        this.size = sz;
        boolean[][] dom = new boolean[sz][sz];
        int[] incoming = new int[sz];
        for (int i = 0; i < sz; ++i) {
            Solution si = solutions.get(i);
            for (int j = i + 1; j < sz; ++j) {
                Solution sj = solutions.get(j);
                int cmpx = si.compareX(sj, counter);
                int cmpy = si.compareY(sj, counter);
                if (cmpx >= 0 && cmpy > 0 || cmpx > 0 && cmpy >= 0) {
                    dom[j][i] = true;
                    ++incoming[i];
                } else if (cmpx <= 0 && cmpy < 0 || cmpx < 0 && cmpy <= 0) {
                    dom[i][j] = true;
                    ++incoming[j];
                }
            }
        }
        int[] curr = new int[sz];
        int[] next = new int[sz];
        int currSize = 0;
        for (int i = 0; i < sz; ++i) {
            if (incoming[i] == 0) {
                curr[currSize++] = i;
            }
        }
        while (currSize > 0) {
            int nextSize = 0;
            Solution[] layer = new Solution[currSize];
            for (int i = 0; i < currSize; ++i) {
                int ci = curr[i];
                layer[i] = solutions.get(ci);
                for (int j = 0; j < sz; ++j) {
                    if (dom[ci][j]) {
                        if (--incoming[j] == 0) {
                            next[nextSize++] = j;
                        }
                    }
                }
            }
            Arrays.sort(layer, (l, r) -> l.compareX(r, counter));
            List<Solution> ll = new ArrayList<>(currSize);
            for (int i = 0; i < currSize; ++i) {
                ll.add(layer[i]);
            }
            layers.add(ll);
            currSize = nextSize;
            int[] tmp = curr;
            curr = next;
            next = tmp;
        }
    }

    private Solution removeWorstImpl(int count) {
        if (size < count) {
            throw new AssertionError("not enough elements");
        }
        size -= count;
        List<Solution> curr = layers.get(layers.size() - 1);
        while (curr.size() < count) {
            layers.remove(layers.size() - 1);
            count -= curr.size();
            curr = layers.get(layers.size() - 1);
        }
        Solution lastRemoved = null;
        Random r = FastRandom.geneticThreadLocal();
        List<Integer> equal = new ArrayList<>();
        while (count > 0) {
            double worst = Double.POSITIVE_INFINITY;
            equal.clear();
            int cs = curr.size();
            for (int i = 0; i < cs; ++i) {
                double crowding = i == 0 || i + 1 == cs
                    ? Double.POSITIVE_INFINITY
                    : Solution.crowdingDistance(
                        curr.get(i - 1),
                        curr.get(i + 1),
                        curr.get(0),
                        curr.get(cs - 1),
                        counter
                    );
                if (worst > crowding) {
                    worst = crowding;
                    equal.clear();
                }
                if (worst == crowding) {
                    equal.add(i);
                }
            }
            int toDel = cs == 1 ? equal.get(0) : equal.get(r.nextInt(equal.size()));
            lastRemoved = curr.remove(toDel);
            --count;
        }
        if (curr.isEmpty()) {
            layers.remove(layers.size() - 1);
        }
        return lastRemoved;
    }

    public void removeWorstDebCompatible(int count) {
        if (size < count) {
            throw new IllegalStateException("not enough elements to delete");
        }
        size -= count;
        while (layers.get(layers.size() - 1).size() <= count) {
            count -= layers.get(layers.size() - 1).size();
            layers.remove(layers.size() - 1);
        }
        if (count > 0) {
            List<Solution> lastLayer = layers.get(layers.size() - 1);
            int lls = lastLayer.size();
            double[] crowding = new double[lls];
            Integer[] indices = new Integer[lls];
            Solution min = lastLayer.get(0);
            Solution max = lastLayer.get(lls - 1);
            for (int i = 0; i < lls; ++i) {
                indices[i] = i;
                crowding[i] = Solution.crowdingDistance(
                    i == 0 ? null : lastLayer.get(i - 1),
                    i + 1 == lls ? null : lastLayer.get(i + 1),
                    min, max, counter
                );
            }
            Arrays.sort(indices, (l, r) -> Double.compare(crowding[r], crowding[l]));
            int remain = lls - count;
            Arrays.sort(indices, 0, remain);
            List<Solution> newContents = new ArrayList<>(remain);
            for (int i = 0; i < remain; ++i) {
                newContents.add(lastLayer.get(indices[i]));
            }
            layers.set(layers.size() - 1, newContents);
        }
    }
}
