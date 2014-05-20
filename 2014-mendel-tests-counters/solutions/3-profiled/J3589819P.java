import java.io.*;
import java.util.*;

import timus1394.TimeoutChecker;
public class J3589819P {
    private static long counter$0 = 0;
    private static long counter$1 = 0;
    private static long counter$2 = 0;
    private static long counter$3 = 0;
    private static long counter$4 = 0;
    private static long counter$5 = 0;
    private static long counter$6 = 0;
    private static long counter$7 = 0;
    private static long counter$8 = 0;
    private static long counter$9 = 0;
    private static long counter$10 = 0;
    private static long counter$11 = 0;
    private static long counter$12 = 0;
    private static long counter$13 = 0;
    private static long counter$14 = 0;
    private static long counter$15 = 0;
    private static long counter$16 = 0;
    private static long counter$17 = 0;
    private static long counter$18 = 0;
    private static long counter$19 = 0;
    private static long counter$20 = 0;
    private static long counter$21 = 0;
    private static long counter$22 = 0;
    private static long counter$23 = 0;
    private static long counter$24 = 0;
    private static long counter$25 = 0;
    private static long counter$26 = 0;
    private static long counter$27 = 0;
    private static long counter$28 = 0;
    public static void profilerCleanup() {
        counter$0 = 0;
        counter$1 = 0;
        counter$2 = 0;
        counter$3 = 0;
        counter$4 = 0;
        counter$5 = 0;
        counter$6 = 0;
        counter$7 = 0;
        counter$8 = 0;
        counter$9 = 0;
        counter$10 = 0;
        counter$11 = 0;
        counter$12 = 0;
        counter$13 = 0;
        counter$14 = 0;
        counter$15 = 0;
        counter$16 = 0;
        counter$17 = 0;
        counter$18 = 0;
        counter$19 = 0;
        counter$20 = 0;
        counter$21 = 0;
        counter$22 = 0;
        counter$23 = 0;
        counter$24 = 0;
        counter$25 = 0;
        counter$26 = 0;
        counter$27 = 0;
        counter$28 = 0;
    }
    public static java.util.Map<String, Long> profilerData() {
        java.util.Map<String, Long> rv = new java.util.HashMap<>();
        rv.put("counter$0", counter$0);
        rv.put("counter$1", counter$1);
        rv.put("counter$2", counter$2);
        rv.put("counter$3", counter$3);
        rv.put("counter$4", counter$4);
        rv.put("counter$5", counter$5);
        rv.put("counter$6", counter$6);
        rv.put("counter$7", counter$7);
        rv.put("counter$8", counter$8);
        rv.put("counter$9", counter$9);
        rv.put("counter$10", counter$10);
        rv.put("counter$11", counter$11);
        rv.put("counter$12", counter$12);
        rv.put("counter$13", counter$13);
        rv.put("counter$14", counter$14);
        rv.put("counter$15", counter$15);
        rv.put("counter$16", counter$16);
        rv.put("counter$17", counter$17);
        rv.put("counter$18", counter$18);
        rv.put("counter$19", counter$19);
        rv.put("counter$20", counter$20);
        rv.put("counter$21", counter$21);
        rv.put("counter$22", counter$22);
        rv.put("counter$23", counter$23);
        rv.put("counter$24", counter$24);
        rv.put("counter$25", counter$25);
        rv.put("counter$26", counter$26);
        rv.put("counter$27", counter$27);
        rv.put("counter$28", counter$28);
        return rv;
    }
	static void swap(int[] array, int i1, int i2) {
if ((++counter$0 & 262143) == 0) TimeoutChecker.check();
		int tmp = array[i1];
		array[i1] = array[i2];
		array[i2] = tmp;
	}
    static class RNG {
        int x = 1987657173, y = 712356789, z = 531288629, w = 138751267;

        int next() {
if ((++counter$1 & 262143) == 0) TimeoutChecker.check();
            int t = x ^ x << 11;
            x = y;
            y = z;
            z = w;
            w = ((t ^ t >>> 8) ^ w) ^ w >>> 19;
			return w;
        }

        void shuffle(int[] array, int from, int to) {
if ((++counter$2 & 262143) == 0) TimeoutChecker.check();
            for (int i = from; i < to; ++i) {
if ((++counter$3 & 262143) == 0) TimeoutChecker.check();
                int idx = from + (int) ((next() & ((1L << 32) - 1)) % (i - from + 1));
				swap(array, i, idx);
            }
        }
    }

    final static int N = 99;
    final static int M = 9;
    final static int S = N * 100;

    class Bitset {
        int[] d = new int[(S >> 5) + 1];
        int n = 0;

