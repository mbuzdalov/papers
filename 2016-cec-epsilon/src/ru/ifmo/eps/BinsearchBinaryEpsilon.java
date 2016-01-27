package ru.ifmo.eps;

import java.util.*;

public class BinsearchBinaryEpsilon extends BinaryEpsilon {
    private static class ArrayWrapper {
        double[][] contents;
        int[] idx;
        int[] ord;
        int[] swp;
        int[] swp2;
        int dimension;

        int splitL, splitR;

        public ArrayWrapper(double[][] contents) {
            this.contents = contents;
            this.dimension = contents[0].length;
            this.idx = new int[contents.length];
            this.swp = new int[contents.length];
            this.swp2 = new int[contents.length];
            for (int i = 0; i < contents.length; ++i) {
                idx[i] = i;
            }
            lexSort(0, contents.length, 0);
            this.ord = new int[contents.length];
            for (int i = 0; i < contents.length; ++i) {
                ord[idx[i]] = i;
            }
///  <<<
            for (int i = 1; i < contents.length; ++i) {
                double[] l = contents[idx[i - 1]];
                double[] r = contents[idx[i]];
                for (int j = 0; j < l.length; ++j) {
                    if (l[j] > r[j]) throw new AssertionError();
                    if (l[j] < r[j]) break;
                }
            }
///  >>>
        }

        public int size() {
            return contents.length;
        }

        public int dimension() {
            return dimension;
        }

        public double get(int index, int k) {
            return contents[idx[index]][k];
        }

        public void split(int left, int right, double median, int k) {
            int lp = left, rp = right, mp = 0;
            for (int i = left; i < right; ++i) {
                double cc = contents[idx[i]][k];
                if (cc < median) {
                    swp[lp++] = idx[i];
                } else if (cc > median) {
                    swp[--rp] = idx[i];
                } else {
                    swp2[mp++] = idx[i];
                }
            }
            System.arraycopy(swp, left, idx, left, right - left);
            for (int l = rp, r = right - 1; l < r; ++l, --r) {
                int tmp = idx[l];
                idx[l] = idx[r];
                idx[r] = tmp;
            }
            System.arraycopy(swp2, 0, idx, lp, mp);
            splitL = lp;
            splitR = rp;
///  <<<
            for (int i = left; i < splitL; ++i) {
                if (contents[idx[i]][k] >= median) throw new AssertionError();
                if (i > left && ord[idx[i - 1]] > ord[idx[i]]) throw new AssertionError();
            }
            for (int i = splitL; i < splitR; ++i) {
                if (contents[idx[i]][k] != median) throw new AssertionError();
                if (i > splitL && ord[idx[i - 1]] > ord[idx[i]]) throw new AssertionError();
            }
            for (int i = splitR; i < right; ++i) {
                if (contents[idx[i]][k] <= median) throw new AssertionError();
                if (i > splitR && ord[idx[i - 1]] > ord[idx[i]]) throw new AssertionError();
            }
///  >>>
        }

        public void merge(int left, int mid, int right) {
            for (int l = left, m = mid, t = left; t < right; ++t) {
                if (m == right || l < mid && ord[idx[l]] < ord[idx[m]]) {
                    swp[t] = idx[l++];
                } else {
                    swp[t] = idx[m++];
                }
            }
            System.arraycopy(swp, left, idx, left, right - left);
        }

        private void lexSort(int left, int right, int k) {
            mergeSort(left, right, k);
            if (k + 1 < dimension) {
                int prev = left;
                for (int i = left + 1; i < right; ++i) {
                    if (contents[idx[i - 1]][k] < contents[idx[i]][k]) {
                        lexSort(prev, i, k + 1);
                        prev = i;
                    }
                }
                lexSort(prev, right, k + 1);
            }
        }

        private void mergeSort(int left, int right, int k) {
            if (left + 1 < right) {
                int mid = (left + right) >>> 1;
                mergeSort(left, mid, k);
                mergeSort(mid, right, k);
                for (int i = left, j = mid, t = left; t < right; ++t) {
                    if (i == mid || j < right && contents[idx[j]][k] <= contents[idx[i]][k]) {
                        swp[t] = idx[j++];
                    } else {
                        swp[t] = idx[i++];
                    }
                }
                System.arraycopy(swp, left, idx, left, right - left);
            }
        }
    }

    private static class DominationRunner {
        ArrayWrapper moving, fixed;
        double offset;
        double[] medianSwap;

        DominationRunner(ArrayWrapper moving, ArrayWrapper fixed) {
            this.moving = moving;
            this.fixed = fixed;
            this.medianSwap = new double[moving.size() + fixed.size()];
        }

