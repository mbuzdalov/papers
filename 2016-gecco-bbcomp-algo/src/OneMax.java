import java.util.*;
import java.math.BigInteger;
import static java.math.BigInteger.*;

public class OneMax {
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
            return new int[][] { { (n + 1) / 2, 0 }, { n, 0 } };
        }
        BigInteger[] generateB(int n) {
            return new BigInteger[] { choose(n, n / 2), ONE.shiftLeft(n) };
        }
        public String toString() {
            return "Simple config";
        }
    }

    static class ComplexConfig1 extends Config {
        int[][] generateA(int n) {
            int[][] complexA;
            if (n % 2 == 1) {
                int root = n / 2 + 1;
                complexA = new int[root + 1][root + 1];
                for (int i = 0; i < root; ++i) {
                    complexA[i][i] = root;
                    for (int j = 0; j < i; ++j) {
                        complexA[i][j] = 1;
                        complexA[i][i] -= complexA[i][j];
                    }
                    complexA[root][i] = i == 0 ? 1 : 2;
                }
            } else {
                int root = n / 2;
                complexA = new int[root + 1][root + 1];
                for (int i = 0; i < root; ++i) {
                    complexA[i][i] = root;
                    for (int j = 0; j < i; ++j) {
                        complexA[i][j] = 1;
                        complexA[i][i] -= complexA[i][j];
                    }
                    complexA[root][i] = 2;
                }
            }
            return complexA;
        }
        BigInteger[] generateB(int n) {
            BigInteger[] complexB;
            if (n % 2 == 1) {
                int root = n / 2 + 1;
                complexB = new BigInteger[root + 1];
                complexB[root] = ONE.shiftLeft(n);
                for (int i = 0; i < root; ++i) {
                    complexB[i] = choose(n, i);
                }
            } else {
                int root = n / 2;
                complexB = new BigInteger[root + 1];
                complexB[root] = ONE.shiftLeft(n);
                for (int i = 0; i < root; ++i) {
                    complexB[i] = choose(n, i + 1);
                }
            }
            return complexB;
        }
        public String toString() {
            return "Complex config 1";
        }
    }

    static class ComplexConfig2 extends Config {
        int[][] generateA(int n) {
            int[][] complexA;
            if (n % 2 == 1) {
                int root = n / 2 + 1;
                complexA = new int[root + 1][root + 1];
                for (int i = 0; i < root; ++i) {
                    complexA[i][i] = root;
                    for (int j = 0; j < i; ++j) {
                        complexA[i][j] = 1;
                        complexA[i][i] -= complexA[i][j];
                    }
                    if (i > 0) {
                        ++complexA[i][i - 1];
                        --complexA[i][i];
                    }
                    complexA[root][i] = i == 0 ? 1 : 2;
                }
            } else {
                int root = n / 2;
                complexA = new int[root + 1][root + 1];
                for (int i = 0; i < root; ++i) {
                    complexA[i][i] = root;
                    for (int j = 0; j < i; ++j) {
                        complexA[i][j] = 1;
                        complexA[i][i] -= complexA[i][j];
                    }
                    if (i > 0) {
                        ++complexA[i][i - 1];
                        --complexA[i][i];
                    }
                    complexA[root][i] = 2;
                }
            }
            return complexA;
        }
        BigInteger[] generateB(int n) {
            BigInteger[] complexB;
            if (n % 2 == 1) {
                int root = n / 2 + 1;
                complexB = new BigInteger[root + 1];
                complexB[root] = ONE.shiftLeft(n);
                for (int i = 0; i < root; ++i) {
                    complexB[i] = choose(n, i);
                }
            } else {
                int root = n / 2;
                complexB = new BigInteger[root + 1];
                complexB[root] = ONE.shiftLeft(n);
                for (int i = 0; i < root; ++i) {
                    complexB[i] = choose(n, i + 1);
                }
            }
            return complexB;
        }
        public String toString() {
            return "Complex config 2";
        }
    }

    private static void oneMax(int n, List<Config> configs) {
        double simpleLower = n == 1 ? 0 : Math.floor((Math.log(n - 1) + n * Math.log(2)) / Math.log(n)) - 1.0 / (n - 1);
        double simpleUpper = 2 * n / Math.log(n) * Math.log(2);
        System.out.printf(
            "n = %d: simpleLower: %.6f, simpleUpper: %.6f%n",
             n, simpleLower, simpleUpper
        );
        for (Config config : configs) {
            System.out.printf("    %20s:", config.toString());
            int[][] a = config.generateA(n);
            BigInteger[] b = config.generateB(n);
            MatrixTheorem th = new MatrixTheorem();
            long t1 = System.nanoTime();
            double rv = th.averageDepth(a, b, ONE.shiftLeft(n));
            long t2 = System.nanoTime();
            System.out.printf("%12.6f (in %12.6f sec)%n", rv, (t2 - t1) / 1e9);
        }
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        List<Config> configs = Arrays.asList(
            new SimpleConfig(), /*new ComplexConfig1(),*/ new ComplexConfig2()
        );

        for (int i = 100; i < 1000; i += 100) {
            oneMax(i, configs);
            oneMax(i + 1, configs);
        }
        for (int i = 1000; i <= 5000; i += 1000) {
            oneMax(i, configs);
            oneMax(i + 1, configs);
        }
    }
}
