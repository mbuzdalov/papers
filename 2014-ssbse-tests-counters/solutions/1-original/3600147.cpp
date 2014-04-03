#include <functional>
#include <algorithm>
#include <iostream>
#include <sstream>
#include <complex>
#include <numeric>
#include <cstring>
#include <vector>
#include <string>
#include <cstdio>
#include <queue>
#include <cmath>
#include <map>
#include <set>
#define next			__next
#define all(a)			(a).begin(), (a).end()
#define sz(a)			int((a).size())
#define FOR(i, a, b)	for (int i(a); i < b; ++i)
#define REP(i, n)		FOR(i, 0, n)
#define UN(v)			sort(all(v)), (v).erase(unique((v).begin(), (v).end()), (v).end())
#define CL(a, b)		memset(a, b, sizeof a)
#define pb				push_back
#define X				first
#define Y				second
#define N	99
#define M	9
#define S	(N * 100)
using namespace std;
typedef long long ll;
typedef vector <int> vi;
typedef pair <int, int> pii;

namespace Random {
  unsigned next() {
    static unsigned x = 1987657173, y = 712356789, z = 531288629, w = 138751267;
    unsigned t = x ^ x << 11; x = y, y = z, z = w;
    return w = ((t ^ t >> 8) ^ w) ^ w >> 19;
  }
  template <class I> void shuffle(I begin, I end) {
    for (I i = begin; i != end; ++i)
      swap(*i, begin[next() % ((i - begin) + 1)]);
  }
};

class bitset {
  unsigned d[(S >> 5) + 1];
  int n;
public:
  void set(int i) {
    int m = i >> 5;
    for (; n <= m; d[n++] = 0);
    d[m] |= 1 << i;
  }
  void operator = (const bitset &b) {
    n = b.n;
    memcpy(d, b.d, n << 2);
  }
  bool operator [] (int i) const {
    return (i >> 5) < n && (d[i >> 5] & 1 << i) != 0;
  }
  void shiftor(const bitset &b, int sh, int m) {
    for (m >>= 5; n <= m; d[n++] = 0);
    unsigned *s = d + (sh >> 5), *e = d + n;
    if (s < e) {
      const unsigned *i = b.d;
      if (sh &= 31) {
        int rh = 32 - sh;
        e = min(e, s + b.n);
        for (; (e - s) & 3; ++s, ++i) {
          s[0] |= i[0] << sh;
          s[1] |= i[0] >> rh;
        }
        for (; s != e; ) {
          s++[0] |= i[0] << sh;
          s[0] |= i++[0] >> rh;
          s++[0] |= i[0] << sh;
          s[0] |= i++[0] >> rh;
          s++[0] |= i[0] << sh;
          s[0] |= i++[0] >> rh;
          s++[0] |= i[0] << sh;
          s[0] |= i++[0] >> rh;
        }
      } else {
        for (; (e - s) & 3; )
          s++[0] |= i++[0];
        for (; s != e; ) {
          s++[0] |= i++[0];
          s++[0] |= i++[0];
          s++[0] |= i++[0];
          s++[0] |= i++[0];
        }
      }
    }
  }
} f[N];

int a[N], row[M], p[N], q[M], v[101], *ve = v, n, m;
int first[M], next[N], u[M];
int same, same_cnt;
unsigned hs[N], hsh;
set<unsigned> U;

bool fit(int k) {
  int h = row[k];
  if (same * same_cnt >= h && h % same == 0) {
    u[k] = h / same;
    same_cnt -= u[k];
    return true;
  }
  int mk = min(h / same, same_cnt);
  for (int *i = v, s = 0; i != ve; ++i) {
    bitset &F = f[(i - v) + 1];
    F = f[(i - v)];
    s = min(s + a[*i], h);
    F.shiftor(f[(i - v)], a[*i], s);
    for (int l = mk; l >= 0 ; --l)
      if (h - l * same >= 0 && F[h - l * same]) {
        h -= l * same;
        for (int *j = i; h; --j)
          if (!f[j - v][h]) {
            p[*j] = k; h -= a[*j];
          }
        for (; i >= v; --i)
          if (~p[*i]) {
            hsh ^= hs[*i];
            next[*i] = first[k];
            first[k] = *i;
            *i = *--ve;
          }
        u[k] = l;
        same_cnt -= l;
        return true;
      }
  }
  return false;
}

void cancel(int k) {
  int *s = ve;
  for (int i = first[k]; ~i; i = next[i]) {
    p[*ve++ = i] = -1;
    hsh ^= hs[i];
  }
  first[k] = -1;
  same_cnt += u[k];
  u[k] = 0;
}

bool solve(int k) {
  if (k == 1) {
    int i = row[q[1]] > row[q[0]];
    if (fit(q[i])) {
      swap(q[i], q[k]);
      return true;
    }
    return false;
  }
  if (hsh && k < 4) {
    if (U.count(hsh))
      return false;
    U.insert(hsh);
  }
  REP (i, k)
    if (fit(q[i])) {
      if (k & 1) {
        reverse(q + i, q + k + 1);
        if (solve(k - 1))
          return true;
        reverse(q + i, q + k + 1);
      } else {
        swap(q[i], q[k]);
        if (solve(k - 1))
          return true;
        swap(q[i], q[k]);
      }
      cancel(q[i]);
    } else break;
  return false;
}

int main() {
  f[0].set(0);
  scanf("%d%d", &n, &m);
  CL(first, -1);
  REP (i, n) {
    hs[i] = Random::next();
    scanf("%d", a + i);
    p[i] = -1;
    v[a[i]]++;
  }
  REP (i, m)
    scanf("%d", row + i), q[i] = i;
  same = max_element(v, v + 101) - v;
  same_cnt = v[same];
  REP (i, n)
    if (a[i] != same)
      *ve++ = i;

  for (; ; ) {
    Random::shuffle(v, ve);
    Random::shuffle(q, q + m);
    if (solve(m - 1)) {
      iterationsLength += lastIteration;
      break;
    }
  }
  fit(*q);
  REP (i, m) {
    vi ships(u[i], same);
    for (int j = first[i]; ~j; j = next[j])
      ships.pb(a[j]);
    printf("%d\n", sz(ships));
    REP (j, sz(ships)) {
      if (j)
        putchar(' ');
      printf("%d", ships[j]);
    }
    puts("");
  }
  return 0;
}
