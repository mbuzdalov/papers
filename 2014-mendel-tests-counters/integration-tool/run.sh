#! /bin/sh

MAINJAR="integration-tool.jar"
ANTLRJAR="antlr-4.0-complete.jar"

PATHSEP=":"
if [[ ! -d "/usr/local" ]]
then
    PATHSEP=";"
fi

if [[ ! -d "lib" ]]; then
    mkdir lib
fi

if [[ ! -f "lib/${ANTLRJAR}" ]]; then
    echo "ANTLR 4.0 not found. Downloading..."
    wget --no-check-certificate -q "https://github.com/antlr/website-antlr4/blob/gh-pages/download/antlr-4.0-complete.jar?raw=true" -O "lib/${ANTLRJAR}"
    echo "ANTLR 4.0 downloaded."
fi

if [[ ! -f "lib/${MAINJAR}" ]]; then
    echo -n "Compiled version not found. Compiling..."
    cd src
    mkdir tmp
    javac -cp ../lib/${ANTLRJAR}${PATHSEP}. -d tmp *.java util/*.java
    cd tmp
    jar -cf ${MAINJAR} .
    mv ${MAINJAR} ../../lib/
    cd ..
    rm -rf tmp
    cd ..
    echo "done."
fi

java -cp lib/${ANTLRJAR}${PATHSEP}lib/${MAINJAR} Main "$@"
