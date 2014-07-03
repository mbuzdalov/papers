@echo off

if "%1"=="clean" goto clean

echo Compiling Scala sources...
call scalac -cp lib\ngp-core.jar;. proteins\*.scala proteins\encodings\*.scala proteins\es\*.scala proteins\intersection\*.scala
echo Starting experiment...
call scala -cp lib\ngp-core.jar;. proteins.es.SinCosMain %1 %2 %3 %4 %5 %6 %7 %8 %9

goto finish

:clean

echo Deleting class files...
del proteins\*.class proteins\encodings\*.class proteins\es\*.class proteins\intersection\*.class

:finish
