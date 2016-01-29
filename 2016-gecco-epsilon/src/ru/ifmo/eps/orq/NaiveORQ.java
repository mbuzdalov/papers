package ru.ifmo.eps.orq;

import java.util.ArrayList;
import ru.ifmo.eps.util.*;

public class NaiveORQ extends ORQBuilder {
    private static class Implementation extends OrthogonalRangeQuery {
        private ArrayWrapper points;
        private int[] indices;
        private int size;

        public void init(ArrayWrapper points) {
            this.points = points;
            if (indices == null || indices.length < points.size()) {
                indices = new int[points.size()];
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
                double[] pt = points.get(indices[i]);
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
