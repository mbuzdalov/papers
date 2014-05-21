#!/bin/bash

g++ evolution.cpp -Wwrite-strings -fpermissive -O2 -o evolution -w

./evolution

rm evolution