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
 * For those who cite, there is a BibTeX entry:
 * <code>
 * @incollection{
 *     author       = {Maxim Buzdalov and Anatoly Shalyto},
 *     title        = {A Provably Asymptotically Fast Version of the Generalized Jensen Algorithm
 *                     for Non-dominated Sorting},
 *     booktitle    = {Parallel Problem Solving from Nature XIII},
 *     series       = {Lecture Notes on Computer Science},
 *     number       = {8672},
 *     year         = {2005},
 *     pages        = {528-537},
 *     langid       = {english}
 * }
 * </code>
 *
 * @author Maxim Buzdalov
 */
public final class FasterNonDominatedSorting {
    /**
     * A factory method which returns a sorter
     * adapted for the given size (the number of points)
     * and dimension (the number of coordinates in each point).
     *
     * The method does not cache anything, do it on the caller's side.
     *
     * @param size the problem's size (the number of points).
     * @param dim the problem's dimension (the number of coordinates in each point).
     * @return the sorter adapted for the given size and dimension.
     */
    public static Sorter getSorter(int size, int dim) {
        if (dim < 0 || size < 0) {
            throw new IllegalArgumentException("Size or dimension is negative");
        }
        if (size == 0) {
            return new SorterEmpty(dim);
        }
        switch (dim) {
            case 0: return new Sorter0D(size);
            case 1: return new Sorter1D(size);
            case 2: return new Sorter2D(size);
            default: return new SorterXD(size, dim);
        }
    }

    /**
     * A base class for all sorters.
     * A sorter supports two getter methods (for size and dimension)
     * and the method for actual sorting.
     */
    public static abstract class Sorter {
        protected final int size;
        protected final int dim;
        protected Sorter(int size, int dim) {
            this.size = size;
            this.dim = dim;
        }
        /**
         * Returns the size of the problem this sorter can handle.
         * @return the size of the problem.
         */
        public int size() {
            return size;
        }
        /**
         * Returns the dimension of the problem this sorter can handle.
         * @return the dimension of the problem.
         */
        public int dimension() {
            return dim;
        }
        /**
         * Performs the non-dominated sorting of the given input array
         * and stores the results in the given output array.
         *
         * The input array should have the dimensions of exactly {#size()} * {#dimension()},
         * otherwise an IllegalArgumentException is thrown.
         *
         * The output array should have the dimension of exactly {#size()},
         * otherwise an IllegalArgumentException is thrown.
         *
         * The method does not change the {#input} array and fills the {#output} array by layer indices:
         * <code>i</code>th element of {#output} will be the layer index of the <code>i</code>th point from {#input}.
         * The layer 0 corresponds to the non-dominated layer of solutions, the layer 1 corresponds to solutions which
         * are dominated by solutions from layer 0 only, and so far.
         *
         * @param the input array which is to be sorted.
         * @param the output array which is filled with the front indices of the corresponding input elements.
         */
        public void sort(double[][] input, int[] output) {
            if (input.length != size) {
                throw new IllegalArgumentException(
                    "Input size (" + input.length + ") does not match the sorter's size (" + size + ")"
                );
            }
            if (output.length != size) {
                throw new IllegalArgumentException(
                    "Output size (" + output.length + ") does not match the sorter's size (" + size + ")"
                );
            }
            for (int i = 0; i < size; ++i) {
                if (input[i].length != dim) {
                    throw new IllegalArgumentException(
                        "Input dimension at index " + i + " (" + input[i].length +
                                ") does not match the sorter's dimension (" + dim + ")"
                    );
                }
            }
            sortImpl(input, output);
        }
        protected abstract void sortImpl(double[][] input, int[] output);
    }

    // Empty sorter: to rule out the case of empty input array.
    private static final class SorterEmpty extends Sorter {
        public SorterEmpty(int dim) {
            super(0, dim);
        }
        protected void sortImpl(double[][] input, int[] output) {
            // do nothing
        }
    }

    // 0D sorter: zero out the answer.
    private static final class Sorter0D extends Sorter {
        public Sorter0D(int size) {
            super(size, 0);
        }
        protected void sortImpl(double[][] input, int[] output) {
            Arrays.fill(output, 0);
        }
    }

    // 1D sorter: do the sorting and uniquification.
    private static final class Sorter1D extends Sorter {
        private final int[] indices;
        private final MergeSorter sorter;

