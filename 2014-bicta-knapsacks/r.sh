#! /bin/sh

if [[ "$1" == "clean" ]]
then
    echo -n "Deleting class files... "
    rm -f knapsack/*.class knapsack/solvers/*.class
    echo "done."
else
    echo -n "Compiling Java sources... "
    javac -cp lib/ngp-core.jar:. knapsack/solvers/*.java knapsack/*.java
    echo "done."
    echo -n "Compiling Scala sources... "
    scalac -cp lib/ngp-core.jar:. knapsack/*.scala
    echo "done."
    echo "Starting experiment..."
    scala -cp lib/ngp-core.jar:. knapsack.Runner
fi
