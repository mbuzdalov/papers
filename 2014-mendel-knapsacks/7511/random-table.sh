#!/bin/bash

g++ random.cpp -Wwrite-strings -fpermissive  -o random -w

for (( C = 1 ; C <= 9 ; ++C ))
do
    echo -n " & "
    echo -n 0.$C
done
echo "\\\\"

for N in 10 15 20 25 30 40 50 100 200 500 1000 2000 5000
do
    echo -n $N
    for (( C = 1 ; C <= 9 ; ++C ))
    do
        echo -n " & "
        ./random -P 1000 -N $N -C 0.$C -V false
    done
    echo "\\\\"
done

rm random
