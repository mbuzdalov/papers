#!/bin/bash

if [[ "$1" == "clean" ]]; then
    rm -rf classes
elif [[ "$1" == "expand" ]]; then
	if [[ "$2" == "" ]]; then
		"$0" "$1" "."
	elif [[ -d "$2" ]]; then
		for Q in "$2"/*; do
			"$0" "$1" "$Q"
		done
	elif [[ -f "$2" ]]; then
		LAST_FIVE=${2:${#2}-5:5}
		if [[ "$LAST_FIVE" == ".java" ]]; then
			expand -t 4 "$2" > "$2.e"
			diff "$2" "$2.e" > /dev/null
			if [[ "$?" != "0" ]]; then
				echo "[expand] $2 expanded"
				mv "$2.e" "$2"
			else
				rm "$2.e"
			fi
		fi
	fi
elif [[ "$1" == "paper-steadiness-wilcox" ]]; then
    which Rscript > /dev/null
    if [[ "$?" == "0" ]]; then
        if [[ "$2" == "" ]]; then
            RUNDIR=paper-steadiness-runs/25000-100
        else
            RUNDIR="$2"
        fi
        for pss in "$RUNDIR"/*-PSS-hv.txt; do
            bibr=${pss/PSS/BIBR}
            bisr=${pss/PSS/BISR}
            sisr=${pss/PSS/SISR}

            src/Wilcox.R $pss $sisr
            src/Wilcox.R $pss $bisr
            src/Wilcox.R $pss $bibr
            src/Wilcox.R $sisr $bisr
            src/Wilcox.R $sisr $bibr
            src/Wilcox.R $bisr $bibr

            echo ""
        done
    else
        echo "Error: no Rscript found, will not do Wilcoxon tests"
    fi
else
	"$0" "expand"
    mkdir -p classes
    javac -Xlint:unchecked -cp src -d classes src/ru/ifmo/steady/{*.java,util/*.java,inds/*.java,enlu/*.java,problem/*.java}
    java -cp classes ru.ifmo.steady.SolutionStorageTests >/dev/null
    if [[ "$?" == "0" ]]; then
        if [[ "$1" == "paper-nsga" ]]; then
            java -cp classes ru.ifmo.steady.Experiments \
                -O:debselTrue -O:jmetalTrue \
                -S:inds -S:enlu -S:deb \
                -V:bibr -V:pss \
                -D=paper-nsga-runs -R=100 -N=25000:100 \
                | tee paper-nsga.log

            which scalac > /dev/null
            if [[ "$?" == "0" ]]; then
                scalac -d classes -sourcepath src src/Parser.scala
                scala -cp classes Parser paper-nsga.log | tee paper-nsga.tex
            else
                echo "Error: no scala compiler found, will not build LaTeX table of results"
            fi
        elif [[ "$1" == "paper-steadiness" ]]; then
            java -cp classes ru.ifmo.steady.Experiments \
                -O:debselTrue -O:jmetalFalse \
                -S:inds -V:pss \
                -V:sisr -V:bisr -V:bibr \
                -D=paper-steadiness-runs -R=1000 -N=25000:100 \
                | tee paper-steadiness.log

            if [[ "$?" == "0" ]]; then
                "$0" paper-steadiness-wilcox | tee paper-steadiness.wilcox
            fi

            which scalac > /dev/null
            if [[ "$?" == "0" ]]; then
                scalac -d classes -sourcepath src src/Parser.scala
                scala -cp classes Parser paper-steadiness.log | tee paper-steadiness.tex
            else
                echo "Error: no scala compiler found, will not build LaTeX table of results"
            fi
        elif [[ "$1" == "paper-convex-hull" ]]; then
            java -cp classes ru.ifmo.steady.Experiments \
                -O:debselTrue -O:jmetalFalse \
                -S:inds -S:inds-lasthull -S:inds-allhulls \
                -V:pss \
                -D=paper-convex-hull -R=20 \
                -N=25000:100 -N=250000:1000 -N=2500000:10000\
                | tee paper-convex-hull.log
        else
            java -cp classes ru.ifmo.steady.Experiments "$@"
            if [[ "$?" != "0" ]]; then
                echo "Experiment exited with non-zero code."
                echo "Possible explanations:"
                echo "  - Experiment code crashed"
                echo "  - You hit Ctrl+C or terminated experiments otherwise"
                echo "  - No arguments given. You should use one of the following options:"
                echo "    - $0 clean"
                echo "      Cleans up the compiled files."
                echo "    - $0 paper-nsga"
                echo "      Runs experiment for the paper:"
                echo "          Fast Implementation of Steady-State NSGA-II Algorithm"
                echo "          for Two Dimensions Based on Incremental Non-Dominated Sorting"
                echo "    - $0 paper-steadiness"
                echo "      Runs experiment for the paper:"
               	echo "          Various Degrees of Steadiness in NSGA-II"
                echo "          and Their Influence on the Quality of Results"
                echo "    - $0 paper-steadiness-wilcox"
                echo "      Reruns Wilcoxon test on the experiment results of paper-steadiness"
                echo "    - $0 paper-convex-hull"
                echo "      Runs experiments from the paper:"
                echo "          Efficient Removal of Points with Smallest Crowding Distance"
                echo "          in Two-dimensional Incremental Non-dominated Sorting"
                echo "    - $0 <experiment arguments>"
                echo "      Runs the experiment subset you want. Adhere to error messages above."
            fi
        fi
    else
        echo "Unit test failure: Re-running tests..."
        java -cp classes ru.ifmo.steady.SolutionStorageTests
    fi
fi
