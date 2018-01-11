name := "cma-es"

version := "1.0"

scalaVersion := "2.12.4"

scalacOptions ++= Seq("-unchecked", "-opt-warnings:_")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "org.apache.commons" % "commons-math3" % "3.6.1",
  "org.scalanlp" %% "breeze-natives" % "0.13.2",
  "org.scalanlp" %% "breeze" % "0.13.2"
)
