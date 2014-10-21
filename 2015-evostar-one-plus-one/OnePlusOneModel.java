import java.io.*;
import java.util.*;

public class OnePlusOneModel {
    public static void main(String[] args) throws IOException {
        for (int N : new int[] {10, 30, 100, 300, 1000, 3000, 10000, 30000, 100000}) {
            for (double gamma : new double[] { 1.0 / N, 1.0 }) {
                OnePlusOne config = new OnePlusOne(N, gamma);
                double sum = 0;
                double sumSq = 0;

                double falseSum = 0;

                int runs = 100;
                int point = 10;
                System.out.print("[");
                for (int t = 0; t < runs; ++t) {
                    double v = config.run();
                    sum += v;
                    sumSq += v * v;
                    if ((t + 1) % point == 0) {
                        System.out.print(".");
                    }
                    falseSum += config.falseQueries;
                }
                System.out.println("]");

                double avg = sum / runs;
                double dev = Math.sqrt(sumSq / runs - avg * avg);

                System.out.printf(
                    "N: %d, gamma: %f: avg = %f, 2e N log N = %f, 2.5e N log N = %f, dev = %f, fq = %f\n",
                    N, gamma, avg,
                    2 * Math.E * N * Math.log(N),
                    2.5 * Math.E * N * Math.log(N),
                    dev, falseSum / runs
                );
            }
        }
    }

    static class OnePlusOne {
        private final int n;
        private final double gamma;
        private final double log1n;

        public OnePlusOne(int n, double gamma) {
            this.n = n;
            this.gamma = gamma;
            this.log1n = Math.log(1 - 1.0 / n);
        }

        /**
         * Returns the number of a bit that is next to flip.
         * The value will always be greater than zero.
         */
        public int nextOffset() {
            double r01 = r().nextDouble();
            return 1 + (int) (Math.log(r01) / log1n);
        }

        int falseQueries;

        public int run() {
            falseQueries = 0;
            // Initialize all the variables
            BitSet x = new BitSet(n);
            BitSet t = new BitSet(n);
            double[][] q = new double[n + 1][2];
            int xf = 0;
            int count = 1;
            // Main loop
            while (xf < n) {
                // Mutation
                t.clear();
                t.xor(x);
                for (int i = nextOffset() - 1; i < n; i += nextOffset()) {
                    t.flip(i);
                }
                // Mutant fitness computation
                int tf = t.cardinality();
                ++count;
                // Choosing whether OneMax or ZeroMax is used
                if (q[xf][0] > q[xf][1]) ++falseQueries;
                boolean use0 = q[xf][0] > q[xf][1] || q[xf][0] == q[xf][1] && r().nextBoolean();
                int idx = use0 ? 0 : 1;
                // If there is an update for the chosen function...
                if (use0 && tf <= xf || !use0 && tf >= xf) {
                    // Replace the parent with the mutant
                    BitSet tmp = x;
                    x = t;
                    t = tmp;
                    // Recompute the Q values
                    q[xf][idx] /= 2;
                    q[xf][idx] += 0.5 * ((tf - xf) + gamma * Math.max(q[tf][0], q[tf][1]));
                    xf = tf;
                } else {
                    // Recompute the Q values
                    q[xf][idx] /= 2;
                    q[xf][idx] += 0.5 * (gamma * Math.max(q[xf][0], q[xf][1]));
                }
            }
            return count;
        }
    }

    static FastRandom r() {
        return FastRandom.THREAD_LOCAL.get();
    }

    /**
     * CMWC-4096 random generator.
     */
    static class FastRandom extends Random {
        private static final ThreadLocal<FastRandom> THREAD_LOCAL = new ThreadLocal<FastRandom>() {
            @Override
            protected FastRandom initialValue() {
                return new FastRandom();
            }
        };

        private static final long multiplier = 0x5DEECE66DL;
        private static final long addend = 0xBL;
        private static final long mask = (1L << 48) - 1;

        private static final int Q_SIZE = 4096;
        private static final long a = 18782;
        private static final int r = 0xfffffffe;

        private int[] Q;
        private int c;
        private int idx;

        public FastRandom() {
            super();
        }

        public FastRandom(long seed) {
            super(seed);
        }

        private long nextSeed(long seed) {
            return (seed & mask) * multiplier + addend + (seed >>> 47);
        }

        public final void setSeed(long seed) {
            super.setSeed(seed);
            if (Q == null) {
                Q = new int[Q_SIZE];
            }
            seed = nextSeed(seed);
            c = ((int) (seed >>> 16)) % (809430660);
            seed = nextSeed(seed);
            for (int i = 0; i < Q_SIZE; ++i) {
                Q[i] = (int) (seed >>> 16);
                seed = nextSeed(seed);
            }
            this.idx = 0;
        }

        @Override
        protected final int next(int nBits) {
            idx = (idx + 1) & (Q_SIZE - 1);
            long t = a * Q[idx] + c;
            c = (int) (t >>> 32);
            int x = (int) t + c;
            if (x < c) {
                x++;
                c++;
            }
            int rv = Q[idx] = r - x;
            return rv >>> (32 - nBits);
        }
    }
}
