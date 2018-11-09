package onell.algorithms

import java.util.concurrent.ThreadLocalRandom

import onell.{Algorithm, Mutation, MutationAwarePseudoBooleanProblem}

/**
  * The (1+1)-EA algorithm.
  */
class OnePlusOneEA[@specialized(Specializable.BestOfBreed) T: Ordering] extends Algorithm[T] {
  override def pgfPlotLegend: String = "(1+1) EA"
  override def name: String = "(1+1)-EA"
  override def revision: String = "rev2"
  override def metrics: Seq[String] = Seq("Fitness evaluations")
  override def solve(problem: MutationAwarePseudoBooleanProblem.Instance[T]): Seq[Double] = {
    val ord = implicitly[Ordering[T]]

    val n = problem.problemSize
    val rng = ThreadLocalRandom.current()
    val mutation = new Mutation(n, 1.0 / n, rng)

    val individual = Array.fill(n)(rng.nextBoolean())
    var evaluations = 1L
    var fitness = problem(individual)

    while (!problem.isOptimumFitness(fitness)) {
      mutation.createRandomBits(false)
      if (mutation.size > 0) {
        val newFitness = problem(individual, fitness, mutation)
        evaluations += 1
        if (ord.gteq(newFitness, fitness)) {
          fitness = newFitness
        } else {
          mutation.undo(individual)
        }
      }
    }

    Seq(evaluations)
  }
}
