package onell

import java.io.PrintWriter
import java.util.Locale

import onell.algorithms.OnePlusLambdaLambdaGA._
import onell.algorithms.{OnePlusLambdaLambdaGA, OnePlusOneEA, RLS}
import onell.problems.{Linear, Random3CNF}

object MainLinear {
  abstract class AbstractBasinFactory(limitText: String, limitTextLaTeX: String) extends LambdaTuningFactory {
    private val tuningMultipleOnSuccess = OnePlusLambdaLambdaGA.OneFifthOnSuccess
    private val tuningMultipleOnFailure = OnePlusLambdaLambdaGA.OneFifthOnFailure

    override def newTuning(n: Int): LambdaTuning = new LambdaTuning {
      private[this] val lambdaLimit = generateLambdaLimit(n)
      private[this] var myLambda: Double = 1
      private[this] var baseLambda: Double = 1
      private[this] var continuousFailedIterations, delta = 0L

      override def lambda: Double = myLambda

      override def notifyChildIsEqual(): Unit = notifyChildIsWorse()
      override def notifyChildIsBetter(): Unit = {
        continuousFailedIterations = 0
        delta = 10
        myLambda = math.max(1, myLambda * tuningMultipleOnSuccess)
        baseLambda = myLambda
      }
      override def notifyChildIsWorse(): Unit = {
        continuousFailedIterations += 1
        if (continuousFailedIterations == delta) {
          delta += 1
          continuousFailedIterations = 0
        }
        myLambda = math.min(lambdaLimit, baseLambda * math.pow(tuningMultipleOnFailure, continuousFailedIterations))
      }
    }

    protected def generateLambdaLimit(n: Int): Double
    override def lambdaTuningDescription: String = s"1;$limitText*"
    override def lambdaTuningDescriptionLaTeX: String = s"$$\\lambda \\le $limitTextLaTeX*$$"
  }

  object LinearBasinFactory extends AbstractBasinFactory("n", "n") {
    override protected def generateLambdaLimit(n: Int): Double = n
  }

  object LogarithmicBasinFactory extends AbstractBasinFactory("ln n", "2 \\ln n") {
    override protected def generateLambdaLimit(n: Int): Double = 2 * math.log(n + 1)
  }

  def demo(): Unit = {
    def getStats[@specialized(Int, Long) T](problem: MutationAwarePseudoBooleanProblem[T], algo: => Algorithm[T]): (String, Double) = {
      val runs = (0 until 100).par.map { _ =>
        val instance = problem.newInstance
        algo.solve(instance).head
      }
      val avg = runs.sum / runs.size
      val std = math.sqrt(runs.map(v => (v - avg) * (v - avg)).sum / (runs.size - 1))
      (f"$avg%.2f ± $std%.2f", avg)
    }

    def runProblem[@specialized(Int, Long) T](problem: MutationAwarePseudoBooleanProblem[T])(implicit ord: Ordering[T]): Unit = {
      val (rlsStr, _) = getStats(problem, new RLS)
      val (onePlusStr, _) = getStats(problem, new OnePlusOneEA)
      val (oneLLlStr, _) = getStats(problem, new OnePlusLambdaLambdaGA(adaptiveLog()))
      val (oneLLnStr, _) = getStats(problem, new OnePlusLambdaLambdaGA(adaptiveDefault()))
      val (oneLLlbStr, _) = getStats(problem, new OnePlusLambdaLambdaGA(LogarithmicBasinFactory))
      val (oneLLnbStr, _) = getStats(problem, new OnePlusLambdaLambdaGA(LinearBasinFactory))
      println(s" RLS: $rlsStr, (1+1) EA: $onePlusStr")
      print(s"               (1+(λ,λ)) GA(log, usual): $oneLLlStr")
      print(s", (1+(λ,λ)) GA(n, usual): $oneLLnStr")
      println()
      print(s"               (1+(λ,λ)) GA(log, Basin): $oneLLlbStr")
      print(s", (1+(λ,λ)) GA(n, Basin): $oneLLnbStr")
      println()
    }

    for (n <- Seq(100, 200, 400, 800, 1600, 3200, 6400, 12800)) {
      for (w <- Seq(1, 2, 5, n, n * n)) {
        print(s"n = $n, w = $w:")
        runProblem(new Linear(n, w))
      }
      print(s"MAX-SAT n = $n:")
      runProblem(new Random3CNF(n, (4 * n * math.log(n)).toInt))
      println()
    }
  }

