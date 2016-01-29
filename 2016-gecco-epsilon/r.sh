#!/bin/bash

rm -rf classes
mkdir classes
echo -n "Compiling... " && javac -cp src -d classes src/ru/ifmo/eps/*.java \
                                                    src/ru/ifmo/eps/tests/*.java \
                                                    src/ru/ifmo/eps/orq/*.java && echo "done!"
echo "Running unit tests..." && java -cp classes ru.ifmo.eps.tests.Tests && \
echo "Running torture tests..." && java -cp classes ru.ifmo.eps.tests.Torture && \
echo "Running timing tests..." && java -cp classes ru.ifmo.eps.tests.Timing
rm -rf classes
