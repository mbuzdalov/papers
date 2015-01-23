#!/bin/bash

if [[ "$1" == "clean" ]]; then
    rm -rf classes
else
    mkdir -p classes
    javac -Xlint:unchecked -cp src -d classes src/ru/ifmo/steady/{*.java,treap/*.java,util/*.java,inds/*.java}
    java -cp classes ru.ifmo.steady.SolutionStorageTests
fi

