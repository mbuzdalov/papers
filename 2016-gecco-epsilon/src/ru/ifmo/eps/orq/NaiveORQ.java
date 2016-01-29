package ru.ifmo.eps.orq;

import java.util.ArrayList;

public class NaiveORQ extends OrthogonalRangeQuery {
    private double[][] points;
    private int[] indices;
    private int size;

    public void init(double[][] points) {
        this.points = points;
        if (indices == null || indices.length < points.length) {
            indices = new int[points.length];
        }
        this.size = 0;
    }

    public void clear() {
        points = null;
    }

    public void add(int index) {
        indices[size++] = index;
    }

    public double getMin(double[] lowerBound) {
        double rv = Double.POSITIVE_INFINITY;
        int lim = lowerBound.length - 1;
        for (int i = 0; i < size; ++i) {
            double[] pt = points[indices[i]];
            boolean ok = true;
            for (int j = 1; j < lim; ++j) {
                if (pt[j] < lowerBound[j]) {
                    ok = false;
                    break;
                }
            }
            if (ok && rv > pt[lim]) {
                rv = pt[lim];
            }
        }
        return rv;
    }

    public String getName() {
        return "NaiveORQ";
    }
}
