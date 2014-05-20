import java.util.*;

public class J2208365V {
    public void solve(List<Integer> ships, List<Integer> havens) {
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
                ship[i + 1] = items.get(i);
            }
            for (int i = 1; i <= m; ++i) {
                row[i] = sacks.get(i - 1);
                idrow[i] = i;
            }
            for (int i = 1; i <= m; ++i) {
                for (int j = i + 1; j <= m; ++j) {
                    if (row[i] < row[j]) {
                        int tmp;
                        tmp = row[i]; row[i] = row[j]; row[j] = tmp;
                        tmp = idrow[i]; idrow[i] = idrow[j]; idrow[j] = tmp;
                    }
                }
            }
            for (int i = 1; i <= n; ++i) {
                id[i] = i;
            }

            Random random = new Random(239);
            f[0][0] = true;
            while (true) {
                for (int i = 1; i <= n; ++i) {
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
            if (x == 0) {
                return true;
            }
            int tot = 0;
            int k = 0;
            for (int i = 1; i <= n; ++i) {
                if (belong[id[i]] == 0) {
                    ++tot;
                    left[tot] = id[i];
                    k += ship[id[i]];
                }
            }
            f[0][0] = true;
            for (int i = 1; i <= tot; ++i) {
                Arrays.fill(f[i], 0, k + 1, false);
            }
            k = 0;
            for (int i = 1; i <= tot; ++i) {
                for (int j = 0; j <= k; ++j) {
                    if (f[i - 1][j]) {
                        f[i][j] = true;
                        f[i][j + ship[left[i]]] = true;
                    }
                }
                k += ship[left[i]];
            }
            k = 0;
            for (int i = 1; i <= x; ++i) {
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
                if (!f[i - 1][k]) {
                    belong[left[i]] = x;
                    k -= ship[left[i]];
                }
            }

            if (dfs(x - 1)) {
                return true;
            }

            for (int i = 1; i <= n; ++i) {
                if (belong[i] == x) {
                    belong[i] = 0;
                }
            }
            return false;
        }
    }
}
