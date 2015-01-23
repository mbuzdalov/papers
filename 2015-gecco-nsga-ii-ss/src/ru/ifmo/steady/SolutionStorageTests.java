package ru.ifmo.steady;

public class SolutionStorageTests {
    private final SolutionStorage storage;
    private final String name;

    public SolutionStorageTests(SolutionStorage storage, String name) {
        this.storage = storage;
        this.name = name;
    }

    public void run() {
        System.out.print(name + ": testOne      -> "); testOne();      System.out.println("OK");
        System.out.print(name + ": testDiag     -> "); testDiag();     System.out.println("OK");
        System.out.print(name + ": testCrowding -> "); testCrowding(); System.out.println("OK");
    }

    private static Solution s(double x, double y) {
        return new Solution(x, y);
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

    public static void main(String[] args) {
        new SolutionStorageTests(new ru.ifmo.steady.inds.Storage(), "INDS").run();
    }
}
