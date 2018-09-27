name := "one-ll"

version := "1.0"

scalaVersion := "2.12.6"

scalacOptions ++= Seq("-unchecked", "-opt:l:inline", "-opt-inline-from:**", "-opt-warnings:_")

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
