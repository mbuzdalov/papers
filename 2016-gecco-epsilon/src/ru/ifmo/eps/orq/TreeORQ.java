package ru.ifmo.eps.orq;

import java.util.*;
import ru.ifmo.eps.util.*;

public class TreeORQ extends ORQBuilder {
    private static class Tree0D extends OrthogonalRangeQuery {
        ArrayWrapper points;
        double minimum;

        public double getMin(double[] lowerBound) {
            return minimum;
        }

        public void init(ArrayWrapper points) {
            this.points = points;
            this.minimum = Double.POSITIVE_INFINITY;
        }

        public void add(int index) {
            minimum = Math.min(minimum, points.get(index, 1));
        }

        public void clear() {
            this.points = null;
        }
    }

    public OrthogonalRangeQuery build(int internalDimension) {
        if (internalDimension == 0) {
            return new Tree0D();
        } else {
            return NaiveORQ.INSTANCE.build(internalDimension);
        }
    }

    public String getName() {
        return "TreeORQ";
    }

    public static ORQBuilder INSTANCE = new TreeORQ();
}
