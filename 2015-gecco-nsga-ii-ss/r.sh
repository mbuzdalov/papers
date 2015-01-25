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
else
	"$0" "expand"
    mkdir -p classes
    javac -Xlint:unchecked -cp src -d classes src/ru/ifmo/steady/{*.java,util/*.java,inds/*.java,enlu/*.java,problem/*.java}
    java -cp classes ru.ifmo.steady.SolutionStorageTests
fi

