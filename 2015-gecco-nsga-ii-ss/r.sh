#!/bin/bash

if [[ "$1" == "clean" ]]; then
    rm -rf classes
else
    mkdir -p classes
    javac -cp src -d classes src/ru/ifmo/steady/{*.java,treap/*.java,util/*.java,nsga2ss/*.java}
    java -cp classes ru.ifmo.steady.nsga2ss.Tests
fi

