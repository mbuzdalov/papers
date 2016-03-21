#!/bin/bash

rm -rf classes
mkdir classes

if [[ "$1" == "pictures" ]]; then
    echo "No pictures yet... :("
elif [[ "$1" == "experiments" ]]; then
    echo -n "Compiling... " && javac -cp src -d classes src/ru/ifmo/ibea/*.java \
                                                        src/ru/ifmo/ibea/tests/*.java \
                            && echo "done!"
    echo "Running unit tests..." && java -cp classes ru.ifmo.ibea.tests.FitnessAssignmentTests
else
    echo "Usage: $0 [<experiments> | <pictures>]"
    exit 1
fi

rm -rf classes
