@echo off

if "%1"=="clean" goto clean

echo Compiling Java sources...
call javac -cp lib\ngp-core.jar;. knapsack\solvers\*.java knapsack\*.java
echo Compiling Scala sources...
call scalac -cp lib\ngp-core.jar;. knapsack\*.scala
echo Starting experiment...
call scala -cp lib\ngp-core.jar;. knapsack.Runner

goto finish

:clean

echo "Deleting class files... "
del knapsack\*.class knapsack\solvers\*.class

:finish
