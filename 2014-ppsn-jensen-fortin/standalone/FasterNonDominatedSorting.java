/*
 * Copyright 2015 Maxim Buzdalov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.*;

/**
 * A stand-alone implementation of the "faster" non-dominated sorting.
 *
 * The idea is outlined in the paper:
 * ==================================================================================================
 * Buzdalov M., Shalyto A. A Provably Asymptotically Fast Version of the Generalized Jensen Algorithm
 * for Non-dominated Sorting // Parallel Problem Solving from Nature XIII. - 2015. - P. 528-537.
 * - (Lecture Notes on Computer Science ; 8672)
 * ==================================================================================================
 *
 * Please cite the paper referenced above when you use this code in your research.
 *
 * @author Maxim Buzdalov
 */
public final class FasterNonDominatedSorting {
    /**
     * Performs non-dominated sorting of the given data.
     * In this method, Pareto dominance considers minimization: a point A dominates a point B
     * if and only if every coordinate of A is not greater than the corresponding coordinate of B
     * and there exists a coordinate in A which is strictly less than the corresponding coordinate of B.
     *
     * If the argument is <code>null</code> or contains a <code>null</code> as an element,
     * a <code>NullPointerException</code> is thrown. If the argument contains points of non-equal dimensions,
     * an <code>IllegalArgumentException</code> is thrown.
     *
     * The method doesn't change the input data and returns an array of layer indices:
     * <code>i</code>th element of the output is the layer index of the <code>i</code>th point from the input.
     * The layer 0 corresponds to the non-dominated layer of solutions, the layer 1 corresponds to solutions which
     * are dominated by solutions from layer 0 only, and so far.
     *
     * @param data the points for sorting.
     * @throws java.lang.NullPointerException if the argument is <code>null</code> or contains <code>null</code>.
     * @throws java.lang.IllegalArgumentException if the argument contains points of non-equal dimensions.
     * @return the array of layer indices.
     */
    public static int[] sort(double[][] data) {
        // Starting from this line, this is a mere copy-paste of sort(int[][]) prefix
        Objects.requireNonNull(data);
        final int n = data.length;
        if (n == 0) {
            return new int[0];
        }
        for (double[] point : data) {
            Objects.requireNonNull(point);
        }
        final int dim = data[0].length;
        for (int i = 1; i < n; ++i) {
            if (dim != data[i].length) {
                throw new IllegalArgumentException(
                        "Input has points of different dimensions: " + dim + " and " + data[i].length
                );
            }
        }
        if (dim == 0) {
            return new int[n];
        }
        if (dim == 1) {
            return sort1D(data);
        }
        // The copy-paste was until now.
        // The main part is implemented by converting double[][] to equivalent int[][] in O((n log n) * dim)
        IntDoublePair[] indices = new IntDoublePair[n];
        for (int i = 0; i < n; ++i) {
            indices[i] = new IntDoublePair(0, 0);
        }
        int[][] equiv = new int[n][dim];
        for (int d = 0; d < dim; ++d) {
            for (int i = 0; i < n; ++i) {
                indices[i].index = i;
                indices[i].value = data[i][d];
            }
            Arrays.sort(indices);
            for (int i = 0, q = 0; i < n; ++i) {
                equiv[indices[i].index][d] = q;
                if (i + 1 < n && indices[i].value != indices[i + 1].value) {
                    ++q;
                }
            }
        }
        return sortChecked(equiv);
    }

    /**
     * Performs non-dominated sorting of the given data.
     * In this method, Pareto dominance considers minimization: a point A dominates a point B
     * if and only if every coordinate of A is not greater than the corresponding coordinate of B
     * and there exists a coordinate in A which is strictly less than the corresponding coordinate of B.
     *
     * If the argument is <code>null</code> or contains a <code>null</code> as an element,
     * a <code>NullPointerException</code> is thrown. If the argument contains points of non-equal dimensions,
     * an <code>IllegalArgumentException</code> is thrown.
     *
     * The method doesn't change the input data and returns an array of layer indices:
     * <code>i</code>th element of the output is the layer index of the <code>i</code>th point from the input.
     * The layer 0 corresponds to the non-dominated layer of solutions, the layer 1 corresponds to solutions which
     * are dominated by solutions from layer 0 only, and so far.
     *
     * @param data the points for sorting.
     * @throws java.lang.NullPointerException if the argument is <code>null</code> or contains <code>null</code>.
     * @throws java.lang.IllegalArgumentException if the argument contains points of non-equal dimensions.
     * @return the array of layer indices.
     */
    public static int[] sort(int[][] data) {
        Objects.requireNonNull(data);
        final int n = data.length;
        if (n == 0) {
            return new int[0];
        }
        for (int[] point : data) {
            Objects.requireNonNull(point);
        }
        final int dim = data[0].length;
        for (int i = 1; i < n; ++i) {
            if (dim != data[i].length) {
                throw new IllegalArgumentException(
                        "Input has points of different dimensions: " + dim + " and " + data[i].length
                );
            }
        }
        if (dim == 0) {
            return new int[n];
        }
        if (dim == 1) {
            return sort1D(data);
        }

        return sortChecked(data);
    }

