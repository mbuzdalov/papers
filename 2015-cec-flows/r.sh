#!/bin/bash

ROOT=`readlink -f .`

if [[ "$1" == "clean" ]]; then
    echo -n "Removing class files... " && find . -iname '*.class' -delete && echo "done."
    echo -n "Removing experiment logs... " && rm -rf flows/logs/* && echo "done."
    echo -n "Removing experiment runs... " && rm -rf flows/tests/genetic flows/tests/random-best && echo "done."
    echo -n "Removing experiment summary... " && rm -rf flows/summary/* && echo "done."
elif [[ "$1" == "compile" ]]; then
    echo -n "Compiling Core... " && cd Core/src &&\
    javac -Xlint:unchecked ru/ifmo/ctd/ngp/util/*.java &&\
    scalac -deprecation  \
           ru/ifmo/ctd/ngp/opt/*.scala\
           ru/ifmo/ctd/ngp/opt/event/*.scala\
           ru/ifmo/ctd/ngp/opt/iteration/*.scala\
           ru/ifmo/ctd/ngp/opt/listeners/*.scala\
           ru/ifmo/ctd/ngp/opt/termination/*.scala\
           ru/ifmo/ctd/ngp/opt/types/*.scala\
           ru/ifmo/ctd/ngp/util/*.scala\
           ru/ifmo/ctd/ngp/util/optimized/*.scala &&\
    echo "done."
    cd $ROOT
    echo -n "Compiling Research... " && cd Research/src &&\
    javac -Xlint:unchecked -cp .:../../Core/src\
          ru/ifmo/ctd/ngp/demo/testgen/*.java \
          ru/ifmo/ctd/ngp/demo/testgen/flows/*.java \
          ru/ifmo/ctd/ngp/demo/testgen/flows/solvers/*.java \
          ru/ifmo/ctd/ngp/demo/testgen/flows/solvers/util/*.java &&\
    scalac -deprecation -cp .:../../Core/src\
          ru/ifmo/ctd/ngp/demo/testgen/flows/*.scala\
          ru/ifmo/ctd/ngp/demo/testgen/flows/experiments/*.scala\
          ru/ifmo/ctd/ngp/demo/testgen/flows/experiments/generators/*.scala &&\
    echo "done."
    cd $ROOT
elif [[ "$1" == "random" ]]; then
    if [[ ! -f "Research/src/ru/ifmo/ctd/ngp/demo/testgen/flows/experiments/RandomBest.class" ]]; then
        "$0" "compile"
    fi
    if [[ -f "Research/src/ru/ifmo/ctd/ngp/demo/testgen/flows/experiments/RandomBest.class" ]]; then
        echo "Starting experiments with random test generation"
        scala -cp Core/src:Research/src ru.ifmo.ctd.ngp.demo.testgen.flows.experiments.RandomBest
    fi
elif [[ "$1" == "genetic" ]]; then
    if [[ ! -f "Research/src/ru/ifmo/ctd/ngp/demo/testgen/flows/experiments/Genetics.class" ]]; then
        "$0" "compile"
    fi
    if [[ -f "Research/src/ru/ifmo/ctd/ngp/demo/testgen/flows/experiments/Genetics.class" ]]; then
        echo "Starting experiments with genetic test generation"
        scala -cp Core/src:Research/src ru.ifmo.ctd.ngp.demo.testgen.flows.experiments.Genetics
    fi
elif [[ "$1" == "summary" ]]; then
    "$0" "random"
    "$0" "genetic"
    if [[ -f "Research/src/ru/ifmo/ctd/ngp/demo/testgen/flows/experiments/Summary.class" ]]; then
        echo -n "Computing experiment summary... " &&\
        scala -cp Core/src:Research/src ru.ifmo.ctd.ngp.demo.testgen.flows.experiments.Summary &&\
        echo "done."
    fi
else
    echo "Expected one of four options:"
    echo "- clean:    removes everything which is generated (INCLUDING EXPERIMENT RESULTS)"
    echo "- random:   runs experiments for random test generation"
    echo "- genetic:  runs experiments for genetic test generation"
    echo "- summary:  computes summary of experiments"
fi
