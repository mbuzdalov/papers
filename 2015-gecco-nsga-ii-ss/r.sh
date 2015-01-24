#!/bin/bash

if [[ "$1" == "clean" ]]; then
    rm -rf classes
elif [[ "$1" == "expand" ]]; then
	if [[ "$2" == "" ]]; then
		echo "[expand] adding ."
		"$0" "$1" "."
	elif [[ -d "$2" ]]; then
		echo "[expand] entering directory $2"
		for Q in "$2"/*; do
			"$0" "$1" "$Q"
		done
	elif [[ -f "$2" ]]; then
		echo "[expand] visiting file $2"
		LAST_FIVE=${2:${#2}-5:5}
		if [[ "$LAST_FIVE" == ".java" ]]; then
			echo "[expand] expanding $2"
			expand -t 4 "$2" > "$2.e"
			mv "$2.e" "$2"
		fi
	fi
else
    mkdir -p classes
    javac -Xlint:unchecked -cp src -d classes src/ru/ifmo/steady/{*.java,util/*.java,inds/*.java,problem/*.java}
    java -cp classes ru.ifmo.steady.SolutionStorageTests
fi