        void set(int i) {
if ((++counter$4 & 262143) == 0) TimeoutChecker.check();
            int m = i >> 5;
            for (; n <= m; d[n++] = 0) {
if ((++counter$5 & 262143) == 0) TimeoutChecker.check();
            }
            d[m] |= 1 << i;
        }
        void assignFrom(Bitset b) {
if ((++counter$6 & 262143) == 0) TimeoutChecker.check();
            this.n = b.n;
            System.arraycopy(b.d, 0, d, 0, d.length);
        }
        boolean get(int i) {
if ((++counter$7 & 262143) == 0) TimeoutChecker.check();
            return (i >> 5) < n && (d[i >> 5] & 1 << i) != 0;
        }
        void shiftOr(Bitset b, int sh, int m) {
if ((++counter$8 & 262143) == 0) TimeoutChecker.check();
            for (m >>= 5; n <= m; d[n++] = 0) {
if ((++counter$9 & 262143) == 0) TimeoutChecker.check();
            }
            int id_s = sh >> 5;
            int id_e = n;
            if (id_s < id_e) {
                int[] i = b.d;
                int ii_i = 0;
                if ((sh &= 31) != 0) {
                    int rh = 32 - sh;
                    id_e = Math.min(id_e, id_s + b.n);
                    for (; ((id_e - id_s) & 3) != 0; ++id_s, ++ii_i) {
if ((++counter$10 & 262143) == 0) TimeoutChecker.check();
                        d[id_s] |= i[ii_i] << sh;
                        d[id_s + 1] |= i[ii_i] >>> rh;
                    }
                    for (; id_e != id_s; ) {
if ((++counter$11 & 262143) == 0) TimeoutChecker.check();
                        d[id_s++] |= i[ii_i] << sh;
                        d[id_s] |= i[ii_i++] >>> rh;
                        d[id_s++] |= i[ii_i] << sh;
                        d[id_s] |= i[ii_i++] >>> rh;
                        d[id_s++] |= i[ii_i] << sh;
                        d[id_s] |= i[ii_i++] >>> rh;
                        d[id_s++] |= i[ii_i] << sh;
                        d[id_s] |= i[ii_i++] >>> rh;
                    }
                } else {
                    for (; ((id_e - id_s) & 3) != 0; ) {
if ((++counter$12 & 262143) == 0) TimeoutChecker.check();
                        d[id_s++] |= i[ii_i++];
                    }
                    for (; id_e != id_s; ) {
if ((++counter$13 & 262143) == 0) TimeoutChecker.check();
                        d[id_s++] |= i[ii_i++];
                        d[id_s++] |= i[ii_i++];
                        d[id_s++] |= i[ii_i++];
                        d[id_s++] |= i[ii_i++];
                    }
                }
            }
        }
    }

    Bitset[] f = new Bitset[N];
    int[] a = new int[N];
    int[] row = new int[M];
    int[] p = new int[N];
    int[] q = new int[M];
    int[] v = new int[N];
    int ve;
    int n, m;
    int[] first = new int[M];
    int[] next = new int[N];

    boolean fit(int k) {
if ((++counter$14 & 262143) == 0) TimeoutChecker.check();
        int h = row[k];
        for (int i = 0, s = 0; i != ve; ++i) {
if ((++counter$15 & 262143) == 0) TimeoutChecker.check();
            Bitset F = f[i + 1];
            F.assignFrom(f[i]);
            s = Math.min(s + a[v[i]], h);
            F.shiftOr(f[i], a[v[i]], s);
            if (F.get(h)) {
                for (int j = i; h != 0; --j) {
if ((++counter$16 & 262143) == 0) TimeoutChecker.check();
                    if (!f[j].get(h)) {
                        p[v[j]] = k;
                        h -= a[v[j]];
                    }
                }
                for (; i >= 0; --i) {
if ((++counter$17 & 262143) == 0) TimeoutChecker.check();
                    if ((~p[v[i]]) != 0) {
                        next[v[i]] = first[k];
                        first[k] = v[i];
                        v[i] = v[--ve];
                    }
                }
                return true;
            }
        }
        return false;
    }

    void cancel(int k) {
if ((++counter$18 & 262143) == 0) TimeoutChecker.check();
        for (int i = first[k]; (~i) != 0; i = next[i]) {
if ((++counter$19 & 262143) == 0) TimeoutChecker.check();
            p[v[ve++] = i] = -1;
        }
        first[k] = -1;
    }

    boolean solve() {
if ((++counter$20 & 262143) == 0) TimeoutChecker.check();
        if (m == 1) {
            return true;
        }
        --m;
        for (int i = 0; i < m; ++i) {
if ((++counter$21 & 262143) == 0) TimeoutChecker.check();
            if (fit(q[i])) {
                for (int j = i; j < m; ++j) {
if ((++counter$22 & 262143) == 0) TimeoutChecker.check();
                    swap(q, j, j + 1);
                }
                if (solve()) {
                    ++m;
                    return true;
                }
                for (int j = m; j > i; --j) {
if ((++counter$23 & 262143) == 0) TimeoutChecker.check();
                    swap(q, j - 1, j);
                }
                cancel(q[i]);
            }
        }
        ++m;
        return false;
    }

    public void solve(List<Integer> ships, List<Integer> havens) {
if ((++counter$24 & 262143) == 0) TimeoutChecker.check();
        RNG random = new RNG();
        for (int i = 0; i < N; ++i) {
if ((++counter$25 & 262143) == 0) TimeoutChecker.check();
            f[i] = new Bitset();
        }
        f[0].set(0);
        n = ships.size();
        m = havens.size();
        Arrays.fill(first, -1);
        for (int i = 0; i < n; ++i) {
if ((++counter$26 & 262143) == 0) TimeoutChecker.check();
            a[i] = ships.get(i);
            p[i] = -1;
            v[ve++] = i;
        }
        for (int i = 0; i < m; ++i) {
if ((++counter$27 & 262143) == 0) TimeoutChecker.check();
            row[i] = havens.get(i);
            q[i] = i;
        }
        while (true) {
if ((++counter$28 & 262143) == 0) TimeoutChecker.check();
            random.shuffle(v, 0, n);
            random.shuffle(q, 0, m);
            if (solve()) {
                break;
            }
        }
        fit(q[0]);
    }
}
