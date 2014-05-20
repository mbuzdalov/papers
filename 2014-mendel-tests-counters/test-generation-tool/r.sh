#! /bin/sh

if [[ "$1" == "clean" ]]
then
    echo -n "Deleting class files... "
    rm -f timus1394/*.class
    rm -f timus.jar
    echo "done."
    echo -n "Deleting compiled solutions and experiment results... "
    rm -rf solutions
    rm -rf timus
    echo "done."
else
    echo -n "Compiling Java sources... "
    javac -cp lib/ngp-core.jar:. timus1394/*.java
    echo "done."
    echo -n "Compiling Scala sources... "
    scalac -cp lib/ngp-core.jar:. timus1394/*.scala
    echo "done."
    echo -n "Compiling tested solutions... "
    mkdir solutions
    cp ../solutions/3-profiled/*.java solutions
    cd solutions
    for SRC in *.java
    do
        javac -cp .. $SRC
        jar -cf ${SRC:0:${#SRC}-5}.jar *.class
        rm *.class
    done
    cd ..
    echo "done."
    echo "Starting experiment..."
    scala -cp lib/ngp-core.jar:. timus1394.Main
fi