        public Sorter1D(int size) {
            super(size, 1);
            indices = new int[size];
            sorter = new MergeSorter(size);
        }

        protected void sortImpl(double[][] input, int[] output) {
            for (int i = 0; i < size; ++i) {
                indices[i] = i;
            }
            sorter.sort(indices, 0, size, input, 0);
            output[indices[0]] = 0;
            for (int i = 1; i < size; ++i) {
                int prev = indices[i - 1], curr = indices[i];
                if (input[prev][0] == input[curr][0]) {
                    output[curr] = output[prev];
                } else {
                    output[curr] = output[prev] + 1;
                }
            }
        }
    }

    // 2D sorter: binary search on layer tails; should be faster than the general one.
    private static final class Sorter2D extends Sorter {
        private final int[] indices;
        private final int[] eqComp;
        private final int[] frontTails;
        private final MergeSorter sorter;

        public Sorter2D(int size) {
            super(size, 2);
            indices = new int[size];
            eqComp = new int[size];
            frontTails = new int[size];
            sorter = new MergeSorter(size);
        }

        protected void sortImpl(double[][] input, int[] output) {
            for (int i = 0; i < size; ++i) {
                indices[i] = i;
            }
            sorter.lexSort(indices, 0, size, input, eqComp);
            output[indices[0]] = 0;
            frontTails[0] = indices[0];
            int nLayers = 1;
            for (int i = 1; i < size; ++i) {
                int curr = indices[i];
                double curr1 = input[curr][1];
                if (eqComp[curr] == eqComp[indices[i - 1]]) {
                    output[curr] = output[indices[i - 1]];
                } else if (input[frontTails[0]][1] > curr1) {
                    output[curr] = 0;
                    frontTails[0] = curr;
                } else {
                    int left = 0, right = nLayers;
                    // left definitely dominates, right definitely not
                    while (right - left > 1) {
                        int mid = (left + right) >>> 1;
                        if (input[frontTails[mid]][1] > curr1) {
                            right = mid;
                        } else {
                            left = mid;
                        }
                    }
                    if (right == nLayers) {
                        ++nLayers;
                    }
                    output[curr] = right;
                    frontTails[right] = curr;
                }
            }
        }
    }

    // XD sorter: the general case.
    private static final class SorterXD extends Sorter {
        private final int[] indices;
        private final int[] swap;
        private final int[] eqComp;
        private final MergeSorter sorter;

        private double[][] input;
        private int[] output;

        private int[]    fenwickData;
        private double[] fenwickPivots;
        private int      fenwickSize;

        private final Random random = new Random();

        private void fenwickInit(int from, int until) {
            for (int i = 0, j = from; j < until; ++i, ++j) {
                fenwickPivots[i] = input[indices[j]][1];
            }
            Arrays.sort(fenwickPivots, 0, until - from);
            int last = 0;
            for (int i = 1; i < until - from; ++i) {
                if (fenwickPivots[i] != fenwickPivots[last]) {
                    fenwickPivots[++last] = fenwickPivots[i];
                }
            }
            fenwickSize = last + 1;
            Arrays.fill(fenwickData, 0, fenwickSize, -1);
        }

        private int fenwickIndex(double key) {
            int left = -1, right = fenwickSize;
            while (right - left > 1) {
                int mid = (left + right) >>> 1;
                if (fenwickPivots[mid] <= key) {
                    left = mid;
                } else {
                    right = mid;
                }
            }
            return left;
        }

        private void fenwickSet(double key, int value) {
            int fwi = fenwickIndex(key);
            while (fwi < fenwickSize) {
                fenwickData[fwi] = Math.max(fenwickData[fwi], value);
                fwi |= fwi + 1;
            }
        }

        private int fenwickQuery(double key) {
            int fwi = fenwickIndex(key);
            if (fwi >= fenwickSize || fwi < 0) {
                return -1;
            } else {
                int rv = -1;
                while (fwi >= 0) {
                    rv = Math.max(rv, fenwickData[fwi]);
                    fwi = (fwi & (fwi + 1)) - 1;
                }
                return rv;
            }
        }

        public SorterXD(int size, int dim) {
            super(size, dim);
            indices = new int[size];
            eqComp = new int[size];
            swap = new int[size];
            fenwickData = new int[size];
            fenwickPivots = new double[size];
            sorter = new MergeSorter(size);
        }

