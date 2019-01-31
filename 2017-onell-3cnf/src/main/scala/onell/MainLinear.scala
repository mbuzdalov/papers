package onell

import java.util.Locale

import scala.collection.mutable.ArrayBuffer

import onell.algorithms.OnePlusLambdaLambdaGA._
import onell.algorithms.{OnePlusLambdaLambdaGA, OnePlusOneEA, RLS}
import onell.problems.Linear

object MainLinear {
  abstract class AbstractBasinFactory(limitText: String, limitTextLaTeX: String) extends LambdaTuningFactory {
    private val tuningMultipleOnSuccess = OnePlusLambdaLambdaGA.OneFifthOnSuccess
    private val tuningMultipleOnFailure = OnePlusLambdaLambdaGA.OneFifthOnFailure

    override def newTuning(n: Int): LambdaTuning = new LambdaTuning {
      private[this] val lambdaLimit = generateLambdaLimit(n)
      private[this] var myLambda: Double = 1
      private[this] var continuousFailedIterations = 0L
      private[this] val histLambdaArray = new ArrayBuffer[Double]
      private[this] var histLambdaIdx = -1
      private[this] var iterations = 0L

      override def lambda: Double = myLambda

      override def notifyChildIsEqual(): Unit = notifyChildIsWorse()
      override def notifyChildIsBetter(): Unit = {
        histLambdaArray += lambda
        histLambdaIdx = histLambdaArray.size
        continuousFailedIterations = 0
        iterations += 1
        myLambda = math.max(1, myLambda * tuningMultipleOnSuccess)
      }
      override def notifyChildIsWorse(): Unit = {
        continuousFailedIterations += 1
        iterations += 1
        myLambda = if (continuousFailedIterations > math.pow(iterations + 1, 0.3) && histLambdaArray.nonEmpty) {
          if (histLambdaIdx < 1) {
            histLambdaIdx = histLambdaArray.size
          }
          histLambdaIdx -= 1
          histLambdaArray(histLambdaIdx)
        } else {
          math.min(lambdaLimit, myLambda * tuningMultipleOnFailure)
        }
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

  def main(args: Array[String]): Unit = {
    Locale.setDefault(Locale.US)

    def getRLS(n: Int) = new RLS[Long]
    def getOnePlus(n: Int) = new OnePlusOneEA[Long]
    def getOneLLl(n: Int) = new OnePlusLambdaLambdaGA[Long](adaptiveLog())
    def getOneLLn(n: Int) = new OnePlusLambdaLambdaGA[Long](adaptiveDefault())
    def getOneLLnb(n: Int) = new OnePlusLambdaLambdaGA[Long](LinearBasinFactory)
    def getOneLLlb(n: Int) = new OnePlusLambdaLambdaGA[Long](LogarithmicBasinFactory)

    def getStats(problem: MutationAwarePseudoBooleanProblem[Long], algo: Int => Algorithm[Long]): (String, Double) = {
      val runs = (0 until 1000).par.map { _ =>
        val instance = problem.newInstance
        algo(instance.problemSize).solve(instance).head
      }
      val avg = runs.sum / runs.size
      val std = math.sqrt(runs.map(v => (v - avg) * (v - avg)).sum / (runs.size - 1))
      (f"$avg%.2f ± $std%.2f", avg)
    }

    for (n <- Seq(100, 200, 400, 800, 1600, 3200, 6400, 12800)) {
      for (w <- Seq(1, 2, 5, n, n * n)) {
        print(s"n = $n, w = $w:")
        val problem = new Linear(n, w)
        val (rlsStr, _) = getStats(problem, getRLS)
        val (onePlusStr, _) = getStats(problem, getOnePlus)
        val (oneLLlStr, _) = getStats(problem, getOneLLl)
        val (oneLLnStr, _) = getStats(problem, getOneLLn)
        val (oneLLlbStr, _) = getStats(problem, getOneLLlb)
        val (oneLLnbStr, _) = getStats(problem, getOneLLnb)
        println(s" RLS: $rlsStr, (1+1) EA: $onePlusStr")
        println(s"               (1+(λ,λ)) GA(log, usual): $oneLLlStr, (1+(λ,λ)) GA(n, usual): $oneLLnStr")
        println(s"               (1+(λ,λ)) GA(log, Basin): $oneLLlbStr, (1+(λ,λ)) GA(n, Basin): $oneLLnbStr")
      }
      println()
    }
  }
}
