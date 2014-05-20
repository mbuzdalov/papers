#!/bin/bash

g++ result-check.cpp -O2 -o result-check

LIMIT=2995

TASKS=""

for (( x = 0 ; x <= $LIMIT ; ++x ))
do
    TASKS="$TASKS `printf %04d $x`"
done

echo $TASKS

parallel './result-check r/result-{}.txt r/result-{}.dat > r/result-{}.out' ::: $TASKS

rm result-check
