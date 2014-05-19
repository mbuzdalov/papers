#include <cstdlib>
#include <unistd.h>

#include "minknap.c"
#define TEST_SYSTEM
#include "bibliophile_7511_ok_X.cpp"

using std::min;
using std::max;

#define SINT sizeof(int)

int   NN = 30;
float CC = 0.9;
int   MAX_PASSES = 10;
float TL = 1.5;

class Test {
public:
    int* year;
    int capacity;

    Test() : capacity(0) {
        int least = 2010;
        year = (int*) calloc(NN,SINT);
        for (int i = 0; i < NN; ++i) {
            year[i] = rand() % 2009 + 1;
            capacity += year[i];
            least = min(least,year[i]);
        }
        capacity = max(least,(int) (capacity*CC));
    }

    ~Test() {
        free(year);
    }
};

int doTheTesting(Test *test) {
    int *trueAns = (int*) calloc(NN,SINT);
    int *subAns = (int*) calloc(NN,SINT);
    int* iterations = new int;

    minknap(NN,test->year, test->year, trueAns,test->capacity);

    int trueCap = 0;
    for (int i = 0; i < NN; ++i) {
        if (trueAns[i]) {
            trueCap += test->year[i];
        }
    }
    solve2(NN,test->year,subAns,test->capacity,iterations,trueCap,TL);

    int subCap = 0;
    for (int i = 0; i < NN; ++i) {
        if (subAns[i]) {
            subCap += test->year[i];
        }
    }
    if (subCap != trueCap) {
        printf("Win!\n");
    }
    int it = *iterations;
    free(trueAns); free(subAns);
    return it;
}

int main(int argc, char **argv) {
    int c;
    opterr = 0;
    bool verbose = true;

    while ((c = getopt(argc,argv,"N:C:P:V:")) != -1) {
        switch (c) {
            case 'N':
                NN = atoi(optarg);
                break;
            case 'C':
                CC = atof(optarg);
                break;
            case 'P':
                MAX_PASSES = atoi(optarg);
                break;
            case 'V':
                verbose = strcmp(optarg, "true") == 0;
                break;
        }
    }

    srand(time(NULL));

    double sumAll = 0;
    double sumSq = 0;

    for (int t = 0; t < MAX_PASSES; ++t) {
        Test test;
        int v = doTheTesting(&test);
        sumAll += v;
        sumSq += (double) (v) * v;
    }

    double avg = sumAll / MAX_PASSES;
    double avg2 = sumSq / MAX_PASSES;

    if (verbose) {
        printf("avg = %lf, std = %lf, avg * n = %lf\n", avg, sqrt(avg2 - avg * avg), avg * NN);
    } else {
        printf("%lf", avg * NN);
    }

    return 0;
}
