package onell.problems

import java.util.Arrays
import java.util.concurrent.ThreadLocalRandom

import onell.{Mutation, MutationAwarePseudoBooleanProblem}

class Linear(n: Int, maxWeight: Int) extends MutationAwarePseudoBooleanProblem[Long] {
  override def newInstance: MutationAwarePseudoBooleanProblem.Instance[Long] = new Linear.Instance(n, maxWeight)
  override def name: String = s"Linear($n,$maxWeight)"
}

object Linear {
  final class Instance(n: Int, maxWeight: Int) extends MutationAwarePseudoBooleanProblem.Instance[Long] {
    private[this] val weights = {
      val rv = Array.ofDim[Int](n)
      rv(0) = 1
      rv(1) = maxWeight
      var i = 2
      val rng = ThreadLocalRandom.current()
      while (i < n) {
        rv(i) = 1 + rng.nextInt(maxWeight)
        i += 1
      }
      Arrays.sort(rv)
      rv
    }

    private[this] val sumWeights = weights.view.map(_.toLong).sum

    override def isOptimumFitness(fitness: Long): Boolean = sumWeights == fitness
    override def problemSize: Int = n
    override def apply(solution: Array[Boolean]): Long = {
      var i = solution.length - 1
      var rv = 0L
      while (i >= 0) {
        if (solution(i)) rv += weights(i)
        i -= 1
      }
      rv
    }

    override def apply(solution: Array[Boolean], originalFitness: Long, mutation: Mutation): Long = {
      var rv = originalFitness
      for (i <- mutation) {
        if (solution(i)) {
          solution(i) = false
          rv -= weights(i)
        } else {
          solution(i) = true
          rv += weights(i)
        }
      }
      rv
    }
  }
}
