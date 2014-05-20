import java.util.*;

import util.TimeoutChecker;
public class J2208365P {
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
        return rv;
    }
    public void solve(List<Integer> ships, List<Integer> havens) {
if ((++counter$0 & 262143) == 0) TimeoutChecker.check();
        new Implementation(ships, havens);
    }

    private static class Implementation {
        private int n;
        private int m;
        private static final int maxM = 12;
        private static final int maxN = 105;
        private static final int maxS = 10000;
        private final boolean[][] f = new boolean[maxN + 1][maxS + 1];
        private final int[] left = new int[maxN + 1];
        private final int[] row = new int[maxM + 1];
        private final int[] idrow = new int[maxM + 1];
        private final int[] ship = new int[maxN + 1];
        private final int[] id = new int[maxN + 1];
        private final int[] belong = new int[maxN + 1];

        public Implementation(List<Integer> items, List<Integer> sacks) {
            n = items.size();
            m = sacks.size();
            for (int i = 0; i < n; ++i) {
if ((++counter$1 & 262143) == 0) TimeoutChecker.check();
                ship[i + 1] = items.get(i);
            }
            for (int i = 1; i <= m; ++i) {
if ((++counter$2 & 262143) == 0) TimeoutChecker.check();
                row[i] = sacks.get(i - 1);
                idrow[i] = i;
            }
            for (int i = 1; i <= m; ++i) {
if ((++counter$3 & 262143) == 0) TimeoutChecker.check();
                for (int j = i + 1; j <= m; ++j) {
if ((++counter$4 & 262143) == 0) TimeoutChecker.check();
                    if (row[i] < row[j]) {
                        int tmp;
                        tmp = row[i]; row[i] = row[j]; row[j] = tmp;
                        tmp = idrow[i]; idrow[i] = idrow[j]; idrow[j] = tmp;
                    }
                }
            }
            for (int i = 1; i <= n; ++i) {
if ((++counter$5 & 262143) == 0) TimeoutChecker.check();
                id[i] = i;
            }

            Random random = new Random(239);
            f[0][0] = true;
            while (true) {
if ((++counter$6 & 262143) == 0) TimeoutChecker.check();
                for (int i = 1; i <= n; ++i) {
if ((++counter$7 & 262143) == 0) TimeoutChecker.check();
                    int j = random.nextInt(n) + 1;
                    int k = random.nextInt(n) + 1;
                    int tmp = id[j]; id[j] = id[k]; id[k] = tmp;
                }
                if (dfs(m)) {
                    return;
                }
            }
        }

        boolean dfs(int x) {
if ((++counter$8 & 262143) == 0) TimeoutChecker.check();
            if (x == 0) {
                return true;
            }
            int tot = 0;
            int k = 0;
            for (int i = 1; i <= n; ++i) {
if ((++counter$9 & 262143) == 0) TimeoutChecker.check();
                if (belong[id[i]] == 0) {
                    ++tot;
                    left[tot] = id[i];
                    k += ship[id[i]];
                }
            }
            f[0][0] = true;
            for (int i = 1; i <= tot; ++i) {
if ((++counter$10 & 262143) == 0) TimeoutChecker.check();
                Arrays.fill(f[i], 0, k + 1, false);
            }
            k = 0;
            for (int i = 1; i <= tot; ++i) {
if ((++counter$11 & 262143) == 0) TimeoutChecker.check();
                for (int j = 0; j <= k; ++j) {
if ((++counter$12 & 262143) == 0) TimeoutChecker.check();
                    if (f[i - 1][j]) {
                        f[i][j] = true;
                        f[i][j + ship[left[i]]] = true;
                    }
                }
                k += ship[left[i]];
            }
            k = 0;
            for (int i = 1; i <= x; ++i) {
if ((++counter$13 & 262143) == 0) TimeoutChecker.check();
                if (!f[tot][row[i]]) {
                    return false;
                }
                if (!f[tot - 1][row[i]]) {
                    ++k;
                }
            }
            if (k > 1) {
                return false;
            }
            k = row[x];
            for (int i = tot; i >= 1; --i) {
if ((++counter$14 & 262143) == 0) TimeoutChecker.check();
                if (!f[i - 1][k]) {
                    belong[left[i]] = x;
                    k -= ship[left[i]];
                }
            }

            if (dfs(x - 1)) {
                return true;
            }

            for (int i = 1; i <= n; ++i) {
if ((++counter$15 & 262143) == 0) TimeoutChecker.check();
                if (belong[i] == x) {
                    belong[i] = 0;
                }
            }
            return false;
        }
    }
}
