package ru.ifmo.eps.util;

import java.util.*;

public class ArrayWrapper {
    protected double[][] contents;
    protected int[] idx;
    protected int[] ord;
    protected int[] swp;
    protected int[] swp2;
    protected int dimension;
    protected int smallestMeaningfulCoordinate;
    protected int largestMeaningfulCoordinate;

    public int splitL, splitR;

    public ArrayWrapper(double[][] contents) {
        this(contents, 0, contents[0].length - 1);
    }

    public ArrayWrapper(double[][] contents, int smallestMeaningfulCoordinate,
                                             int largestMeaningfulCoordinate) {
        this.contents = contents;
        this.dimension = contents[0].length;
        this.idx = new int[contents.length];
        this.swp = new int[contents.length];
        this.swp2 = new int[contents.length];
        this.smallestMeaningfulCoordinate = smallestMeaningfulCoordinate;
        this.largestMeaningfulCoordinate = largestMeaningfulCoordinate;
        for (int i = 0; i < contents.length; ++i) {
            idx[i] = i;
        }
        lexSort(0, contents.length, smallestMeaningfulCoordinate);
        this.ord = new int[contents.length];
        for (int i = 0; i < contents.length; ++i) {
            ord[idx[i]] = i;
        }
    }

    public int smallestMeaningfulCoordinate() {
        return smallestMeaningfulCoordinate;
    }

    public int largestMeaningfulCoordinate() {
        return largestMeaningfulCoordinate;
    }

    public void reloadContents() {
        for (int i = 0; i < contents.length; ++i) {
            idx[i] = i;
        }
        lexSort(0, contents.length, smallestMeaningfulCoordinate);
        for (int i = 0; i < contents.length; ++i) {
            ord[idx[i]] = i;
        }
    }

    public int size() {
        return contents.length;
    }

    public int dimension() {
        return dimension;
    }

    public int getIndex(int index) {
        return idx[index];
    }

    public double[] get(int index) {
        return contents[idx[index]];
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
    }

    public void merge(int left, int mid, int right) {
        if (left == mid || mid == right) {
            return;
        }
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
        if (k + 1 <= largestMeaningfulCoordinate) {
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
                if (i == mid || j < right && contents[idx[j]][k] < contents[idx[i]][k]) {
                    swp[t] = idx[j++];
                } else {
                    swp[t] = idx[i++];
                }
            }
            System.arraycopy(swp, left, idx, left, right - left);
        }
    }
}