        protected void sortImpl(double[][] input, int[] output) {
            for (int i = 0; i < size; ++i) {
                indices[i] = i;
            }
            Arrays.fill(output, 0);
            sorter.lexSort(indices, 0, size, input, eqComp);
            this.input = input;
            this.output = output;
            sort(0, size, dim - 1);
            this.input = null;
            this.output = null;
        }

        private void updateFront(int target, int source) {
            if (eqComp[target] == eqComp[source]) {
                output[target] = output[source];
            } else {
                output[target] = Math.max(output[target], output[source] + 1);
            }
        }

        private void sort2D(int from, int until) {
            fenwickInit(from, until);
            int curr = from;
            while (curr < until) {
                int currI = indices[curr];
                int next = curr + 1;
                while (next < until && eqComp[indices[next]] == eqComp[currI]) {
                    ++next;
                }
                int result = Math.max(output[currI], fenwickQuery(input[currI][1]) + 1);
                for (int i = curr; i < next; ++i) {
                    output[indices[i]] = result;
                }
                fenwickSet(input[currI][1], result);
                curr = next;
            }
        }

        private void sortHighByLow2D(int lFrom, int lUntil, int hFrom, int hUntil) {
            fenwickInit(lFrom, lUntil);
            int li = lFrom;
            for (int hi = hFrom; hi < hUntil; ++hi) {
                int currH = indices[hi];
                int eCurrH = eqComp[currH];
                while (li < lUntil && eqComp[indices[li]] < eCurrH) {
                    int currL = indices[li++];
                    fenwickSet(input[currL][1], output[currL]);
                }
                output[currH] = Math.max(output[currH], fenwickQuery(input[currH][1]) + 1);
            }
        }

