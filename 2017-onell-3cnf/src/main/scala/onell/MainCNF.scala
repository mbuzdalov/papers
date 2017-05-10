package onell

import java.util.Locale

import onell.algorithms.{OnePlusLambdaLambdaGA, OnePlusOneEA}
import onell.problems.{OneMax, Random3CNF}
import onell.util.Plotter
import onell.util.RunHelpers._
import onell.util.Misc._

import scala.language.implicitConversions

/**
  * The main class which runs experiments for OneMax and 3CNF.
  */
object MainCNF {
  def getOneMax(n: Int)     = new OneMax(n)
  def getRandom3CNF(n: Int) = new Random3CNF(n, (4 * n * math.log(n)).toInt)

  def getOnePlusOneEA(n: Int) = OnePlusOneEA
  def getOnePlusLLN(n: Int)   = new OnePlusLambdaLambdaGA()
  def getOnePlusLLLog(n: Int) = new OnePlusLambdaLambdaGA(1, "1", 2 * math.log(n + 1), "ln n", "$\\lambda \\le 2 \\ln n$")
  def getOnePlusLLx(n: Int, x: Int) = new OnePlusLambdaLambdaGA(x, x.toString, x, x.toString, s"$$\\lambda = $x$$")

  def main(args: Array[String]): Unit = {
    Locale.setDefault(Locale.US)

    val nThreads = args.getOption("--threads=").map(_.toInt).getOrElse(Runtime.getRuntime.availableProcessors())
    val nRuns = args.getOption("--runs=").map(_.toInt).getOrElse(100)
    val plotPath = args.getOption("--plots=")
    val cachePath = args.getOption("--cache=").getOrElse("cache")

    println("My options are:")
    println(s"  threads=$nThreads")
    println(s"  runs=$nRuns")
    println(s"  plots='$plotPath'")
    println(s"  cache='$cachePath'")

    val runner = new Runner(nThreads)
    try {
      val oneLLConfigurations = {
        (4 to 24).map(1 << _).flatMap(n => Seq(
          Config(getOneMax(n), getOnePlusOneEA(n)),
          Config(getOneMax(n), getOnePlusLLN(n)),
          Config(getOneMax(n), getOnePlusLLLog(n))
        ))
      } ++ {
        (7 to 20).map(1 << _).flatMap(n => Seq(
          Config(getRandom3CNF(n), getOnePlusOneEA(n)),
          Config(getRandom3CNF(n), getOnePlusLLLog(n))
        ))
      } ++ {
        (7 to 16).map(1 << _).flatMap(n => Seq(
          Config(getRandom3CNF(n), getOnePlusLLN(n))
        ))
      }

      val fixedOneLLConfigurations = (for {
        x <- 2 to 20
        np <- 4 to 24
        n = 1 << np
      } yield Config(getOneMax(n), getOnePlusLLx(n, x))) ++ (for {
        x <- 2 to 20
        np <- 7 to 20
        n = 1 << np
      } yield Config(getRandom3CNF(n), getOnePlusLLx(n, x)))

      val oneLLByProblem = byProblem(oneLLConfigurations)
      val fixedOneLLByProblem = byProblem(fixedOneLLConfigurations)

      val plotter = new Plotter(p => {
        val name = p.name
        val open = name.indexOf('(')
        val next = name.indexWhere(!_.isDigit, open + 1)
        val title0 = name.substring(0, open)
        val title = title0.flatMap(ch => if (ch.isLetter) Some(ch) else None)
        (title, name.substring(open + 1, next).toDouble)
      })

      for ((problemName, configs) <- oneLLByProblem ++ fixedOneLLByProblem) {
        println(s"$problemName:")
        for (config <- configs) {
          val algorithm = config.algorithm
          val problem = config.problem
          val stats = runner.compute(cachePath, algorithm, problem, nRuns).transpose.map(d => new Statistics(d))
          val names = algorithm.metrics
          println(s"  ${algorithm.name}:")
          for ((stat, name) <- (stats, names).zipped) {
            println(s"    $name: ${stat.everything}")
          }
          val fevMetricIndex = names.indexOf("Fitness evaluations")
          plotter.append(algorithm, problem, stats(fevMetricIndex))
        }
      }

      plotPath.foreach(p => plotter.writeAllTikZPlots(p, iqr = false, _ => true))
    } finally {
      runner.close()
    }
  }
}
