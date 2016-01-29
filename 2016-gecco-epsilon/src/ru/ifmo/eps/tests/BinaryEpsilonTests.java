package ru.ifmo.eps.tests;

import java.util.Objects;
import ru.ifmo.eps.*;

public class BinaryEpsilonTests {
    private BinaryEpsilon algorithm;
    public BinaryEpsilonTests(BinaryEpsilon algorithm) {
        this.algorithm = algorithm;
    }

    protected double runEpsilon(double[][] moving, double[][] fixed) {
        return algorithm.computeBinaryEpsilon(moving, fixed);
    }

    protected void assertEquals(double expected, double found, double tolerance) {
        if (Math.abs(expected - found) > tolerance) {
            throw new AssertionError("Expected " + expected + " found " + found + " tolerance " + tolerance);
        }
    }

    public void singleEqualPoints() {
        System.out.print("        singleEqualPoints()...");
        double[] point = {1, 2, 3, 4, 5};
        assertEquals(0, runEpsilon(new double[][] { point }, new double[][] { point }), 1e-9);
        System.out.println(" OK");
    }

    public void singleIncomparablePoints() {
        System.out.print("        singleIncomparablePoints()...");
        double[] pointA = {1, 0};
        double[] pointB = {0, 2};
        assertEquals(1, runEpsilon(new double[][] { pointA }, new double[][] { pointB }), 1e-9);
        assertEquals(2, runEpsilon(new double[][] { pointB }, new double[][] { pointA }), 1e-9);
        System.out.println(" OK");
    }

    public void singleDominatingPoints() {
        System.out.print("        singleDominatingPoints()...");
        double[] pointA = {1, 1};
        double[] pointB = {3, 2};
        assertEquals(-1, runEpsilon(new double[][] { pointA }, new double[][] { pointB }), 1e-9);
        assertEquals(2,  runEpsilon(new double[][] { pointB }, new double[][] { pointA }), 1e-9);
        System.out.println(" OK");
    }

    public void parallelSets() {
        System.out.print("        parallelSets()...");
        double[][] setA = {{2, 0}, {0, 2}};
        double[][] setB = {{3, 1}, {1, 3}};
        assertEquals(-1, runEpsilon(setA, setB), 1e-9);
        assertEquals(1,  runEpsilon(setB, setA), 1e-9);
        System.out.println(" OK");
    }

    public void notSoParallelSets() {
        System.out.print("        notSoParallelSets()...");
        double[][] setA = {{1, 0}, {0, 2}};
        double[][] setB = {{2, 1}, {0, 3}};
        assertEquals(0, runEpsilon(setA, setB), 1e-9);
        assertEquals(1, runEpsilon(setB, setA), 1e-9);
        System.out.println(" OK");
    }

    public void crossingSets() {
        System.out.print("        crossingSets()...");
        double[][] setA = {{0, 1}, {3, 2}};
        double[][] setB = {{1, 3}, {3, 1}};
        assertEquals(0, runEpsilon(setA, setB), 1e-9);
        assertEquals(2, runEpsilon(setB, setA), 1e-9);
        System.out.println(" OK");
    }

    public void dominationInMoving() {
        System.out.print("        dominationInMoving()...");
        double[][] moving = {
            { 0.0, 9.0, },
            { 3.0, 9.0, },
        };
        double[][] fixed = {
            { 5.0, 0.0, },
            { 1.0, 1.0, },
        };
        assertEquals(9, runEpsilon(moving, fixed), 1e-9);
        System.out.println(" OK");
    }

    public void simple3D() {
        System.out.print("        simple3D()...");
        double[][] moving = {
            { 34.0, 72.0, 48.0, },
            { 69.0, 8.0, 57.0, },
        };
        double[][] fixed = {
            { 80.0, 94.0, 40.0, },
            { 57.0, 45.0, 64.0, },
        };
        assertEquals(12, runEpsilon(moving, fixed), 1e-9);
        System.out.println(" OK");
    }

    public void runTests() {
        System.out.println("    Running " + algorithm.getName());
        singleEqualPoints();
        singleIncomparablePoints();
        singleDominatingPoints();
        parallelSets();
        notSoParallelSets();
        crossingSets();
        dominationInMoving();
        simple3D();
    }
}