        boolean dominatesNonStrict(int mi, int fi, int k) {
            for (int i = 0; i <= k; ++i) {
                if (moving.get(mi, i) + offset > fixed.get(fi, i)) {
                    return false;
                }
            }
            return true;
        }

        boolean dominates(int mL, int mR, int fL, int fR, int k) {
            if (fL == fR) {
                return true;    // nothing to be dominated
            }
            if (mL == mR) {
                return false;   // nothing to dominate by
            }
            if (fL + 1 == fR || mL + 1 == mR) {
                // Testing everyone to everyone
                for (int mi = mL; mi < mR; ++mi) {
                    for (int fi = fL; fi < fR; ++fi) {
                        if (!dominatesNonStrict(mi, fi, k)) {
                            return false;
                        }
                    }
                }
                return true;
            }
            if (k == 1) {
                // Two-dimensional case, work in linear time
                int mi = mL, fi = fL;
                double mX = moving.get(mi, 0) - moving.get(mi, 1);
                double fX = fixed.get(fi, 0) - fixed.get(fi, 1);
                boolean fixedIsDominated = false;
                while (true) {
                    fixedIsDominated |= dominatesNonStrict(mi, fi, 1);
                    if (mX <= fX) {
                        ++mi;
                        if (mi < mR) {
                            mX = moving.get(mi, 0) - moving.get(mi, 1);
                        } else {
                            break;
                        }
                    } else {
                        if (!fixedIsDominated) {
                            return false;
                        }
                        ++fi;
                        if (fi < fR) {
                            fX = fixed.get(fi, 0) - fixed.get(fi, 1);
                            fixedIsDominated = false; // new slot is allocated
                        } else {
                            break;
                        }
                    }
                }
                return fixedIsDominated;
            } else {
                int mc = 0;
                for (int mi = mL; mi < mR; ++mi) {
                    medianSwap[mc++] = offset + moving.get(mi, k);
                }
                for (int fi = fL; fi < fR; ++fi) {
                    medianSwap[mc++] = fixed.get(fi, k);
                }
                double median = kth(0, mc - 1, mc / 2);

                moving.split(mL, mR, median - offset, k);
                int mML = moving.splitL;
                int mMR = moving.splitR;

                fixed.split(fL, fR, median, k);
                int fML = fixed.splitL;
                int fMR = fixed.splitR;

                boolean rv = dominates(mL, mML, fL, fML, k)
                          && dominates(mMR, mR, fMR, fR, k);

                moving.merge(mL, mML, mMR);
                fixed.merge(fML, fMR, fR);

                rv &= dominates(mL, mMR, fML, fR, k - 1);

                moving.merge(mL, mMR, mR);
                fixed.merge(fL, fML, fR);

                return rv;
            }
        }

        double kth(int left, int right, int index) {
            while (true) {
                double pivot = (medianSwap[left] + medianSwap[right]) / 2;
                int l = left, r = right;
                while (l <= r) {
                    while (medianSwap[l] < pivot) ++l;
                    while (medianSwap[r] > pivot) --r;
                    if (l <= r) {
                        double tmp = medianSwap[l];
                        medianSwap[l] = medianSwap[r];
                        medianSwap[r] = tmp;
                        ++l;
                        --r;
                    }
                }
                if (index <= r) {
                    right = r;
                } else if (l <= index) {
                    left = l;
                } else {
                    return medianSwap[index];
                }
            }
        }

        boolean dominates(double offset) {
            this.offset = -offset; // offset is positive when you move towards zero; funny one.
            return dominates(0, moving.size(), 0, fixed.size(), moving.dimension() - 1);
        }
    }

    @Override
    protected double computeBinaryEpsilonImpl(double[][] moving, double[][] fixed) {
        double left = Double.POSITIVE_INFINITY, right = Double.NEGATIVE_INFINITY;
        int dimension = moving[0].length;
        for (int k = 0; k < dimension; ++k) {
            double minFixed = fixed[0][k];
            double minMoving = moving[0][k];
            double maxMoving = moving[0][k];
            for (int i = 1; i < fixed.length; ++i) {
                minFixed = Math.min(minFixed, fixed[i][k]);
            }
            for (int i = 1; i < moving.length; ++i) {
                minMoving = Math.min(minMoving, moving[i][k]);
                maxMoving = Math.max(maxMoving, moving[i][k]);
            }
            left = Math.min(left, minMoving - minFixed);
            right = Math.max(right, maxMoving - minFixed);
        }

        DominationRunner runner = new DominationRunner(new ArrayWrapper(moving), new ArrayWrapper(fixed));

        for (int iterations = 0; iterations < 40 && right - left > 1e-9; ++iterations) {
            double mid = left + (right - left) / 2;
            if (runner.dominates(mid)) {
                right = mid;
            } else {
                left = mid;
            }
        }
        return right;
    }
}
