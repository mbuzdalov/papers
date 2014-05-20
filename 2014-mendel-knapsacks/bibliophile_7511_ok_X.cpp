#include <stdio.h>
#include <math.h>
#include <string.h>
#include <time.h>

#include <string>
#include <sstream>
#include <algorithm>
#include <vector>
#include <list>
#include <map>
#include <set>
#include <queue>
#include <limits>
#include <iostream>

#define PB push_back
#define MP make_pair
#define ALL(x) x.begin(), x.end()
#define CLR(x, val) memset(x, val, sizeof(x))
#define LL long long

int n, c;

std::pair<int, int> arr[5005];

int bestAns = 0;
std::vector<int> ans;

void relax() {
	int curW = 0;
	for(int i = 0; i < n; ++i) {
		if (curW + arr[i].first <= c) {
			curW += arr[i].first;
		}
	}
	if (curW > bestAns) {
		int curW = 0;
		ans.clear();
		for(int i = 0; i < n; ++i) {
			if (curW + arr[i].first <= c) {
				curW += arr[i].first;
				ans.PB(arr[i].second);
			}
		}
		bestAns = curW;
	}
}

#ifdef TEST_SYSTEM
void solve2(int size, int* years, int* answer, int cap, int *iterations, int trueAns, float TL) {
    n = size; c = cap;
    bestAns = 0;

    for (int i = 0; i < n; ++i) {
        arr[i].first = years[i];
        arr[i].second = i;
    }
    int iters = 0;
    LL a = clock();
    while ((clock() - a) * 1.0 / CLOCKS_PER_SEC < TL) {
        relax();
        if (bestAns < trueAns) {
            iters++;
        } else if (bestAns == trueAns) {
            break;
        }
        std::random_shuffle(arr, arr + n);
    }

    *iterations = iters;
    sort(ALL(ans));
    for (int i = 0; i < ans.size(); ++i) {
        answer[ans[i]] = 1;
    }
}
#endif

#ifndef TEST_SYSTEM
void solve() {
	scanf("%d%d", &n, &c);
	for(int i = 0; i < n; ++i) {
		scanf("%d", &arr[i].first);
		arr[i].second = i;
	}
	LL a = clock();
	while((clock() - a) * 1.0 / CLOCKS_PER_SEC < 1.5) {
		relax();
		std::random_shuffle(arr, arr + n);
	}
	printf("%lu\n", ans.size());
	sort(ALL(ans));
	for(int i = 0; i < ans.size(); ++i)
		printf("%d ", ans[i] + 1);
}

int main() {
  freopen("bibliophile.in", "r", stdin);
  freopen("bibliophile.out", "w", stdout);
  srand(time(NULL));
  solve();
  return 0;
}
#endif
