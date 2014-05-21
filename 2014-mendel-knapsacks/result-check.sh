#!/bin/bash

g++ result-check.cpp -O2 -Wunused-result -w -o result-check

LIMIT=4000

TASKS=""

for (( x = 0 ; x <= $LIMIT ; ++x ))
do
    ZZZ=`printf %04d $x`
    if [[ -f r/result-$ZZZ.txt ]]
    then
        TASKS="$TASKS $ZZZ"
    fi
done

parallel -j 28 './result-check r/result-{}.txt r/result-{}.dat > r/result-{}.out' ::: $TASKS

rm result-check
