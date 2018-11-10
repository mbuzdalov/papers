package onell

import java.util.Locale

import onell.algorithms.{OnePlusLambdaLambdaGA, OnePlusOneEA}
import onell.problems.Linear

object MainLinear {
  def main(args: Array[String]): Unit = {
    Locale.setDefault(Locale.US)

    def getOnePlus(n: Int) = new OnePlusOneEA[Long]
    def getOneLLl(n: Int) = new OnePlusLambdaLambdaGA[Long](maximalLambda = 2 * math.log(n) + 1, maximalLambdaText = "ln n")
    def getOneLLn(n: Int) = new OnePlusLambdaLambdaGA[Long](maximalLambda = n, maximalLambdaText = "n")
    def getOneLLt(n: Int) = new OnePlusLambdaLambdaGA[Long](maximalLambda = n, maximalLambdaText = "n*", useAutoTune = true)

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
        val (onePlusStr, onePlusRes) = getStats(problem, getOnePlus)
        val (oneLLlStr, oneLLlRes) = getStats(problem, getOneLLl)
        val (oneLLnStr, oneLLnRes) = getStats(problem, getOneLLn)
        val (oneLLtStr, oneLLtRes) = getStats(problem, getOneLLt)
        println(s" (1+1) EA: $onePlusStr, (1+(λ,λ)) GA(log): $oneLLlStr, (1+(λ,λ)) GA(n): $oneLLnStr, (1+(λ,λ)) GA(n, auto): $oneLLtStr")
      }
      println()
    }
  }
}
