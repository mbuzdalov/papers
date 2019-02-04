package onell

/**
  * An optimization problem which knows how to recompute the answer faster on small mutations.
  * @tparam F the type of the fitness value.
  */
trait MutationAwarePseudoBooleanProblem[@specialized(Specializable.BestOfBreed) F] {
  type InstanceType <: MutationAwarePseudoBooleanProblem.Instance[F]
  /**
    * Returns a new instance of this problem.
    * @return a new instance of this problem.
    */
  def newInstance: InstanceType

  /**
    * Returns the name of the problem.
    * @return the name of the problem.
    */
  def name: String
}

object MutationAwarePseudoBooleanProblem {
  trait WithDistanceToOptimum[@specialized(Specializable.BestOfBreed) F] extends MutationAwarePseudoBooleanProblem[F] {
    override type InstanceType <: MutationAwarePseudoBooleanProblem.Instance[F] with HasDistanceToOptimum
  }

  /**
    * An interface for an instance of the problem.
    *
    * @tparam F the type of the fitness value.
    */
  trait Instance[@specialized(Specializable.BestOfBreed) F] {

    /**
      * Checks if the fitness value corresponds to the problem's optimum.
      *
      * @return the optimum fitness value.
      */
    def isOptimumFitness(fitness: F): Boolean

    /**
      * Returns the number of bits in the candidate solution to the problem.
      *
      * @return the number of bits in the solution.
      */
    def problemSize: Int

    /**
      * Evaluates the fitness value of the given candidate solution.
      *
      * @param solution the candidate solution.
      * @return the fitness of the given solution.
      */
    def apply(solution: Array[Boolean]): F

    /**
      * Evaluates the fitness value of the given candidate solution after applying the given mutation.
      * The original fitness value is also supplied.
      *
      * @param solution        the candidate solution before the mutation. Will be changed (the mutation will be applied).
      * @param originalFitness the fitness of the candidate solution before the mutation.
      * @param mutation        the mutation to be applied.
      * @return the fitness after applying the mutation.
      */
    def apply(solution: Array[Boolean], originalFitness: F, mutation: Mutation): F
  }

  trait HasDistanceToOptimum {
    def distanceToOptimum(solution: Array[Boolean]): Int
    def distanceToOptimum(solution: Array[Boolean], prevDistance: Int, changeIndices: Array[Int], changeSize: Int): Int
  }

  trait DistanceToAllOnes extends HasDistanceToOptimum {
    override def distanceToOptimum(solution: Array[Boolean]): Int = solution.count(v => !v)

    override def distanceToOptimum(solution: Array[Boolean], prevDistance: Int,
                                   changeIndices: Array[Int], changeSize: Int): Int = {
      var rv = prevDistance
      var i = 0
      while (i < changeSize) {
        if (solution(changeIndices(i))) {
          rv -= 1
        } else {
          rv += 1
        }
        i += 1
      }
      rv
    }
  }
}
