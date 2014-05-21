#include "minknap.c"
#include <cstdlib>
#define TEST_SYSTEM
#include "bibliophile_7511_ok_X.cpp"
#include <unistd.h>

using std::min;
using std::max;
using std::cout;

#define SINT sizeof(int)

int NN = 30;
int Q = 25;
float CC = 0.9;
int SEED = 53245235;
int MAX_PASSES = 100;
float TL = 30;
int passes;
int generation;
int brkn = 0;

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

    Test(float cc) : capacity(0) {
        int least = 2010;
        year = (int*) calloc(NN,SINT);
        for (int i = 0; i < NN; ++i) {
            year[i] = rand() % 2009 + 1;
            capacity += year[i];
            least = min(least,year[i]);
        }
        capacity = max(least,(int) (capacity*cc));
    }

    Test(const Test* pre) : capacity(0) {
        int least = 2010;
        year = (int*) calloc(NN,SINT);
        for (int i = 0; i < NN; ++i) {
            year[i] = min(max(1, pre->year[i] + rand() % Q - (Q/2) ),2009);
            capacity += year[i];
            least = min(least,year[i]);
        }
        capacity = max(least,(int) (capacity*CC));
        //random_shuffle(year,year+NN);
    }

    void print() {
        for (int i = 0; i < NN; ++i) {
            cout << year[i] << " ";
        }
        cout << "\n" << "Capacity: " << capacity << "\n";
    }

    void dump() {
        char seedPrefix[50] = {0};
        sprintf(seedPrefix, "generated/S%d_%.2f_P%d_G%d_XXXXXX", SEED, TL, passes, generation);
        int file = mkstemp(seedPrefix);
        printf("%d\n",file);
        dprintf(file, "%d %d\n",NN,capacity);
        for (int i = 0; i < NN; ++i) {
            dprintf(file,"%d ",year[i]);
        }
        dprintf(file,"\n");
        close(file);
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
    int v = rand();
    srand(SEED);
    solve2(NN,test->year,subAns,test->capacity,iterations,trueCap,TL);
    srand(v);

    int subCap = 0;
    for (int i = 0; i < NN; ++i) {
        if (subAns[i]) {
            subCap += test->year[i];
        }
    }
    printf("C: %d T: %d S: %d D: %d\n", test->capacity, trueCap, subCap, trueCap - subCap);
    int it = *iterations;
    if (subCap != trueCap || subCap > test->capacity) {
        test->dump();
        brkn = 1;
        return it;
    } else {
        return it;
    }
    free(trueAns); free(subAns);
}

void genTest() {
    Test *current;
    Test *best = new Test();

    FILE *progress = fopen("progress.csv","w");
    fprintf(progress,"passes;generation;bestIterations;newIter;difference\n");
    int nextSeed = rand();
    int bestIterations = doTheTesting(best);
    passes = 0;
    generation = 0;
    int newIter = 0;
    while (true) {
        srand(nextSeed);
        nextSeed = rand();
        if (passes > MAX_PASSES)  {
            printf("Restared\n");
            generation = 0;
            bestIterations = 0;
            passes = 0;
            current = new Test();
        } else {
            current = new Test(best);
        }
        newIter = doTheTesting(current);
        fprintf(progress,"%d;%d;%d;%d;%d\n",passes,generation,bestIterations,newIter,newIter-bestIterations);
        printf("B: %d N: %d D: %d P: %d G: %d\n",bestIterations,newIter,newIter-bestIterations,passes,generation);
        if (brkn) {
            //it broke
            printf("Broken\n");
            current->dump();
            fclose(progress);
            return;
        } else {
            if (newIter > bestIterations) {
                passes = 0;
                bestIterations = newIter;
                best = current;
            } else {
                ++passes;
            }
            generation++;
        }
    }

}

void doTheFlop() {
    FILE* data = fopen("flop2.txt","w");
    for (int NN =10; NN <= 1000; NN+=10) {
        for (float cc = 0.7; cc <= 1; cc+= 0.03) {
        //    srand(SEED);
            fprintf(data,"%d %f %d\n", NN, cc, doTheTesting(new Test(cc)));
            printf("%d %f\n", NN, cc);
        }
    }
    fclose(data);
}

int main(int argc, char **argv) {
    //
    // NN, Q, CC, SEED, MAX_PASSES
    //

    int c;
    opterr = 0;

    while ((c = getopt(argc,argv,"N:Q:C:S:P:T:")) != -1) {
        switch (c) {
            case 'N':
                NN = atoi(optarg);
                break;
            case 'Q':
                Q = atoi(optarg);
                break;
            case 'C':
                CC = atof(optarg);
                break;
            case 'S':
                SEED = atoi(optarg);
                break;
            case 'P':
                MAX_PASSES = atoi(optarg);
                break;
            case 'T':
                TL = atof(optarg);
                break;
        }
    }

    srand(time(NULL));
    genTest();
    //doTheFlop();

    return 0;
}
