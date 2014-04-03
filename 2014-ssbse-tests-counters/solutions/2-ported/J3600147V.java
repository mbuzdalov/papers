import java.io.*;
import java.util.*;

public class J3600147V {
    final static int N = 99;
    final static int M = 9;
    final static int S = N * 100;
    final static int L = 2000;

	static void swap(int[] array, int i1, int i2) {
		int tmp = array[i1];
		array[i1] = array[i2];
		array[i2] = tmp;
	}
    static class RNG {
        int x = 1987657173, y = 712356789, z = 531288629, w = 138751267;

        int next() {
            int t = x ^ x << 11;
            x = y;
            y = z;
            z = w;
            w = ((t ^ t >>> 8) ^ w) ^ w >>> 19;
			return w;
        }

        void shuffle(int[] array, int from, int to) {
            for (int i = from; i < to; ++i) {
                int idx = from + (int) ((next() & ((1L << 32) - 1)) % (i - from + 1));
				swap(array, i, idx);
            }
        }
    }

    static class Bitset {
        int[] d = new int[(S >> 5) + 1];
        int n = 0;

        void set(int i) {
            int m = i >> 5;
            for (; n <= m; d[n++] = 0) {
            }
            d[m] |= 1 << i;
        }
        void assignFrom(Bitset b) {
            this.n = b.n;
            System.arraycopy(b.d, 0, d, 0, d.length);
        }
        boolean get(int i) {
            return (i >> 5) < n && (d[i >> 5] & 1 << i) != 0;
        }
        void shiftOr(Bitset b, int sh, int m) {
            for (m >>= 5; n <= m; d[n++] = 0) {
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
                        d[id_s] |= i[ii_i] << sh;
                        d[id_s + 1] |= i[ii_i] >>> rh;
                    }
                    for (; id_e != id_s; ) {
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
                        d[id_s++] |= i[ii_i++];
                    }
                    for (; id_e != id_s; ) {
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
    int[] v = new int[101];
    int ve;
    int n, m;
    int[] first = new int[M];
    int[] next = new int[N];
    int[] u = new int[M];
    int same, same_cnt;
    int[] hs = new int[N];
    int hsh;
    TreeSet<Integer> U = new TreeSet<>();

    boolean fit(int k) {
        int h = row[k];
        if (same * same_cnt >= h && h % same == 0) {
            u[k] = h / same;
            same_cnt -= u[k];
            return true;
        }
        int mk = Math.min(h / same, same_cnt);
        for (int i = 0, s = 0; i != ve; ++i) {
            Bitset F = f[i + 1];
            F.assignFrom(f[i]);
            s = Math.min(s + a[v[i]], h);
            F.shiftOr(f[i], a[v[i]], s);
            for (int l = mk; l >= 0; --l) {
                if (h - l * same >= 0 && F.get(h - l * same)) {
                    h -= l * same;
                    for (int j = i; h != 0; --j) {
                        if (!f[j].get(h)) {
                            p[v[j]] = k;
                            h -= a[v[j]];
                        }
                    }
                    for (; i >= 0; --i) {
                        if ((~p[v[i]]) != 0) {
                            hsh ^= hs[v[i]];
                            next[v[i]] = first[k];
                            first[k] = v[i];
                            v[i] = v[--ve];
                        }
                    }
                    u[k] = l;
                    same_cnt -= l;
                    return true;
                }
            }
        }
        return false;
    }

    void reverse(int[] a, int from, int to) {
        --to;
        int tmp;
        while (from < to) {
			swap(a, from++, to--);
        }
    }

    void cancel(int k) {
        int s = ve;
        for (int i = first[k]; (~i) != 0; i = next[i]) {
            p[v[ve++] = i] = -1;
            hsh ^= hs[i];
        }
        first[k] = -1;
        same_cnt += u[k];
        u[k] = 0;
    }

    boolean solve(int k) {
        if (k == 1) {
            int i = row[q[1]] > row[q[0]] ? 1 : 0;
            if (fit(q[i])) {
				swap(q, i, k);
                return true;
            }
            return false;
        }
        if (hsh != 0 && k < 4) {
            if (U.contains(hsh)) {
                return false;
            }
            U.add(hsh);
        }
        for (int i = 0; i < k; ++i) {
            if (fit(q[i])) {
                if ((k & 1) != 0) {
                    reverse(q, i, k + 1);
                    if (solve(k - 1)) {
                        return true;
                    }
                    reverse(q, i, k + 1);
                } else {
					swap(q, i, k);
                    if (solve(k - 1)) {
                        return true;
                    }
					swap(q, i, k);
                }
                cancel(q[i]);
            } else {
                break;
            }
        }
        return false;
    }

    public void solve(List<Integer> ships, List<Integer> havens) {
        RNG random = new RNG();
        for (int i = 0; i < N; ++i) {
            f[i] = new Bitset();
        }
        f[0].set(0);
        n = ships.size();
        m = havens.size();
        Arrays.fill(first, -1);
        for (int i = 0; i < n; ++i) {
            hs[i] = random.next();
            a[i] = ships.get(i);
            p[i] = -1;
            v[a[i]]++;
        }
        for (int i = 0; i < m; ++i) {
            row[i] = havens.get(i);
            q[i] = i;
        }
        same = 0;
        for (int me = v[0], i = 0; i < v.length; ++i) {
            if (v[i] > me) {
                me = v[i];
                same = i;
            }
        }
        same_cnt = v[same];
		ve = 0;
        for (int i = 0; i < n; ++i) {
            if (a[i] != same) {
                v[ve++] = i;
            }
        }
        while (true) {
            random.shuffle(v, 0, ve);
            random.shuffle(q, 0, m);
            if (solve(m - 1)) {
                break;
            }
        }
        fit(q[0]);
    }
}
