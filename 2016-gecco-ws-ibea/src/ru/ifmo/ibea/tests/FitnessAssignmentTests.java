package ru.ifmo.ibea.tests;

import ru.ifmo.ibea.*;

public class FitnessAssignmentTests {
    private static final int maxN = 100000;
    private static final int maxD = 10;
    private static final double kappa = 0.05;

    private static void checkEqual(double[] expected, double[] found) {
        for (int i = 0; i < expected.length; ++i) {
            double diff = expected[i] - found[i];
            double base = Math.max(Math.abs(expected[i]), 1.0);
            if (Math.abs(diff) / base > 1e-9) {
                throw new AssertionError(i + "th component differ: expected "
                                        + expected[i] + " found " + found[i]);
            }
        }
    }

    private static void singleTest(FitnessAssignment algo, String name, double[][] points, double[] expected) {
        System.out.println("    " + name);
        double[] found = new double[points.length];
        algo.assignFitness(points, found);
        checkEqual(expected, found);
    }

    private static void testOne(FitnessAssignment algo) {
        singleTest(algo, "testOne", new double[][] {
            { 1.0, 1.0, 1.0 }
        }, new double[] {
            0.0
        });
    }

    private static void testTwoComparable(FitnessAssignment algo) {
        singleTest(algo, "testTwoComparable", new double[][] {
            { 2.5, 3.5 }, { 3.5, 4.5 }
        }, new double[] {
            -Math.exp(-1 / kappa), -Math.exp(1 / kappa)
        });
    }

    private static void testTwoIncomparable(FitnessAssignment algo) {
        singleTest(algo, "testTwoIncomparable", new double[][] {
            { 2.5, 3.5 }, { 3.5, 2.5 }
        }, new double[] {
            -Math.exp(-1 / kappa), -Math.exp(-1 / kappa)
        });
    }

    private static void testTwoEqual(FitnessAssignment algo) {
        singleTest(algo, "testTwoEqual", new double[][] {
            { 2.5, 3.5, 4.5 }, { 2.5, 3.5, 4.5 }
        }, new double[] {
            -1.0, -1.0
        });
    }

    private static void testFour(FitnessAssignment algo) {
        singleTest(algo, "testFour", new double[][] {
            { 0.0, 0.0 }, { 1.0, 1.0 }, { 0.0, 1.0 }, { 1.0, 0.0 }
        }, new double[] {
            -3 * Math.exp(-1 / kappa), -Math.exp(1 / kappa) - 2, -2 * Math.exp(-1 / kappa) - 1, -2 * Math.exp(-1 / kappa) - 1
        });
    }

    private static void test(FitnessAssignment algo, String name) {
        System.out.println("  Running tests for " + name);
        testOne(algo);
        testTwoComparable(algo);
        testTwoIncomparable(algo);
        testTwoEqual(algo);
        testFour(algo);
        System.out.println("  Tests passed for " + name);
    }

    public static void main(String[] args) {
        System.out.println("Running fitness assignment tests");
        test(new BruteForceFitnessAssignment(maxN, maxD, kappa), "BruteForceFitnessAssignment");
        System.out.println("Tests passed");
    }
}
