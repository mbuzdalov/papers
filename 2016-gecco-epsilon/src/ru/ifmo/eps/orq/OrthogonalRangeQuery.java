package ru.ifmo.eps.orq;

import ru.ifmo.eps.util.*;

public abstract class OrthogonalRangeQuery {
    /**
     * Considering indices 1 ... L-2 of the given array of length L,
     * returns the minimum of the L-1 index of all added points
     * which are not less in the considered indices.
     */
    public abstract double getMin(double[] lowerBound);
    /**
     * Tells the data structure that the {#points} would be added.
     */
    public abstract void init(ArrayWrapper points);
    /**
     * Adds the next point. The {#index} is the index of the point
     * in the ArrayWrapper "points" passed to in {#init(double[][], int[])}.
     */
    public abstract void add(int index);
    /**
     * Indicates the data structure that it can free all resouces.
     */
    public abstract void clear();
}
