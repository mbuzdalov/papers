package onell.problems

import java.util.Random
import java.util.concurrent.ThreadLocalRandom

import onell.util.ArrayIntSet
import onell.{Mutation, MutationAwarePseudoBooleanProblem}

import scala.annotation.tailrec

/**
  * A random planted-solution 3-CNF-SAT instance.
  */
class Random3CNF(n: Int, m: Int) extends MutationAwarePseudoBooleanProblem[Int] {
  override def name: String = s"Random3CNF($n,$m)"
  override def newInstance = new Random3CNF.Instance(n, m)
}

object Random3CNF {
  final class Instance(n: Int, m: Int) extends MutationAwarePseudoBooleanProblem.Instance[Int] {
    private[this] val assignment = Array.ofDim[Boolean](n)
    private[this] val clauseVar = Array.ofDim[Int](3 * m)
    private[this] val clauseVal = Array.ofDim[Boolean](3 * m)
    private[this] val clausesOfVarValues = Array.ofDim[Int](3 * m)
    private[this] val clausesOfVarIndices = Array.ofDim[Int](n + 1)
    private[this] val usedClauses = new ArrayIntSet(m)

    private[this] def isOk(clauseIndex: Int, solution: Array[Boolean]): Boolean = {
      val i1 = 3 * clauseIndex
      val i2 = i1 + 1
      val i3 = i2 + 1
      solution(clauseVar(i1)) == clauseVal(i1) || solution(clauseVar(i2)) == clauseVal(i2) || solution(clauseVar(i3)) == clauseVal(i3)
    }

    @tailrec
    private[this] def initAssignment(i: Int, n: Int, rng: Random): Unit = if (i < n) {
      assignment(i) = rng.nextBoolean()
      initAssignment(i + 1, n, rng)
    }
    @tailrec
    private[this] def initClause(i: Int, i1: Int, i2: Int, i3: Int, n: Int, rng: Random): Unit = {
      clauseVar(i1) = rng.nextInt(n)
      clauseVar(i2) = rng.nextInt(n)
      clauseVar(i3) = rng.nextInt(n)
      clauseVal(i1) = rng.nextBoolean()
      clauseVal(i2) = rng.nextBoolean()
      clauseVal(i3) = rng.nextBoolean()
      if (!isOk(i, assignment)) {
        initClause(i, i1, i2, i3, n, rng)
      } else {
        clausesOfVarIndices(clauseVar(i1)) += 1
        clausesOfVarIndices(clauseVar(i2)) += 1
        clausesOfVarIndices(clauseVar(i3)) += 1
      }
    }
    @tailrec
    private[this] def initClauses(i: Int, n: Int, m: Int, rng: Random): Unit = {
      if (i < m) {
        initClause(i, 3 * i, 3 * i + 1, 3 * i + 2, n, rng)
        initClauses(i + 1, n, m, rng)
      }
    }
    @tailrec
    private[this] def makePartialSums(i: Int, n: Int): Unit = if (i <= n) {
      clausesOfVarIndices(i) += clausesOfVarIndices(i - 1)
      makePartialSums(i + 1, n)
    }
    @tailrec
    private[this] def fillResultArrays(i: Int, m: Int): Unit = if (i < m) {
      val j1 = clauseVar(3 * i)
      val j2 = clauseVar(3 * i + 1)
      val j3 = clauseVar(3 * i + 2)
      clausesOfVarIndices(j1) -= 1
      clausesOfVarValues(clausesOfVarIndices(j1)) = i
      clausesOfVarIndices(j2) -= 1
      clausesOfVarValues(clausesOfVarIndices(j2)) = i
      clausesOfVarIndices(j3) -= 1
      clausesOfVarValues(clausesOfVarIndices(j3)) = i
      fillResultArrays(i + 1, m)
    }

    initAssignment(0, n, ThreadLocalRandom.current())
    initClauses(0, n, m, ThreadLocalRandom.current())
    makePartialSums(1, n)
    fillResultArrays(0, m)

    override def isOptimumFitness(fitness: Int): Boolean = fitness == m

    override def problemSize: Int = n

    def distance(solution: Array[Boolean]): Int = (0 until n).count(i => solution(i) != assignment(i))

    def distance(solution: Array[Boolean], prevDistance: Int, changedPositions: Array[Int], changedPositionCount: Int): Int = {
      var rv = prevDistance
      var i = 0
      while (i < changedPositionCount) {
        val index = changedPositions(i)
        if (solution(index) == assignment(index)) {
          rv -= 1
        } else {
          rv += 1
        }
        i += 1
      }
      rv
    }

    override def apply(solution: Array[Boolean]): Int = (0 until m).count(i => isOk(i, solution))

    override def apply(solution: Array[Boolean], originalFitness: Int, mutation: Mutation): Int = {
      if (3 * mutation.size < n) {
        usedClauses.clear()
        for (i <- mutation) {
          var j = clausesOfVarIndices(i)
          val jMax = clausesOfVarIndices(i + 1)
          while (j < jMax) {
            usedClauses += clausesOfVarValues(j)
            j += 1
          }
        }
        val fitnessDecrease = usedClauses.count(i => isOk(i, solution))
        mutation.mutate(solution)
        val fitnessIncrease = usedClauses.count(i => isOk(i, solution))
        originalFitness + fitnessIncrease - fitnessDecrease
      } else {
        // This path should be faster for very large mutations
        mutation.mutate(solution)
        apply(solution)
      }
    }
  }
}
