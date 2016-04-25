package ru.ifmo.eps.omq;

public abstract class OMQBuilder {
    public abstract OrthantMinimumQuery build(int internalDimension);
    public abstract String getName();
}
