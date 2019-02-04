package onell.problems

import onell.{Mutation, MutationAwarePseudoBooleanProblem}

/**
  * The OneMax problem implemented as a mutation-aware pseudo-Boolean problem.
  */
class OneMax(n: Int)
  extends MutationAwarePseudoBooleanProblem.WithDistanceToOptimum[Int]
  with MutationAwarePseudoBooleanProblem.Instance[Int]
  with MutationAwarePseudoBooleanProblem.DistanceToAllOnes
{
  override type InstanceType = this.type
  override def newInstance: InstanceType = this

  override def name: String = s"OneMax($n)"
  override def isOptimumFitness(fitness: Int): Boolean = fitness == n

  override def problemSize: Int = n
  override def apply(solution: Array[Boolean]): Int = (0 until n).count(solution)
  override def apply(solution: Array[Boolean], originalFitness: Int, mutation: Mutation): Int = {
    var newFitness = originalFitness
    for (i <- mutation) {
      if (solution(i)) {
        newFitness -= 1
      } else {
        newFitness += 1
      }
    }
    mutation.mutate(solution)
    newFitness
  }
}
