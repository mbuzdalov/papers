import java.util.*;
import java.math.BigInteger;
import static java.math.BigInteger.*;

public class Mastermind {
    private static BigInteger choose(int n, int k) {
        BigInteger rv = ONE;
        for (int i = 0; i < k; ++i) {
            rv = rv.multiply(BigInteger.valueOf(n - i));
            rv = rv.divide(BigInteger.valueOf(i + 1));
        }
        return rv;
    }

    abstract static class Config {
        abstract int[][] generateA(int n);
        abstract BigInteger[] generateB(int n);
    }

    static class SimpleConfig extends Config {
        int[][] generateA(int n) {
            return new int[][] { { n, 0 }, { n, 0 } };
        }
        BigInteger[] generateB(int n) {
            return new BigInteger[] { choose(n, 1).multiply(BigInteger.valueOf(n - 1).pow(n - 1)), BigInteger.valueOf(n).pow(n) };
        }
        public String toString() {
            return "Simple config";
        }
    }

    static class ComplexConfig extends Config {
        int[][] generateA(int n) {
            int[][] complexA = new int[n + 1][n + 1];
            for (int i = 0; i < n; ++i) {
                complexA[i][i] = n;
                for (int j = 0; j < i; ++j) {
                    if (!(i == n - 1 && j == n - 2)) {
                        complexA[i][j] = 1;
                        complexA[i][i] -= 1;
                    }
                }
                complexA[n][i] = 1;
            }
            return complexA;
        }
        BigInteger[] generateB(int n) {
            BigInteger[] complexB = new BigInteger[n + 1];
            for (int i = 0; i < n; ++i) {
                complexB[i] = choose(n, i + 1).multiply(BigInteger.valueOf(n - 1).pow(i + 1));
            }
            complexB[n] = BigInteger.valueOf(n).pow(n);
            return complexB;
        }
        public String toString() {
            return "Complex config";
        }
    }

    private static void masterMind(int n, List<Config> configs) {
        System.out.printf(
            "n = %5d: %20s: %12d%n",
             n, "simpleLower", n
        );
        for (Config config : configs) {
            System.out.printf("           %20s:", config.toString());
            int[][] a = config.generateA(n);
            BigInteger[] b = config.generateB(n);
            MatrixTheorem th = new MatrixTheorem();
            long t1 = System.nanoTime();
            double rv = th.averageDepth(a, b, BigInteger.valueOf(n).pow(n));
            long t2 = System.nanoTime();
            System.out.printf(" %12.6f (in %12.6f sec)%n", rv, (t2 - t1) / 1e9);
        }
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        List<Config> configs = Arrays.asList(
            new SimpleConfig(), new ComplexConfig()
        );

        for (int i = 100; i <= 2000; i += 100) {
            masterMind(i, configs);
        }
    }
}
