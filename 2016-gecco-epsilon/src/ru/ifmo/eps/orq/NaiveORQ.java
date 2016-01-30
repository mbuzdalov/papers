package ru.ifmo.eps.orq;

import java.util.ArrayList;
import ru.ifmo.eps.util.*;

public class NaiveORQ extends ORQBuilder {
    private static class Implementation extends OrthogonalRangeQuery {
        private double[][] points;
        private int size;

        public void init(ArrayWrapper points) {
            if (this.points == null || this.points.length < points.size()) {
                this.points = new double[points.size()][];
            }
            this.size = 0;
        }

        public void clear() {
            size = 0;
        }

        public void add(double[] point) {
            points[size++] = point;
        }

        public double getMin(double[] lowerBound) {
            double rv = Double.POSITIVE_INFINITY;
            int lim = lowerBound.length - 1;
            for (int i = 0; i < size; ++i) {
                double[] pt = points[i];
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
    }

    public OrthogonalRangeQuery build(int internalDimension) {
        return new Implementation();
    }

    public String getName() {
        return "NaiveORQ";
    }

    public static final NaiveORQ INSTANCE = new NaiveORQ();
}
