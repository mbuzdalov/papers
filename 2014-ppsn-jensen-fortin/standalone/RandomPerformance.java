import java.util.*;

public class RandomPerformance {
    private static void randomCube(int n, int dim, int times, boolean silent) {
        double[][] points = new double[n][dim];
        int[] result = new int[n];
        FasterNonDominatedSorting.Sorter sorter = FasterNonDominatedSorting.getSorter(n, dim);

        long[] nanos = new long[times];
        Random random = new Random();
        System.gc();
        System.gc();
        for (int attempt = 0; attempt < times; ++attempt) {
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < dim; ++j) {
                    points[i][j] = random.nextDouble();
                }
            }
            long t0 = System.nanoTime();
            sorter.sort(points, result);
            nanos[attempt] = System.nanoTime() - t0;
        }
        if (!silent) {
            Arrays.sort(nanos);
            double sum = 0;
            for (long nano : nanos) sum += nano;
            double median = (nanos[times / 2] + nanos[times - 1 - times / 2]) / 2.0;
            System.out.printf("    n = %d, dim = %2d, times = %d: average %.2e, min %.2e, max %.2e, median %.2e%n",
                n, dim, times, sum / times / 1e6, nanos[0] / 1e6, nanos[times - 1] / 1e6, median / 1e6);
        }
    }

    public static void main(String[] args) {
        System.out.println("randomCube:");
        for (int i = 1; i <= 100; ++i) {
            randomCube(i, i / 10, 100, true);
        }
        System.out.println("    warmed up");
        for (int n : new int[] { 100, 1000, 10000, 100000 }) {
            for (int d = 2; d <= 10; ++d) {
                randomCube(n, d, 11, false);
            }
            System.out.println("    ------------------------------------");
        }
    }
}
