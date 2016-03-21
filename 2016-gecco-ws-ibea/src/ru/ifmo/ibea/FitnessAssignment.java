package ru.ifmo.ibea;

import java.util.*;

public abstract class FitnessAssignment {
    protected final int maxPoints, maxDimension;
    private double[] minCoordinate, maxCoordinate;
    private double[] minima, maxima;
    private double kappa;

    protected FitnessAssignment(int maxPoints, int maxDimension, double kappa) {
        this.maxPoints = maxPoints;
        this.maxDimension = maxDimension;
        this.minima = new double[maxPoints];
        this.maxima = new double[maxPoints];
        this.minCoordinate = new double[maxDimension];
        this.maxCoordinate = new double[maxDimension];
        this.kappa = kappa;
    }

    public void assignFitness(double[][] points, double[] fitness) {
        // 0. Basic bounds checking
        if (points.length > maxPoints) {
            throw new IllegalArgumentException("Too many points: " + points.length
                                              + ", maximum supported is " + maxPoints);
        }
        if (points.length == 0) {
            return;
        }
        int dimension = points[0].length;
        if (dimension > maxDimension) {
            throw new IllegalArgumentException("Too large dimension: " + dimension
                                              + ", maximum supported is " + maxDimension);
        }
        if (fitness.length < points.length) {
            throw new IllegalArgumentException("Fitness array is too small");
        }

        // 1. Normalizing coordinates.
        Arrays.fill(minCoordinate, Double.POSITIVE_INFINITY);
        Arrays.fill(maxCoordinate, Double.NEGATIVE_INFINITY);
        for (double[] point : points) {
            for (int i = 0; i < dimension; ++i) {
                minCoordinate[i] = Math.min(minCoordinate[i], point[i]);
                maxCoordinate[i] = Math.max(maxCoordinate[i], point[i]);
            }
        }
        for (double[] point : points) {
            for (int i = 0; i < dimension; ++i) {
                point[i] = (point[i] - minCoordinate[i]) / (maxCoordinate[i] - minCoordinate[i]);
            }
        }

        // 2. Determining maximum pairwise indicator.
        // Reusing the fitness array...
        assignIncomingIndicators(points, minima, maxima);
        double maxIndicator = Double.NEGATIVE_INFINITY;
        double minIndicator = Double.POSITIVE_INFINITY;
        for (double ind : minima) {
            minIndicator = Math.min(minIndicator, ind);
        }
        for (double ind : maxima) {
            maxIndicator = Math.max(maxIndicator, ind);
        }
        double quotient = kappa * Math.max(Math.abs(maxIndicator), Math.abs(minIndicator));

        // 3. Running the assignment scheme
        assignNormalizedFitness(points, fitness, quotient);
    }

    protected abstract void assignIncomingIndicators(double[][] points, double[] minima, double[] maxima);
    protected abstract void assignNormalizedFitness(double[][] points, double[] fitness, double quotient);
}
