package ru.ifmo.ctd.ngp.demo.testgen.flows.experiments

import java.io.{File, FileWriter, PrintWriter}

import ru.ifmo.ctd.ngp.demo.testgen.flows.solvers._
import ru.ifmo.ctd.ngp.demo.testgen.flows.{Config, MaxFlowSolver}

import scala.collection.JavaConverters._

/**
 * Genetic test generation.
 */
object Genetics extends App {
  val maxFitnessEvaluations = 500000

  val algorithms = IndexedSeq(
    new FordFulkersonScaling,
    new EdmondsCarp,
    new EdmondsCarpScaling,
    new Dinic,
    new DinicSlow,
    new ImprovedShortestPath
  )

  val logRoot = new File("flows/logs/genetic")
  logRoot.mkdirs()
  val dimacsRoot = new File("flows/tests/genetic")
  dimacsRoot.mkdirs()

  val emptyRun = () => ()
  val runs = for {
    a <- algorithms
    f <- a.getPossibleKeys.asScala.filter(_ != MaxFlowSolver.ANSWER_KEY)
    x <- IndexedSeq("shift", "single")
    o <- IndexedSeq("any", "inc")
    n <- 0 until 25
  } yield {
    val dimacsFile = new File(dimacsRoot, f"gen-${a.getName.filter(_.isUpper)}-$f-$x-$o-run$n%02d.dimacs")
    if (dimacsFile.exists()) {
      println(s"File ${dimacsFile.getName} exists, skipping")
      emptyRun
    } else () => {
      val logFile = new File(logRoot, f"${a.getName}/$f-$x-$o-run$n%02d")
      logFile.getParentFile.mkdirs()
      val logWriter = new PrintWriter(new FileWriter(logFile), true)
      val ind = new Config(
        solver = a,
        maxV = 100,
        maxE = 5000,
        maxC = 10000,
        criterion = f,
        maxEvaluations = maxFitnessEvaluations,
        generationSize = 100,
        useShiftCrossover = x == "shift",
        increasingEdge = o == "inc",
        output = logWriter
      ).evolve().input
      logWriter.close()

     Util.printDimacs(ind, 1, 100,
       s"""c Genetically generated test
          |c Algorithm against which generated: ${a.getName}
          |c Target objective: $f
          |c Index in row: $n
        """.stripMargin, dimacsFile)
    }
  }
  val nonEmpty = runs.filter(_ != emptyRun)
  println(s"Total configurations to run: ${nonEmpty.size}")
  nonEmpty.par.foreach(_())
}
