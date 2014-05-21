#!/bin/bash

g++ random.cpp -Wwrite-strings -fpermissive -O2 -o random -w

rm -rf r t
mkdir r t

TASKS=""

for N in 10 15 20 25 30 50 100 300 1000 5000
do
    for (( C = 5 ; C <= 95 ; C += 5 ))
    do
        TASKS="$TASKS $N-0.`printf %02d $C`"
    done
done

parallel -j 28 './random -P 1000 -X {} -V false > t/{}.out' ::: $TASKS

echo -n "C{\\textbackslash}N"
for N in 10 15 20 25 30 50 100 300 1000 5000
do
    echo -n "&"
    echo -n $N
done
echo "\\\\\\hline"

for (( C = 5; C <= 95; C += 5))
do
    echo -n `printf 0.%02d $C`
    for N in 10 15 20 25 30 50 100 300 1000 5000
    do
        echo -n "&"
        cat t/$N-0.`printf %02d $C`.out
    done
    echo "\\\\"
done

rm random