        private double medianInSwap(int from, int until, int dimension) {
            int to = until - 1;
            int med = (from + until) >>> 1;
            while (from <= to) {
                double pivot = input[swap[from + random.nextInt(to - from + 1)]][dimension];
                int ff = from, tt = to;
                while (ff <= tt) {
                    while (input[swap[ff]][dimension] < pivot) ++ff;
                    while (input[swap[tt]][dimension] > pivot) --tt;
                    if (ff <= tt) {
                        int tmp = swap[ff];
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
                    return input[swap[med]][dimension];
                }
            }
            return input[swap[from]][dimension];
        }

        private int lessThan, equalTo, greaterThan;

        private void split3(int from, int until, int dimension, double median) {
            lessThan = equalTo = greaterThan = 0;
            for (int i = from; i < until; ++i) {
                int cmp = Double.compare(input[indices[i]][dimension], median);
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
                int cmp = Double.compare(input[indices[i]][dimension], median);
                if (cmp < 0) {
                    swap[lessThanPtr++] = indices[i];
                } else if (cmp == 0) {
                    swap[equalToPtr++] = indices[i];
                } else {
                    swap[greaterThanPtr++] = indices[i];
                }
            }
            System.arraycopy(swap, 0, indices, from, until - from);
        }

        private void merge(int from, int mid, int until) {
            int p0 = from, p1 = mid;
            for (int i = from; i < until; ++i) {
                if (p0 == mid || p1 < until && eqComp[indices[p1]] < eqComp[indices[p0]]) {
                    swap[i] = indices[p1++];
                } else {
                    swap[i] = indices[p0++];
                }
            }
            System.arraycopy(swap, from, indices, from, until - from);
        }

        private void sortHighByLow(int lFrom, int lUntil, int hFrom, int hUntil, int dimension) {
            int lSize = lUntil - lFrom, hSize = hUntil - hFrom;
            if (lSize == 0 || hSize == 0) {
                return;
            }
            if (lSize == 1) {
                for (int hi = hFrom; hi < hUntil; ++hi) {
                    if (dominatesEq(lFrom, hi, dimension)) {
                        updateFront(indices[hi], indices[lFrom]);
                    }
                }
            } else if (hSize == 1) {
                for (int li = lFrom; li < lUntil; ++li) {
                    if (dominatesEq(li, hFrom, dimension)) {
                        updateFront(indices[hFrom], indices[li]);
                    }
                }
            } else if (dimension == 1) {
                sortHighByLow2D(lFrom, lUntil, hFrom, hUntil);
            } else {
                if (maxValue(lFrom, lUntil, dimension) <= minValue(hFrom, hUntil, dimension)) {
                    sortHighByLow(lFrom, lUntil, hFrom, hUntil, dimension - 1);
                } else {
                    System.arraycopy(indices, lFrom, swap, 0, lSize);
                    System.arraycopy(indices, hFrom, swap, lSize, hSize);
                    double median = medianInSwap(0, lSize + hSize, dimension);

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

        private void sort(int from, int until, int dimension) {
            int size = until - from;
            if (size == 2) {
                if (dominatesEq(from, from + 1, dimension)) {
                    updateFront(indices[from + 1], indices[from]);
                }
            } else if (size > 2) {
                if (dimension == 1) {
                    sort2D(from, until);
                } else {
                    if (allValuesEqual(from, until, dimension)) {
                        sort(from, until, dimension - 1);
                    } else {
                        System.arraycopy(indices, from, swap, from, size);
                        double median = medianInSwap(from, until, dimension);

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

        private boolean allValuesEqual(int from, int until, int k) {
            double value = input[indices[from]][k];
            for (int i = from + 1; i < until; ++i) {
                if (input[indices[i]][k] != value) {
                    return false;
                }
            }
            return true;
        }

        private double minValue(int from, int until, int k) {
            double rv = Double.MAX_VALUE;
            for (int i = from; i < until; ++i) {
                rv = Math.min(rv, input[indices[i]][k]);
            }
            return rv;
        }

        private double maxValue(int from, int until, int k) {
            double rv = Double.MIN_VALUE;
            for (int i = from; i < until; ++i) {
                rv = Math.max(rv, input[indices[i]][k]);
            }
            return rv;
        }

        private boolean dominatesEq(int l, int r, int k) {
            int il = indices[l];
            int ir = indices[r];
            for (int i = 0; i <= k; ++i) {
                if (input[il][i] > input[ir][i]) {
                    return false;
                }
            }
            return true;
        }
    }

    private static class MergeSorter {
        final int[] scratch;
        int[] indices = null;
        int secondIndex = -1;
        double[][] reference = null;
        int[] eqComp = null;

        public MergeSorter(int size) {
            this.scratch = new int[size];
        }

        public void lexSort(int[] indices, int from, int until, double[][] reference, int[] eqComp) {
            this.indices = indices;
            this.reference = reference;
            this.eqComp = eqComp;
            lexSortImpl(from, until, 0, 0);
            this.eqComp = null;
            this.reference = null;
            this.indices = null;
        }

        private int lexSortImpl(int from, int until, int currIndex, int compSoFar) {
            if (from + 1 < until) {
                secondIndex = currIndex;
                sortImpl(from, until);
                secondIndex = -1;

                if (currIndex + 1 == reference[0].length) {
                    eqComp[indices[from]] = compSoFar;
                    for (int i = from + 1; i < until; ++i) {
                        int prev = indices[i - 1], curr = indices[i];
                        if (reference[prev][currIndex] != reference[curr][currIndex]) {
                            ++compSoFar;
                        }
                        eqComp[curr] = compSoFar;
                    }
                    return compSoFar + 1;
                } else {
                    int lastIndex = from;
                    for (int i = from + 1; i < until; ++i) {
                        if (reference[indices[lastIndex]][currIndex] != reference[indices[i]][currIndex]) {
                            compSoFar = lexSortImpl(lastIndex, i, currIndex + 1, compSoFar);
                            lastIndex = i;
                        }
                    }
                    return lexSortImpl(lastIndex, until, currIndex + 1, compSoFar);
                }
            } else {
                eqComp[indices[from]] = compSoFar;
                return compSoFar + 1;
            }
        }

        public void sort(int[] indices, int from, int until, double[][] reference, int secondIndex) {
            this.indices = indices;
            this.reference = reference;
            this.secondIndex = secondIndex;
            sortImpl(from, until);
            this.indices = null;
            this.reference = null;
            this.secondIndex = -1;
        }

        private void sortImpl(int from, int until) {
            if (from + 1 < until) {
                int mid = (from + until) >>> 1;
                sortImpl(from, mid);
                sortImpl(mid, until);
                int i = from, j = mid, k = 0, kMax = until - from;
                while (k < kMax) {
                    if (i == mid || j < until && reference[indices[j]][secondIndex] < reference[indices[i]][secondIndex]) {
                        scratch[k] = indices[j];
                        ++j;
                    } else {
                        scratch[k] = indices[i];
                        ++i;
                    }
                    ++k;
                }
                System.arraycopy(scratch, 0, indices, from, kMax);
            }
        }
    }
}
