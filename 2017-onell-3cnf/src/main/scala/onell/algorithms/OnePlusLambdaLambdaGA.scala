package onell.algorithms

import java.util.concurrent.ThreadLocalRandom

import onell.{Algorithm, Mutation, MutationAwarePseudoBooleanProblem}

/**
  * The (1+(L,L))-GA by Doerr, Doerr, Ebel.
  */
class OnePlusLambdaLambdaGA(
  minimalLambda: Double = 1,
  minimalLambdaText: String = "1",
  maximalLambda: Double = Double.PositiveInfinity,
  maximalLambdaText: String = "n",
  val pgfPlotLegend: String = "$\\lambda \\le n$",
  val tuning: OnePlusLambdaLambdaGA.ConstantTuning = OnePlusLambdaLambdaGA.defaultTuning,
  evaluationLimit: Long = Long.MaxValue
) extends Algorithm[Int] {
  // last change: don't count ignored fitness evaluations.
  override def revision: String = "rev3"

  override def name: String = s"(1+LL)[$minimalLambdaText;$maximalLambdaText]"
  override def metrics: Seq[String] = Seq("Fitness evaluations", "Iterations", "Maximal lambda")
  override def solve(problem: MutationAwarePseudoBooleanProblem.Instance[Int]): Seq[Double] = solve(problem, None)

  def solve(
    problem: MutationAwarePseudoBooleanProblem.Instance[Int],
    trace: Option[(Array[Boolean], Double) => Unit]
  ): Seq[Double] = {
    val rng = ThreadLocalRandom.current()
    val n = problem.problemSize
    val mutation = new Mutation(n, tuning.mutationProbabilityQuotient * minimalLambda / n, rng)
    val crossover = new Mutation(n, tuning.crossoverProbabilityQuotient * 1 / minimalLambda, rng)

    val individual = Array.fill(n)(rng.nextBoolean())
    var fitness = problem(individual)
    var iterations = 1L
    var evaluations = 1L
    var lambda = minimalLambda
    var maxSeenLambda = lambda
    val firstChildDiff = Array.ofDim[Int](n)
    var firstChildDiffCount = 0
    val secondChildDiff = Array.ofDim[Int](n)
    var secondChildDiffCount = 0

    trace.foreach(f => f(individual, lambda))

    while (!problem.isOptimumFitness(fitness) && math.max(evaluations, iterations) < evaluationLimit) {
      mutation.setProbability(tuning.mutationProbabilityQuotient * lambda / n)
      crossover.setProbability(tuning.crossoverProbabilityQuotient * 1 / lambda)

      val firstLambdaInt = math.max(1, (lambda * tuning.firstPopulationSizeQuotient).toInt)
      val secondLambdaInt = math.max(1, (lambda * tuning.secondPopulationSizeQuotient).toInt)
      var bestFirstChildFitness = -1
      var t = 0
      while (t < firstLambdaInt) {
        mutation.createRandomBits(t != 0)
        if (mutation.size != 0) {
          evaluations += 1
          val firstChildFitness = problem(individual, fitness, mutation)
          if (firstChildFitness > bestFirstChildFitness) {
            firstChildDiffCount = mutation.fill(firstChildDiff)
            bestFirstChildFitness = firstChildFitness
          }
          mutation.undo(individual)
        }
        t += 1
      }
      var bestSecondChildFitness = -1
      t = 0
      while (t < secondLambdaInt) {
        crossover.chooseRandomBits(firstChildDiff, firstChildDiffCount)
        if (crossover.size != 0) {
          if (crossover.size == firstChildDiffCount) {
            // this is the same as applying the entire mutation back
            // the fitness would be `bestFirstChildFitness`
            if (bestFirstChildFitness > bestSecondChildFitness) {
              bestSecondChildFitness = bestFirstChildFitness
              secondChildDiffCount = firstChildDiffCount
              System.arraycopy(firstChildDiff, 0, secondChildDiff, 0, secondChildDiffCount)
            }
          } else {
            evaluations += 1
            val secondChildFitness = problem(individual, fitness, crossover)
            if (secondChildFitness > bestSecondChildFitness) {
              secondChildDiffCount = crossover.fill(secondChildDiff)
              bestSecondChildFitness = secondChildFitness
            }
            crossover.undo(individual)
          }
        }
        t += 1
      }
      lambda = if (bestSecondChildFitness > fitness) {
        math.max(minimalLambda, lambda * tuning.tuningMultipleOnSuccess)
      } else {
        math.min(math.min(n, maximalLambda), lambda * tuning.tuningMultipleOnFailure)
      }
      maxSeenLambda = math.max(maxSeenLambda, lambda)
      if (bestSecondChildFitness >= fitness) {
        fitness = bestSecondChildFitness
        var i = 0
        while (i < secondChildDiffCount) {
          individual(secondChildDiff(i)) ^= true
          i += 1
        }
      }
      trace.foreach(f => f(individual, lambda))
      iterations += 1
    }

    if (problem.isOptimumFitness(fitness)) {
      Seq(evaluations, iterations, maxSeenLambda)
    } else {
      Seq(Double.PositiveInfinity, Double.PositiveInfinity, maxSeenLambda)
    }
  }
}

object OnePlusLambdaLambdaGA {
  case class ConstantTuning(mutationProbabilityQuotient: Double,
                            crossoverProbabilityQuotient: Double,
                            firstPopulationSizeQuotient: Double,
                            secondPopulationSizeQuotient: Double,
                            tuningMultipleOnSuccess: Double,
                            tuningMultipleOnFailure: Double)
  val defaultTuning = ConstantTuning(1.0, 1.0, 1.0, 1.0, 1 / 1.5, math.pow(1.5, 0.25))
}
