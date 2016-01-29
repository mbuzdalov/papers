package ru.ifmo.eps.orq;

public abstract class OrthogonalRangeQuery {
    public abstract double getMin(double[] lowerBound);
    public abstract void add(double[] point);
    public abstract void clear();
    public abstract void hint(double[][] points);
    public abstract String getName();
}
