#!/bin/bash

g++ random.cpp -Wwrite-strings -fpermissive -O2 -o random -w

rm -rf r t
mkdir r t

TASKS=""

for N in 10 15 20 25 30 40 50 100 200 500 1000 2000 5000
do
    for (( C = 1 ; C <= 9 ; ++C ))
    do
        TASKS="$TASKS $N-0.$C"
    done
done

for (( N = 20 ; N <= 70 ; ++N ))
do
    for (( C = 0 ; C <= 9 ; ++C ))
    do
        TASKS="$TASKS $N-0.9$C"
    done
done

parallel './random -P 1000 -X {} -V false > t/{}.out' ::: $TASKS

echo "Initial table"
echo -n "N{\\textbackslash}C"
for (( C = 1 ; C <= 9 ; ++C ))
do
    echo -n "&"
    echo -n 0.$C
done
echo "\\\\\\hline"

for N in 10 15 20 25 30 40 50 100 200 500 1000 2000 5000
do
    echo -n $N
    for (( C = 1 ; C <= 9 ; ++C ))
    do
        echo -n "&"
        cat t/$N-0.$C.out
    done
    echo "\\\\"
done

echo "Next table"
echo -n "N{\\textbackslash}C"
for (( C = 0 ; C <= 9 ; ++C ))
do
    echo -n "&"
    echo -n 0.9$C
done
echo "\\\\\\hline"

for (( N = 20; N <= 70; ++N ))
do
    echo -n $N
    for (( C = 0 ; C <= 9 ; ++C ))
    do
        echo -n "&"
        cat t/$N-0.9$C.out
    done
    echo "\\\\"
done

rm random
