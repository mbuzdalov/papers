package ru.ifmo.steady.nsga2ss;

import ru.ifmo.steady.Solution;

public class Tests {
    private static Solution s(double x, double y) {
        return new Solution(x, y);
    }

    private static void expectEqual(int expected, int found) {
        if (expected != found) {
            throw new AssertionError("Expected " + expected + " found " + found);
        }
    }

    private static void expectEqual(Solution solution, double x, double y) {
        Solution base = s(x, y);
        if (base.compareX(solution) != 0 || base.compareY(solution) != 0) {
            throw new AssertionError("Expected " + base + " found " + solution);
        }
    }

    private static void testSolutionStorage() {
        SolutionStorage ss = new SolutionStorage(8);
        ss.add(s(1, 6)); expectEqual(1, ss.size());
        ss.add(s(2, 5)); expectEqual(2, ss.size());
        ss.add(s(3, 4)); expectEqual(3, ss.size());
        ss.add(s(4, 3)); expectEqual(4, ss.size());
        ss.add(s(5, 1)); expectEqual(5, ss.size());
        ss.add(s(0, 5)); expectEqual(6, ss.size());
        ss.add(s(0, 3)); expectEqual(7, ss.size());
        ss.add(s(0, 0)); expectEqual(8, ss.size());
        ss.add(s(1, 3)); expectEqual(8, ss.size());
        ss.add(s(1, 1)); expectEqual(8, ss.size());
        ss.add(s(2, 2)); expectEqual(8, ss.size());
        ss.add(s(2, 0)); expectEqual(8, ss.size());

        expectEqual(8, ss.size());
        expectEqual(ss.deleteWorst(), 1, 3);
        expectEqual(ss.deleteWorst(), 2, 2);
        ss.deleteWorst();
        ss.deleteWorst();

        expectEqual(4, ss.size());
        expectEqual(ss.deleteWorst(), 1, 1);
        ss.deleteWorst();
        ss.deleteWorst();

        expectEqual(1, ss.size());
        expectEqual(ss.deleteWorst(), 0, 0);
        expectEqual(0, ss.size());
    }

    public static void main(String[] args) {
        System.out.print("Testing ru.ifmo.steady.nsga2ss.SolutionStorage... ");
        testSolutionStorage();
        System.out.println("OK.");
    }
}
