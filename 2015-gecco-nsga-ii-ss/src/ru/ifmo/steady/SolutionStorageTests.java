package ru.ifmo.steady;

import java.util.*;

public class SolutionStorageTests {
    private final SolutionStorage storage;

    public SolutionStorageTests(SolutionStorage storage) {
        this.storage = storage;
    }

    public void run() {
        System.out.println("Running tests for " + storage.getName());
        System.out.print("  testOne         -> "); testOne();         System.out.println("OK");
        System.out.print("  testDiag        -> "); testDiag();        System.out.println("OK");
        System.out.print("  testRemoveWorst -> "); testRemoveWorst(); System.out.println("OK");
        System.out.print("  testBulk        -> "); testBulk();        System.out.println("OK");
        System.out.print("  testQueries     -> "); testQueries();     System.out.println("OK");
        System.out.print("  testHyperVolume -> "); testHyperVolume(); System.out.println("OK");
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

    private static void expectE(double expected, double found) {
        if (Math.abs(expected - found) / Math.max(1, Math.abs(expected)) > 1e-14) {
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

    private void testRemoveWorst() {
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

    private void testBulk() {
        storage.clear();
        storage.addAll(
            s(0, 3), s(1, 1), s(3, 0),
            s(0, 5), s(1, 4), s(2, 1), s(5, 0),
            s(0, 7), s(3, 6), s(4, 5), s(6, 4), s(7, 2), s(9, 0),
            s(2, 8), s(4, 7), s(7, 6), s(8, 5), s(9, 3),
            s(4, 9), s(7, 8), s(8, 7), s(10, 6),
            s(6, 10), s(8, 9), s(10, 8),
            s(9, 11), s(10, 10),
            s(10, 11)
        );
        expect(28, storage.size());
        expect(s(10, 11), storage.removeWorst());
        storage.removeWorst(2);
        expect(25, storage.size());
        expect(s(8, 9), storage.removeWorst());
        storage.removeWorst(7);
        expect(17, storage.size());
        expect(s(4, 7), storage.removeWorst());
        expect(s(7, 6), storage.removeWorst());
        storage.removeWorst(8);
        expect(7, storage.size());
        expect(s(1, 4), storage.removeWorst());
        storage.removeWorst(3);
        expect(3, storage.size());
        expect(s(1, 1), storage.removeWorst());
        storage.removeWorst(2);
        expect(0, storage.size());
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

        Set<SolutionStorage.QueryResult> queries = new TreeSet<>(new Comparator<SolutionStorage.QueryResult>() {
            public int compare(SolutionStorage.QueryResult l, SolutionStorage.QueryResult r) {
                int cmpx = l.solution.compareX(r.solution);
                if (cmpx != 0) return cmpx;
                int cmpy = l.solution.compareY(r.solution);
                if (cmpy != 0) return cmpy;
                int cmpl = Integer.compare(l.layer, r.layer);
                if (cmpl != 0) return cmpl;
                if (Math.abs(l.crowdingDistance - r.crowdingDistance) > 1e-9) {
                    return Double.compare(l.crowdingDistance, r.crowdingDistance);
                }
                return 0;
            }
        });
        for (int i = 0; i < 120; ++i) {
            queries.add(storage.getRandom());
        }

        final double INF = Double.POSITIVE_INFINITY;
        expect(true, queries.remove(q(0, 0, INF,     0)));
        expect(true, queries.remove(q(2, 0, INF,     1)));
        expect(true, queries.remove(q(1, 1, 2,       1)));
        expect(true, queries.remove(q(0, 3, INF,     1)));
        expect(true, queries.remove(q(5, 1, INF,     2)));
        expect(true, queries.remove(q(2, 2, 1.3,     2)));
        expect(true, queries.remove(q(1, 3, 1.15,    2)));
        expect(true, queries.remove(q(0, 5, INF,     2)));
        expect(true, queries.remove(q(4, 3, INF,     3)));
        expect(true, queries.remove(q(3, 4, 4.0 / 3, 3)));
        expect(true, queries.remove(q(2, 5, 4.0 / 3, 3)));
        expect(true, queries.remove(q(1, 6, INF,     3)));
        expect(0, queries.size());
    }

    private void testHyperVolume() {
        storage.clear();
        storage.add(s(3, 3));
        expectE(1.0, storage.hyperVolume(0, 4, 0, 4) * 16);
        storage.add(s(2, 3));
        expectE(2.0, storage.hyperVolume(0, 4, 0, 4) * 16);
        storage.add(s(3, 2));
        expectE(3.0, storage.hyperVolume(0, 4, 0, 4) * 16);
        storage.add(s(1, 3));
        expectE(4.0, storage.hyperVolume(0, 4, 0, 4) * 16);
        storage.add(s(2, 1));
        expectE(7.0, storage.hyperVolume(0, 4, 0, 4) * 16);
        storage.add(s(1, 2));
        expectE(8.0, storage.hyperVolume(0, 4, 0, 4) * 16);
        storage.add(s(0, 0));
        expectE(16.0, storage.hyperVolume(0, 4, 0, 4) * 16);
    }

    public static void main(String[] args) {
        new SolutionStorageTests(new ru.ifmo.steady.inds.Storage()).run();
        new SolutionStorageTests(new ru.ifmo.steady.enlu.Storage()).run();
    }
}
