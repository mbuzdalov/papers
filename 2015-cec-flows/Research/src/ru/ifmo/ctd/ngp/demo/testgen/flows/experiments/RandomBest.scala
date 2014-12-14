package ru.ifmo.ctd.ngp.demo.testgen.flows.experiments

import java.io.{FileWriter, PrintWriter, File}

import ru.ifmo.ctd.ngp.demo.testgen.flows.MaxFlowSolver
import ru.ifmo.ctd.ngp.demo.testgen.flows.experiments.generators._
import ru.ifmo.ctd.ngp.demo.testgen.flows.solvers._

import scala.collection.JavaConverters._

/**
 * Generates tests by tracking the best test over 500000 randomly generated according to some patterns.
 */
object RandomBest extends App {
  val maxFitnessEvaluations = 500000

  val algorithms = IndexedSeq(
    new FordFulkersonScaling,
    new EdmondsCarp,
    new EdmondsCarpScaling,
    new Dinic,
    new DinicSlow,
    new ImprovedShortestPath
  )

  val logRoot = new File("flows/logs/random")
  logRoot.mkdirs()
  val dimacsRoot = new File("flows/tests/random-best")
  dimacsRoot.mkdirs()

  val emptyRun = () => ()

  val runs = for {
    a <- algorithms
    f <- a.getPossibleKeys.asScala.filter(_ != MaxFlowSolver.ANSWER_KEY)
    g <- IndexedSeq(RandomAcyclic, RandomAny, RandomNoDup, RandomNoSame, RandomFrames, TransitGrid)
    n <- 0 until 25
  } yield {
    val dimacsFile = new File(dimacsRoot, f"${g.name}-${a.getName.filter(_.isUpper)}-$f-run$n%02d.dimacs")
    if (dimacsFile.exists()) {
      println(s"File ${dimacsFile.getName} exists, skipping")
      emptyRun
    } else () => {
      val logFile = new File(logRoot, f"${g.name}-${a.getName}-$f-run$n%02d.log")
      logFile.getParentFile.mkdirs()
      val logWriter = new PrintWriter(new FileWriter(logFile), true)

      val maxV = 100
      val maxE = 5000
      val maxC = 10000

      def runOne() = {
        val t = g.generate(maxV, maxE, maxC)
        val v = a.solve(t.edges.asJava, t.src, t.trg, 5000).get(f)
        (t, v)
      }

      var (bestTest, bestValue) = runOne()
      logWriter.println(s"1 $bestValue")
      for (t <- 2 to maxFitnessEvaluations) {
        val (currTest, currValue) = runOne()
        if (currValue > bestValue) {
          bestValue = currValue
          bestTest = currTest
          logWriter.println(s"$t $bestValue")
        }
      }
      logWriter.close()

      Util.printDimacs(bestTest.edges, bestTest.src + 1, bestTest.trg + 1,
        s"""c Best-of-random test
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
