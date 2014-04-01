#! /bin/sh

if [[ "$1" == "clean" ]]
then
    echo -n "Deleting class files... "
    rm -f jobshop/*.class
    echo "done."
else
    echo -n "Compiling Java sources... "
    javac -cp lib/ngp-core.jar:. jobshop/*.java
    echo "done."
    echo -n "Compiling Scala sources... "
    scalac -cp lib/ngp-core.jar:. jobshop/*.scala
    echo "done."
    echo "Starting experiment..."
    scala -cp lib/ngp-core.jar:. jobshop.Main
fi
