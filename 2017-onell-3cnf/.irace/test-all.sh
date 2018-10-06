#!/bin/bash

INSTANCE_FILE="$1"
TUNING_FILE="$2"

while read -r INSTANCE
do
    echo "$INSTANCE"
    INDEX=0
    while read -r TUNING
    do
        INDEX=$((INDEX + 1))
        EXPR="0.0"
        TIMES=10
        TIMES_INV="0.1"
        for (( i = 0; i < TIMES; ++i )); do
            OUTCOME=`./target-runner.sh $INSTANCE $TUNING`
            EXPR="$EXPR + $OUTCOME"
        done
        RESULT=`bc -q <(echo "($EXPR) * ${TIMES_INV}; halt")`
        echo "  $INDEX: $RESULT"
    done < "$TUNING_FILE"
done < "$INSTANCE_FILE"
