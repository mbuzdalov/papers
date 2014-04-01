@echo off

if "%1"=="clean" goto clean

echo Compiling Java sources...
call javac -cp lib\ngp-core.jar;. jobshop\*.java
echo Compiling Scala sources...
call scalac -cp lib\ngp-core.jar;. jobshop\*.scala
echo Starting experiment...
call scala -cp lib\ngp-core.jar;. jobshop.Main

goto finish

:clean

echo "Deleting class files... "
del jobshop\*.class

:finish
