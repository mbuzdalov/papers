package ru.ifmo.steady;

import java.util.HashSet;
import java.util.Set;

public class SolutionStorageTests {
    private final SolutionStorage storage;
    private final String name;

    public SolutionStorageTests(SolutionStorage storage, String name) {
        this.storage = storage;
        this.name = name;
    }

    public void run() {
        System.out.println("Running tests for " + name);
        System.out.print("  testOne      -> "); testOne();      System.out.println("OK");
        System.out.print("  testDiag     -> "); testDiag();     System.out.println("OK");
        System.out.print("  testCrowding -> "); testCrowding(); System.out.println("OK");
        System.out.print("  testQueries  -> "); testQueries();  System.out.println("OK");
    }

    private static Solution s(double x, double y) {
        return new Solution(x, y);
    }

    private static SolutionStorage.QueryResult q(double x, double y, double crowding, int layer) {
        return new SolutionStorage.QueryResult(s(x, y), crowding, layer);
    }

    private static <T> void expect(T expected, T found) {
        if (!expected.equals(found)) {
            throw new AssertionError("Expected " + expected + " found " + found);
        }
    }

    private void testOne() {
        storage.clear();
        expect(0, storage.size());
        storage.add(s(1, 1));
        expect(1, storage.size());
        expect(s(1, 1), storage.removeWorst());
        expect(0, storage.size());
    }

    private void testDiag() {
        storage.clear();
        expect(0, storage.size());
        storage.add(s(4, 4));
        expect(1, storage.size());
        storage.add(s(1, 1));
        expect(2, storage.size());
        storage.add(s(6, 6));
        expect(3, storage.size());
        storage.add(s(4, 4));
        expect(4, storage.size());
        expect(s(6, 6), storage.removeWorst());
        expect(3, storage.size());
        expect(s(4, 4), storage.removeWorst());
        expect(2, storage.size());
        expect(s(4, 4), storage.removeWorst());
        expect(1, storage.size());
        expect(s(1, 1), storage.removeWorst());
        expect(0, storage.size());
    }

    private void testCrowding() {
        storage.clear();
        storage.add(s(0, 7));
        storage.add(s(1, 5));
        storage.add(s(2, 4));
        storage.add(s(5, 3));
        storage.add(s(7, 1));
        storage.add(s(8, 0));
        expect(s(1, 5), storage.removeWorst());
        expect(s(7, 1), storage.removeWorst());
        expect(s(2, 4), storage.removeWorst());
        expect(s(5, 3), storage.removeWorst());
        Solution a = storage.removeWorst();
        Solution b = storage.removeWorst();
        expect(true, a.equals(s(0, 7)) && b.equals(s(8, 0)) || a.equals(s(8, 0)) && b.equals(s(0, 7)));
    }

    private void testQueries() {
        storage.clear();
        storage.add(s(1, 6));
        storage.add(s(0, 5));
        storage.add(s(2, 5));
        storage.add(s(3, 4));
        storage.add(s(0, 3));
        storage.add(s(1, 3));
        storage.add(s(4, 3));
        storage.add(s(2, 2));
        storage.add(s(1, 1));
        storage.add(s(5, 1));
        storage.add(s(0, 0));
        storage.add(s(2, 0));
        expect(12, storage.size());

        Set<SolutionStorage.QueryResult> queries = new HashSet<>();
        for (int i = 0; i < 120; ++i) {
            queries.add(storage.getRandom());
        }
        final double I = Double.POSITIVE_INFINITY;
        expect(true, queries.remove(q(0, 0, I, 0)));
        expect(true, queries.remove(q(2, 0, I, 1)));
        expect(true, queries.remove(q(1, 1, 6, 1)));
        expect(true, queries.remove(q(0, 3, I, 1)));
        expect(true, queries.remove(q(5, 1, I, 2)));
        expect(true, queries.remove(q(2, 2, 8, 2)));
        expect(true, queries.remove(q(1, 3, 6, 2)));
        expect(true, queries.remove(q(0, 5, I, 2)));
        expect(true, queries.remove(q(4, 3, I, 3)));
        expect(true, queries.remove(q(3, 4, 4, 3)));
        expect(true, queries.remove(q(2, 5, 4, 3)));
        expect(true, queries.remove(q(1, 6, I, 3)));
        expect(0, queries.size());
    }

    public static void main(String[] args) {
        new SolutionStorageTests(new ru.ifmo.steady.inds.Storage(), "INDS").run();
        new SolutionStorageTests(new ru.ifmo.steady.enlu.Storage(), "ENLU").run();
    }
}