    private static int[] sortChecked(int[][] data) {
        final int n = data.length;
        final int dim = data[0].length;

        ValueIndexLayer[] vil = new ValueIndexLayer[n];
        for (int i = 0; i < n; ++i) {
            vil[i] = new ValueIndexLayer(data[i], i);
        }

        CoordinateComparator[] comparators = new CoordinateComparator[dim];
        for (int i = 0; i < dim; ++i) {
            comparators[i] = new CoordinateComparator(i);
        }

        ValueIndexLayer[] vilClone = vil.clone();
        int equalityComponents = new LexEqSorter(vilClone, comparators).lexSort(0, 0, n, 0);
        // Safe to reuse vilClone now
        Arrays.fill(vilClone, 0, equalityComponents, null);
        for (int i = 0; i < n; ++i) {
            int cmp = vil[i].equalityComponent;
            if (vilClone[cmp] == null) {
                vilClone[cmp] = vil[i];
            }
        }

        new NonDomSorter(vilClone).sort(0, equalityComponents, dim - 1);

        int[] rv = new int[n];
        for (int i = 0; i < n; ++i) {
            rv[i] = vilClone[vil[i].equalityComponent].layer;
        }
        return rv;
    }

    private static final class NonDomSorter {
        final ValueIndexLayer[] data;
        final Random random = new Random();
        final ValueIndexLayer[] swap;

        private NonDomSorter(ValueIndexLayer[] data) {
            this.data = data;
            this.swap = new ValueIndexLayer[data.length];
        }

        void ifFirstDominatesUpdateSecond(int i1, int i2, int dimension) {
            ValueIndexLayer first = data[i1], second = data[i2];
            for (int i = 0; i <= dimension; ++i) {
                if (first.value[i] > second.value[i]) {
                    return;
                }
            }
            second.layer = Math.max(second.layer, first.layer + 1);
        }

        void cleanup(ValueIndexLayer curr, TreeSet<ValueIndexLayer> set) {
            Iterator<ValueIndexLayer> greaterIterator = set.tailSet(curr, true).iterator();
            while (greaterIterator.hasNext()) {
                if (greaterIterator.next().layer <= curr.layer) {
                    greaterIterator.remove();
                } else {
                    break;
                }
            }
        }

        final TreeSet<ValueIndexLayer> set = new TreeSet<>(YXComparator);

        void sort2D(int from, int until) {
            for (int i = from; i < until; ++i) {
                ValueIndexLayer curr = data[i];
                Iterator<ValueIndexLayer> lessIterator = set.headSet(curr, true).descendingIterator();
                if (lessIterator.hasNext()) {
                    curr.layer = Math.max(curr.layer, 1 + lessIterator.next().layer);
                }
                cleanup(curr, set);
                set.add(curr);
            }
            set.clear();
        }

        void sortHighByLow2D(int lFrom, int lUntil, int hFrom, int hUntil) {
            int li = lFrom;
            for (int hi = hFrom; hi < hUntil; ++hi) {
                while (li < lUntil && data[li].equalityComponent < data[hi].equalityComponent) {
                    ValueIndexLayer curr = data[li++];
                    Iterator<ValueIndexLayer> lessIterator = set.headSet(curr, true).descendingIterator();
                    if (!lessIterator.hasNext() || lessIterator.next().layer < curr.layer) {
                        cleanup(curr, set);
                        set.add(curr);
                    }
                }
                ValueIndexLayer curr = data[hi];
                Iterator<ValueIndexLayer> lessIterator = set.headSet(curr, true).descendingIterator();
                if (lessIterator.hasNext()) {
                    curr.layer = Math.max(curr.layer, 1 + lessIterator.next().layer);
                }
            }
            set.clear();
        }

        int medianInSwap(int from, int until, int dimension) {
            int to = until - 1;
            int med = (from + until) >>> 1;
            while (from <= to) {
                int pivot = swap[from + random.nextInt(to - from + 1)].value[dimension];
                int ff = from, tt = to;
                while (ff <= tt) {
                    while (swap[ff].value[dimension] < pivot) ++ff;
                    while (swap[tt].value[dimension] > pivot) --tt;
                    if (ff <= tt) {
                        ValueIndexLayer tmp = swap[ff];
                        swap[ff] = swap[tt];
                        swap[tt] = tmp;
                        ++ff;
                        --tt;
                    }
                }
                if (med <= tt) {
                    to = tt;
                } else if (med >= ff) {
                    from = ff;
                } else {
                    return swap[med].value[dimension];
                }
            }
            return swap[from].value[dimension];
        }

