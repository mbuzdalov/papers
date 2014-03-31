@echo off

if "%1"=="clean" goto clean

echo "Compiling Java sources... "
javac -cp . opt\util\FastRandom.java

echo "Compiling Scala sources..."
scalac -cp . opt\util\optimized\*.scala opt\util\*.scala opt\test\*.scala opt\types\*.scala opt\multicriteria\nds\*.scala opt\multicriteria\*.scala opt\*.scala

echo "Running tests..."
scala -cp . opt.test.All

echo "Running benchmark..."
scala -cp . opt.test.NonDominatedSortingPerformance

goto finish

:clean

echo "Deleting class files... "
del opt\util\optimized\*.class opt\util\*.class opt\test\*.class opt\types\*.class opt\multicriteria\nds\*.class opt\multicriteria\*.class opt\*.class

:finish
