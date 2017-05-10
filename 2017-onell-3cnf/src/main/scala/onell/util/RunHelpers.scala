package onell.util

import java.nio.file.{Files, Paths}
import java.util.Arrays
import java.util.concurrent.{Callable, Executors}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

import onell.{Algorithm, MutationAwarePseudoBooleanProblem}

/**
  * Helper classes and functions for running experiments.
  */
object RunHelpers {
  def byProblem(configs: Seq[Configuration]): Seq[(String, Seq[Configuration])] = {
    configs.groupBy(_.problem.name).mapValues(_.sortBy(_.algorithm.name)).toIndexedSeq.sortBy(_._1)(numberTokenSorting)
  }

  val numberTokenSorting = new Ordering[String] {
    override def compare(x: String, y: String): Int = {
      if (x.isEmpty && y.isEmpty) {
        0
      } else if (x.isEmpty) {
        -1
      } else if (y.isEmpty) {
        1
      } else {
        val xStartsWithDigit = x(0).isDigit
        val yStartsWithDigit = y(0).isDigit
        if (xStartsWithDigit != yStartsWithDigit) {
          x(0).compare(y(0))
        } else if (xStartsWithDigit) {
          val xFirstNonDigit0 = x.indexWhere(!_.isDigit)
          val yFirstNonDigit0 = y.indexWhere(!_.isDigit)
          val xFirstNonDigit = if (xFirstNonDigit0 == -1) x.length else xFirstNonDigit0
          val yFirstNonDigit = if (yFirstNonDigit0 == -1) y.length else yFirstNonDigit0
          val xDigit = x.substring(0, xFirstNonDigit).toLong
          val yDigit = y.substring(0, yFirstNonDigit).toLong
          if (xDigit == yDigit) {
            compare(x.substring(xFirstNonDigit), y.substring(yFirstNonDigit))
          } else {
            xDigit.compare(yDigit)
          }
        } else {
          val xFirstDigit0 = x.indexWhere(_.isDigit)
          val yFirstDigit0 = y.indexWhere(_.isDigit)
          val xFirstDigit = if (xFirstDigit0 == -1) x.length else xFirstDigit0
          val yFirstDigit = if (yFirstDigit0 == -1) y.length else yFirstDigit0
          val xPrefix = x.substring(0, xFirstDigit)
          val yPrefix = y.substring(0, yFirstDigit)
          if (xPrefix != yPrefix) {
            xPrefix.compareTo(yPrefix)
          } else {
            compare(x.substring(xFirstDigit), y.substring(yFirstDigit))
          }
        }
      }
    }
  }

  abstract class Configuration {
    type Fitness
    def problem: MutationAwarePseudoBooleanProblem[Fitness]
    def algorithm: Algorithm[Fitness]
  }

  case class Config[F](problem: MutationAwarePseudoBooleanProblem[F], algorithm: Algorithm[F]) extends Configuration {
    override type Fitness = F
  }

  class Runner(nThreads: Int) {
    private final val service = Executors.newFixedThreadPool(nThreads)

    def close(): Unit = {
      service.shutdown()
    }

    def runInParallel[F](algorithm: Algorithm[F], problem: MutationAwarePseudoBooleanProblem[F], times: Int): Seq[Seq[Double]] = {
      val timeStart = System.nanoTime()
      val tasks = Array.fill[Callable[Seq[Double]]](times)(() => algorithm.solve(problem.newInstance))
      val rv = service.invokeAll(Arrays.asList(tasks :_*)).asScala.map(_.get()).toIndexedSeq
      val timeDone = (System.nanoTime() - timeStart) / 1e9
      println(s"  [$times runs done in $timeDone s]")
      rv
    }

    def compute[F](
      cacheRoot: String,
      algorithm: Algorithm[F],
      problem: MutationAwarePseudoBooleanProblem[F],
      times: Int
    ): Seq[Seq[Double]] = {
      val path = Paths.get(cacheRoot, algorithm.name, problem.name)
      val metricCount = algorithm.metrics.size
      val previousResults = if (Files.exists(path)) {
        val lines = Files.lines(path)
        val okResults = Seq.newBuilder[Seq[Double]]
        var revisionString: String = null
        lines.forEach((t: String) => try {
          if (revisionString == null) {
            revisionString = t
          } else {
            val values = t.split(" ").filter(_.nonEmpty).map(_.toDouble)
            if (values.length == metricCount) {
              okResults += values.toIndexedSeq
            }
          }
        } catch {
          case e: Throwable => println(s"An exception of type ${e.getClass} thrown when processing string '$t'")
        })
        if (revisionString != algorithm.revision) Seq() else okResults.result()
      } else Seq()
      if (previousResults.size < times) {
        val result = previousResults ++ runInParallel(algorithm, problem, times - previousResults.size)
        val resultSorted = result.sortBy(_.head)
        Files.createDirectories(path.getParent)
        Files.write(path, (algorithm.revision +: resultSorted.map(_.mkString(" "))).asJava)
        resultSorted
      } else {
        previousResults
      }
    }
  }

  class Statistics(data: Seq[Double]) {
    private final val sortedData = data.toIndexedSeq.sorted

    def percentile(ratio: Double): Double = {
      val index = (sortedData.size - 1) * ratio
      val indexD = math.floor(index).toInt
      val indexU = math.ceil(index).toInt
      if (indexD == indexU) {
        sortedData(indexD)
      } else {
        sortedData(indexD) * (indexU - index) + sortedData(indexU) * (index - indexD)
      }
    }

    lazy val min: Double = sortedData.head
    lazy val max: Double = sortedData.last
    lazy val std: Double = {
      val avg = mean
      val sumSqDiff = sortedData.view.map(v => (v - avg) * (v - avg)).sum
      math.sqrt(sumSqDiff  / (sortedData.size - 1))
    }
    lazy val mean: Double = sortedData.sum / sortedData.size
    lazy val median: Double = percentile(0.5)
    lazy val interQuartile: Double = percentile(0.75) - percentile(0.25)

    def everything: String = {
      f"{$mean%.02f \u00b1 $std%.02f, med = $median%.02f, iqr = $interQuartile%.02f}"
    }
  }
}
