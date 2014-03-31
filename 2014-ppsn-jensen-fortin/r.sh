#! /bin/bash

if [[ "$1" != "clean" ]]
then
    echo -n "Compiling Java sources... "
    javac -cp . opt/util/FastRandom.java
    echo "done."

    echo -n "Compiling Scala sources..."
    scalac -cp . opt/util/optimized/*.scala\
             opt/util/*.scala\
             opt/test/*.scala\
             opt/types/*.scala\
             opt/multicriteria/nds/*.scala\
             opt/multicriteria/*.scala\
             opt/*.scala
    echo "done."

    echo -n "Running tests..."
    scala -cp . opt.test.All
    echo "done."

    echo "Running benchmark..."
    scala -cp . opt.test.NonDominatedSortingPerformance
    echo "done."
else
    rm -f opt/util/optimized/*.class\
          opt/util/*.class\
          opt/test/*.class\
          opt/types/*.class\
          opt/multicriteria/nds/*.class\
          opt/multicriteria/*.class\
          opt/*.class
fi
