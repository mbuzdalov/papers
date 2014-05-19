#include <cstdlib>
#include <unistd.h>
#include <sys/stat.h>

#define TEST_SYSTEM
#include "bibliophile_7511_ok_X.cpp"

int NN = -1;
float TL = 1.5;

using std::min;
using std::max;

#define SINT sizeof(int)

bool doTheTesting(int *test, int capacity, int trueAns) {
    int *subAns = (int*) calloc(NN,SINT);
    int* iterations = new int;

    solve2(NN,test,subAns,capacity,iterations,trueAns,TL);

    int subCap = 0;
    for (int i = 0; i < NN; ++i) {
        if (subAns[i]) {
            subCap += test[i];
        }
    }
    return subCap == trueAns;
}

int main(int argc, char **argv) {
    FILE *test = fopen(argv[1], "rt");
    FILE *dat = fopen(argv[2], "rt");
    int capacity;
    fscanf(test, "%d%d", &NN, &capacity);
    int *items = (int*) calloc(NN, SINT);
    for (int i = 0; i < NN; ++i) fscanf(test, "%d", items + i);
    char swap[32];
    for (int i = 0; i < 8; ++i) fscanf(dat, "%s", swap);
    int trueAns;
    fscanf(dat, "%d", &trueAns);
    
    int fails = 0;
    for (int i = 0; i < 1000; ++i) {
        fails += !doTheTesting(items, capacity, trueAns);
    }
    printf("%d/1000 fails\n", fails);
    return 0;
}