        int lessThan, equalTo, greaterThan;

        void split3(int from, int until, int dimension, int median) {
            lessThan = equalTo = greaterThan = 0;
            for (int i = from; i < until; ++i) {
                int cmp = Integer.compare(data[i].value[dimension], median);
                if (cmp < 0) {
                    ++lessThan;
                } else if (cmp == 0) {
                    ++equalTo;
                } else {
                    ++greaterThan;
                }
            }
            int lessThanPtr = 0, equalToPtr = lessThan, greaterThanPtr = lessThan + equalTo;
            for (int i = from; i < until; ++i) {
                int cmp = Integer.compare(data[i].value[dimension], median);
                if (cmp < 0) {
                    swap[lessThanPtr++] = data[i];
                } else if (cmp == 0) {
                    swap[equalToPtr++] = data[i];
                } else {
                    swap[greaterThanPtr++] = data[i];
                }
            }
            System.arraycopy(swap, 0, data, from, until - from);
        }

        void merge(int from, int mid, int until) {
            int p0 = from, p1 = mid;
            for (int i = from; i < until; ++i) {
                if (p0 == mid || p1 < until && data[p1].equalityComponent < data[p0].equalityComponent) {
                    swap[i] = data[p1++];
                } else {
                    swap[i] = data[p0++];
                }
            }
            System.arraycopy(swap, from, data, from, until - from);
        }

        void sortHighByLow(int lFrom, int lUntil, int hFrom, int hUntil, int dimension) {
            int lSize = lUntil - lFrom, hSize = hUntil - hFrom;
            if (lSize == 0 || hSize == 0) {
                return;
            }
            if (lSize == 1) {
                for (int hi = hFrom; hi < hUntil; ++hi) {
                    ifFirstDominatesUpdateSecond(lFrom, hi, dimension);
                }
            } else if (hSize == 1) {
                for (int li = lFrom; li < lUntil; ++li) {
                    ifFirstDominatesUpdateSecond(li, hFrom, dimension);
                }
            } else if (dimension == 1) {
                sortHighByLow2D(lFrom, lUntil, hFrom, hUntil);
            } else {
                int lMin = data[lFrom].value[dimension], lMax = lMin;
                int hMin = data[hFrom].value[dimension], hMax = hMin;

                for (int li = lFrom + 1; li < lUntil; ++li) {
                    int v = data[li].value[dimension];
                    lMin = Math.min(lMin, v);
                    lMax = Math.max(lMax, v);
                }
                for (int hi = hFrom + 1; hi < hUntil; ++hi) {
                    int v = data[hi].value[dimension];
                    hMin = Math.min(hMin, v);
                    hMax = Math.max(hMax, v);
                }
                if (lMax < hMin) {
                    sortHighByLow(lFrom, lUntil, hFrom, hUntil, dimension - 1);
                } else {
                    System.arraycopy(data, lFrom, swap, 0, lSize);
                    System.arraycopy(data, hFrom, swap, lSize, hSize);
                    int median = medianInSwap(0, lSize + hSize, dimension);

                    split3(lFrom, lUntil, dimension, median);
                    int lMidL = lFrom + lessThan, lMidR = lMidL + equalTo;

                    split3(hFrom, hUntil, dimension, median);
                    int hMidL = hFrom + lessThan, hMidR = hMidL + equalTo;

                    sortHighByLow(lFrom, lMidL, hFrom, hMidL, dimension);
                    sortHighByLow(lFrom, lMidL, hMidL, hMidR, dimension - 1);
                    sortHighByLow(lMidL, lMidR, hMidL, hMidR, dimension - 1);
                    merge(lFrom, lMidL, lMidR);
                    merge(hFrom, hMidL, hMidR);
                    sortHighByLow(lFrom, lMidR, hMidR, hUntil, dimension - 1);
                    sortHighByLow(lMidR, lUntil, hMidR, hUntil, dimension);
                    merge(lFrom, lMidR, lUntil);
                    merge(hFrom, hMidR, hUntil);
                }
            }
        }