  class DistanceTracer(n: Int) extends Tracer[MutationAwarePseudoBooleanProblem.HasDistanceToOptimum] {
    private[this] val countRuns, sumRuns = new Array[Long](n + 1)
    private[this] var currentRunDistance: Int = _
    private[this] var currentRunEvaluationsStart: Long = _

    def getExpectation(d: Int): (Double, Long) = (sumRuns(d).toDouble / countRuns(d), countRuns(d))

    override def trace(problem: MutationAwarePseudoBooleanProblem.HasDistanceToOptimum,
                       individual: Array[Boolean], lambda: Double, evaluations: Long, iterations: Long): Unit = {
      if (evaluations == 1) {
        // initialization
        currentRunDistance = problem.distanceToOptimum(individual)
        currentRunEvaluationsStart = evaluations
      }
    }

    override def traceChange(problem: MutationAwarePseudoBooleanProblem.HasDistanceToOptimum,
                             individual: Array[Boolean], lambda: Double, evaluations: Long, iterations: Long,
                             diffFromPrevious: Array[Int], diffSize: Int): Unit = {
      val nextRunDistance = problem.distanceToOptimum(individual, currentRunDistance, diffFromPrevious, diffSize)
      if (nextRunDistance < currentRunDistance) {
        countRuns(currentRunDistance) += 1
        sumRuns(currentRunDistance) += evaluations - currentRunEvaluationsStart
        currentRunEvaluationsStart = evaluations
      }
      currentRunDistance = nextRunDistance
    }
  }

  private def printTracers(n: Int, tracers: Seq[DistanceTracer], out: PrintWriter): Unit = {
    for (d <- n / 2 to 1 by -1) {
      out.print(d)
      val expectations = Array.tabulate(tracers.length)(i => tracers(i).getExpectation(d))
      for (i <- tracers.indices) {
        val (exp, _) = expectations(i)
        out.print(",")
        out.print(exp)
      }
      out.println()
    }
  }

  def probabilities(): Unit = {
    val lambdas = (1 to 50).toIndexedSeq

    def run[@specialized(Int, Long) T](n: Int,
                                       problem: MutationAwarePseudoBooleanProblem.WithDistanceToOptimum[T],
                                       tracers: Seq[DistanceTracer])
                                      (implicit ord: Ordering[T]): Unit = {
      for (i <- lambdas.indices.par) {
        (0 until 1000) foreach { _ =>
          val solver = new OnePlusLambdaLambdaGA[T](new OnePlusLambdaLambdaGA.FixedLambda(lambdas(i)))
          solver.solve(problem.newInstance, Some(tracers(i)))
        }
      }
    }

    for (n <- Seq(1000)) {
      for (w <- Seq(1, 2, 5, n)) {
        val out = new PrintWriter(s"probabilities-$n-$w.txt")
        out.println(lambdas.map(v => s"L=$v").mkString("d,", ",", ""))
        println(s"Linear, n = $n, weights in [1; $w]")

        val problem = new Linear(n, w)
        val tracers = IndexedSeq.fill(lambdas.length)(new DistanceTracer(n))
        run(n, problem, tracers)
        printTracers(n, tracers, out)
        out.close()
      }

      val out = new PrintWriter(s"probabilities-$n-SAT.txt")
      val tracers = Array.fill(lambdas.length)(new DistanceTracer(n))
      println(s"MAX-SAT, n = $n")
      val problem = new Random3CNF(n, (4 * n * math.log(n)).toInt)
      run(n, problem, tracers)
      printTracers(n, tracers, out)
      out.close()
    }
  }

  def main(args: Array[String]): Unit = {
    Locale.setDefault(Locale.US)
    args(0) match {
      case "demo" => demo()
      case "probabilities" => probabilities()
    }
  }
}
