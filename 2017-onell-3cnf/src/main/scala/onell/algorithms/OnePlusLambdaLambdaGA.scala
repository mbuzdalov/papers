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
  val pgfPlotLegend: String = "$\\lambda \\le n$"
) extends Algorithm[Int] {
  private[this] final val tuningStrength = 1.5
  private[this] final val tuningStrength4 = math.pow(tuningStrength, 0.25)

  // last change: if the resulting individual is equal to x, don't think it is good
  override def revision: String = "rev2"

  override def name: String = s"(1+LL)[$minimalLambdaText;$maximalLambdaText]"
  override def metrics: Seq[String] = Seq("Fitness evaluations", "Iterations", "Maximal lambda")
  override def solve(problem: MutationAwarePseudoBooleanProblem.Instance[Int]): Seq[Double] = solve(problem, None)

  def solve(
    problem: MutationAwarePseudoBooleanProblem.Instance[Int],
    trace: Option[(Array[Boolean], Double) => Unit]
  ): Seq[Double] = {
    val rng = ThreadLocalRandom.current()
    val n = problem.problemSize
    val mutation = new Mutation(n, minimalLambda / n, rng)
    val crossover = new Mutation(n, 1 / minimalLambda, rng)

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

    while (!problem.isOptimumFitness(fitness)) {
      mutation.setProbability(lambda / n)
      crossover.setProbability(1 / lambda)

      val lambdaInt = lambda.toInt
      var bestFirstChildFitness = -1
      var t = 0
      while (t < lambdaInt) {
        mutation.createRandomBits(t != 0)
        if (mutation.size != 0) {
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
      while (t < lambdaInt) {
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
        math.max(minimalLambda, lambda / tuningStrength)
      } else {
        math.min(math.min(n, maximalLambda), lambda * tuningStrength4)
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
      evaluations += 2 * lambdaInt
      iterations += 1
    }

    Seq(evaluations, iterations, maxSeenLambda)
  }
}
