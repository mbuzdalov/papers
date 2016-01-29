package ru.ifmo.eps.orq;

public abstract class ORQBuilder {
    public abstract OrthogonalRangeQuery build(int internalDimension);
    public abstract String getName();
}
