package ru.ifmo.eps.orq;

import java.util.ArrayList;

public class NaiveORQ extends OrthogonalRangeQuery {
    private ArrayList<double[]> points = new ArrayList<>();

    public void hint(double[][] points) {
        this.points.ensureCapacity(points.length);
    }

    public void clear() {
        points.clear();
    }

    public void add(double[] point) {
        points.add(point);
    }

    public double getMin(double[] lowerBound) {
        double rv = Double.POSITIVE_INFINITY;
        int lim = lowerBound.length - 1;
        for (int i = 0, s = points.size(); i < s; ++i) {
            double[] pt = points.get(i);
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
