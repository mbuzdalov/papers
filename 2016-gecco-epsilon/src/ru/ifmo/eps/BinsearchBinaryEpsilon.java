package ru.ifmo.eps;

import java.util.*;
import ru.ifmo.eps.util.*;

public class BinsearchBinaryEpsilon extends BinaryEpsilon {
    private static class ArrayWrapper2 extends ArrayWrapper {
        boolean[] isDominated;

        public ArrayWrapper2(double[][] contents) {
            super(contents);
            this.isDominated = new boolean[contents.length];
        }

        public void reset() {
            Arrays.fill(isDominated, false);
        }

        public void setDominated(int index) {
            isDominated[idx[index]] = true;
        }

        public boolean isDominated() {
            for (boolean b : isDominated) {
                if (!b) return false;
            }
            return true;
        }
    }

    private static class DominationRunner {
        ArrayWrapper moving;
        ArrayWrapper2 fixed;
        double offset;
        double[] medianSwap;

        DominationRunner(ArrayWrapper moving, ArrayWrapper2 fixed) {
            this.moving = moving;
            this.fixed = fixed;
            this.medianSwap = new double[moving.size() + fixed.size()];
        }

        void updateDomination(int mi, int fi, int k) {
            for (int i = 0; i <= k; ++i) {
                if (moving.get(mi, i) + offset > fixed.get(fi, i)) {
                    return;
                }
            }
            fixed.setDominated(fi);
        }

        void assignDomination(int mL, int mR, int fL, int fR, int k) {
            if (fL == fR || mL == mR) {
                return;
            }
            if (fL + 1 == fR || mL + 1 == mR) {
                // Testing everyone to everyone
                for (int fi = fL; fi < fR; ++fi) {
                    for (int mi = mL; mi < mR; ++mi) {
                        updateDomination(mi, fi, k);
                    }
                }
            } else if (k == 1) {
                // Two-dimensional case, works in linear time
                int mi = mL, fi = fL;
                double lastY = Double.POSITIVE_INFINITY;
                while (mi < mR && fi < fR) {
                    double mX = moving.get(mi, 0) + offset;
                    double mY = moving.get(mi, 1) + offset;
                    double fX = fixed.get(fi, 0);
                    double fY = fixed.get(fi, 1);
                    if (mX < fX || mX == fX && mY < fY) {
                        if (mY < lastY) {
                            lastY = mY;
                        }
                        ++mi;
                    } else {
                        if (fY >= lastY) {
                            fixed.setDominated(fi);
                        }
                        ++fi;
                    }
                }
                while (fi < fR) {
                    if (fixed.get(fi, 1) >= lastY) {
                        fixed.setDominated(fi);
                    }
                    ++fi;
                }
            } else {
                int mc = 0;
                for (int mi = mL; mi < mR; ++mi) {
                    medianSwap[mc++] = moving.get(mi, k) + offset;
                }
                for (int fi = fL; fi < fR; ++fi) {
                    medianSwap[mc++] = fixed.get(fi, k);
                }
                double median = Miscellaneous.destructiveMedian(medianSwap, 0, mc - 1);

                moving.split(mL, mR, median - offset, k);
                int mML = moving.splitL;
                int mMR = moving.splitR;

                fixed.split(fL, fR, median, k);
                int fML = fixed.splitL;
                int fMR = fixed.splitR;

                assignDomination(mL, mML, fL, fML, k);
                assignDomination(mMR, mR, fMR, fR, k);

                moving.merge(mL, mML, mMR);
                fixed.merge(fML, fMR, fR);

                assignDomination(mL, mMR, fML, fR, k - 1);

                moving.merge(mL, mMR, mR);
                fixed.merge(fL, fML, fR);
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
            fixed.reset();
            assignDomination(0, moving.size(), 0, fixed.size(), moving.dimension() - 1);
            return fixed.isDominated();
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

        DominationRunner runner = new DominationRunner(new ArrayWrapper(moving), new ArrayWrapper2(fixed));

        for (int iterations = 0; iterations < 40 && right - left > 1e-9; ++iterations) {
            double mid = left + (right - left) / 2;
            boolean ans = runner.dominates(mid);
            if (ans) {
                right = mid;
            } else {
                left = mid;
            }
        }
        return right;
    }


    @Override
    public String getName() {
        return "BinsearchBinaryEpsilon";
    }
}
