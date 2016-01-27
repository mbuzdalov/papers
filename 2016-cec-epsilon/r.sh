#!/bin/bash

rm -rf classes
mkdir classes
echo -n "Compiling... " && javac -cp src -d classes src/ru/ifmo/eps/*.java src/ru/ifmo/eps/tests/*.java && echo "done!"
echo "Running tests..."
java -cp classes ru.ifmo.eps.tests.Tests
rm -rf classes
