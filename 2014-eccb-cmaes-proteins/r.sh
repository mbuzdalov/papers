#! /bin/sh

if [[ "$1" == "clean" ]]
then
    echo -n "Deleting class files... "
    rm -f proteins/*.class proteins/encodings/*.class proteins/es/*.class proteins/intersection/*.class
    echo "done."
else
    echo -n "Compiling Scala sources... "
    scalac -cp lib/ngp-core.jar:. proteins/*.scala proteins/encodings/*.scala proteins/es/*.scala proteins/intersection/*.scala
    echo "done."
    echo "Starting experiment..."
    scala -cp lib/ngp-core.jar:. proteins.es.SinCosMain "$@"
fi