        void sort(int from, int until, int dimension) {
            int size = until - from;
            if (size == 2) {
                ifFirstDominatesUpdateSecond(from, from + 1, dimension);
            } else if (size > 2) {
                if (dimension == 1) {
                    sort2D(from, until);
                } else {
                    boolean allEqual = true;
                    for (int i = from + 1; allEqual && i < until; ++i) {
                        allEqual = data[from].value[dimension] == data[i].value[dimension];
                    }
                    if (allEqual) {
                        sort(from, until, dimension - 1);
                    } else {
                        System.arraycopy(data, from, swap, from, size);
                        int median = medianInSwap(from, until, dimension);

                        split3(from, until, dimension, median);
                        int midL = from + lessThan, midH = midL + equalTo;

                        sort(from, midL, dimension);
                        sortHighByLow(from, midL, midL, midH, dimension - 1);
                        sort(midL, midH, dimension - 1);
                        merge(from, midL, midH);
                        sortHighByLow(from, midH, midH, until, dimension - 1);
                        sort(midH, until, dimension);
                        merge(from, midH, until);
                    }
                }
            }
        }
    }

    private static final class LexEqSorter {
        final ValueIndexLayer[] data;
        final CoordinateComparator[] comparators;

        LexEqSorter(ValueIndexLayer[] data, CoordinateComparator[] comparators) {
            this.data = data;
            this.comparators = comparators;
        }

        final int lexSort(int coordinate, int left, int right, int minEqComp) {
            if (coordinate < comparators.length) {
                Arrays.sort(data, left, right, comparators[coordinate]);
                int nc = coordinate + 1;
                int last = left;
                for (int i = left + 1; i < right; ++i) {
                    if (data[last].value[coordinate] != data[i].value[coordinate]) {
                        if (last + 1 != i) {
                            minEqComp = lexSort(nc, last, i, minEqComp);
                        } else {
                            data[last].equalityComponent = minEqComp++;
                        }
                        last = i;
                    }
                }
                if (last + 1 != right) {
                    minEqComp = lexSort(nc, last, right, minEqComp);
                } else {
                    data[last].equalityComponent = minEqComp++;
                }
            } else {
                for (int i = left; i < right; ++i) {
                    data[i].equalityComponent = minEqComp;
                }
                ++minEqComp;
            }
            return minEqComp;
        }

    }

    private static int[] sort1D(int[][] data) {
        final int n = data.length;
        IntPair[] toBeSorted = new IntPair[n];
        for (int i = 0; i < n; ++i) {
            toBeSorted[i] = new IntPair(i, data[i][0]);
        }
        Arrays.sort(toBeSorted);
        int[] rv = new int[n];
        for (int i = 0, layer = 0; i < n; ++i) {
            IntPair curr = toBeSorted[i];
            if (i > 0 && toBeSorted[i - 1].value != curr.value) {
                ++layer;
            }
            rv[curr.index] = layer;
        }
        return rv;
    }

    private static final class IntPair implements Comparable<IntPair> {
        final int index, value;

        private IntPair(int index, int value) {
            this.index = index;
            this.value = value;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public int compareTo(IntPair that) {
            return Integer.compare(value, that.value);
        }
    }

    private static int[] sort1D(double[][] data) {
        final int n = data.length;
        IntDoublePair[] toBeSorted = new IntDoublePair[n];
        for (int i = 0; i < n; ++i) {
            toBeSorted[i] = new IntDoublePair(i, data[i][0]);
        }
        Arrays.sort(toBeSorted);
        int[] rv = new int[n];
        for (int i = 0, layer = 0; i < n; ++i) {
            IntDoublePair curr = toBeSorted[i];
            if (i > 0 && toBeSorted[i - 1].value != curr.value) {
                ++layer;
            }
            rv[curr.index] = layer;
        }
        return rv;
    }

    private static final class IntDoublePair implements Comparable<IntDoublePair> {
        int index;
        double value;

        private IntDoublePair(int index, double value) {
            this.index = index;
            this.value = value;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public int compareTo(IntDoublePair that) {
            return Double.compare(value, that.value);
        }
    }

    private static final class ValueIndexLayer {
        final int[] value;
        final int index;
        int layer;
        int equalityComponent;

        private ValueIndexLayer(int[] value, int index) {
            this.value = value;
            this.index = index;
        }
    }

    @SuppressWarnings("Convert2Lambda") // in order to have Java-8-enabled IDEs stop complaining
    private static final Comparator<ValueIndexLayer> YXComparator = new Comparator<ValueIndexLayer>() {
        @Override
        public int compare(ValueIndexLayer left, ValueIndexLayer right) {
            int cmpY = Integer.compare(left.value[1], right.value[1]);
            return cmpY != 0 ? cmpY : Integer.compare(left.value[0], right.value[0]);
        }
    };

    private static final class CoordinateComparator implements Comparator<ValueIndexLayer> {
        final int coordinate;

        private CoordinateComparator(int coordinate) {
            this.coordinate = coordinate;
        }

        @Override
        public int compare(ValueIndexLayer left, ValueIndexLayer right) {
            return Integer.compare(left.value[coordinate], right.value[coordinate]);
        }
    }
}
