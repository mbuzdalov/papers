#!/bin/bash

rm -rf classes
mkdir classes

if [[ "$1" == "pictures" ]]; then
    which scala > /dev/null
    if [[ "$?" == "0" ]]; then
        echo -n "Scala found, compiling result parser..." && scalac -cp src -d classes src/ru/ifmo/eps/ResultParser.scala && echo "done!"
        echo -n "Building pictures... " && scala -cp classes ru.ifmo.eps.ResultParser logs/results.log . && echo "done!"
    else
        echo "No Scala found, not building pictures"
    fi
elif [[ "$1" == "experiments" ]]; then
    echo -n "Compiling... " && javac -cp src -d classes src/ru/ifmo/eps/*.java \
                                                        src/ru/ifmo/eps/tests/*.java \
                                                        src/ru/ifmo/eps/orq/*.java \
                                                        src/ru/ifmo/eps/util/*.java \
                            && echo "done!"
    echo "Running unit tests..." && java -cp classes ru.ifmo.eps.tests.Tests && \
    echo "Running torture tests..." && java -cp classes ru.ifmo.eps.tests.Torture && \
    echo "Running timing tests..." && taskset 0x01 java -Xmx2G -Xms2G -cp classes ru.ifmo.eps.tests.Timing | tee logs/results.log
else
    echo "Usage: $0 [<experiments> | <pictures>]"
    exit 1
fi

rm -rf classes
