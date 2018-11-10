package onell.algorithms

import java.util.concurrent.ThreadLocalRandom

import onell.{Algorithm, Mutation, MutationAwarePseudoBooleanProblem}

import scala.annotation.tailrec

/**
  * The (1+(L,L))-GA by Doerr, Doerr, Ebel
  * with subsequent "implementation-aware" modifications.
  */
class OnePlusLambdaLambdaGA[@specialized(Specializable.BestOfBreed) T: Ordering](
  minimalLambda: Double = 1,
  minimalLambdaText: String = "1",
  maximalLambda: Double = Double.PositiveInfinity,
  maximalLambdaText: String = "n",
  val pgfPlotLegend: String = "$\\lambda \\le n$",
  val tuning: OnePlusLambdaLambdaGA.ConstantTuning = OnePlusLambdaLambdaGA.defaultTuning,
  evaluationLimit: Long = Long.MaxValue,
  useAutoTune: Boolean = false
) extends Algorithm[T] {
  // rev3: don't count ignored fitness evaluations.
  // rev4: restart mutation/crossover when neutral
  override def revision: String = "rev4"

  override def name: String = s"(1+LL)[$minimalLambdaText;$maximalLambdaText]"
  override def metrics: Seq[String] = Seq("Fitness evaluations", "Iterations", "Maximal lambda")
  override def solve(problem: MutationAwarePseudoBooleanProblem.Instance[T]): Seq[Double] = solve(problem, None)

  private[this] val ord = implicitly[Ordering[T]]

  @inline
  @tailrec
  private[this] def runFirstPhaseEtc(problem: MutationAwarePseudoBooleanProblem.Instance[T],
                                     individual: Array[Boolean],
                                     fitness: T,
                                     mutation: Mutation,
                                     diff: Array[Int], remainingCount: Int, bestFitness: T, bestDiffCount: Int): (T, Int) = {
    if (remainingCount == 0) {
      (bestFitness, bestDiffCount)
    } else {
      mutation.createRandomBits(true)
      val newFitness = problem(individual, fitness, mutation)
      mutation.undo(individual)
      if (ord.gt(newFitness, bestFitness)) {
        runFirstPhaseEtc(problem, individual, fitness, mutation, diff, remainingCount - 1, newFitness, mutation.fill(diff))
      } else {
        runFirstPhaseEtc(problem, individual, fitness, mutation, diff, remainingCount - 1, bestFitness, bestDiffCount)
      }
    }
  }


  @inline
  @tailrec
  private[this] def runFirstPhase(problem: MutationAwarePseudoBooleanProblem.Instance[T],
                                  individual: Array[Boolean],
                                  fitness: T,
                                  mutation: Mutation, diff: Array[Int], count: Int): (T, Int) = {
    mutation.createRandomBits(false)
    if (mutation.size == 0) {
      runFirstPhase(problem, individual, fitness, mutation, diff, count)
    } else {
      val newFitness = problem(individual, fitness, mutation)
      mutation.undo(individual)
      val diffCount = mutation.fill(diff)
      runFirstPhaseEtc(problem, individual, fitness, mutation, diff, count - 1, newFitness, diffCount)
    }
  }

  @inline
  @tailrec
  private[this] def runSecondPhase(problem: MutationAwarePseudoBooleanProblem.Instance[T],
                                   individual: Array[Boolean],
                                   fitness: T,
                                   mutationDiff: Array[Int], mutationDiffCount: Int,
                                   crossover: Mutation, crossoverDiff: Array[Int],
                                   bestFitness: T, bestDiffCount: Int,
                                   remainingCount: Int, evaluationsDone: Int): (T, Int, Int) = {
    if (remainingCount == 0) {
      (bestFitness, bestDiffCount, evaluationsDone)
    } else {
      crossover.chooseRandomBits(mutationDiff, mutationDiffCount)
      val crossoverSize = crossover.size
      if (crossoverSize == 0) {
        runSecondPhase(problem, individual, fitness, mutationDiff, mutationDiffCount,
          crossover, crossoverDiff, bestFitness, bestDiffCount, remainingCount, evaluationsDone)
      } else if (crossoverSize == mutationDiffCount) {
        // this is about taking the entire child;
        // in turn, this never improves bestFitness/bestDiffCount as they are initialized with the entire child.
        // however, we have to decrease remainingCount there, as otherwise it can be too long to fall through
        runSecondPhase(problem, individual, fitness, mutationDiff, mutationDiffCount,
          crossover, crossoverDiff, bestFitness, bestDiffCount, remainingCount - 1, evaluationsDone)
      } else {
        val newFitness = problem(individual, fitness, crossover)
        crossover.undo(individual)
        if (ord.gt(newFitness, bestFitness)) {
          runSecondPhase(problem, individual, fitness, mutationDiff, mutationDiffCount,
            crossover, crossoverDiff, newFitness, crossover.fill(crossoverDiff), remainingCount - 1, evaluationsDone + 1)
        } else {
          runSecondPhase(problem, individual, fitness, mutationDiff, mutationDiffCount,
            crossover, crossoverDiff, bestFitness, bestDiffCount, remainingCount - 1, evaluationsDone + 1)
        }
      }
    }
  }

  def solve(
    problem: MutationAwarePseudoBooleanProblem.Instance[T],
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
    val secondChildDiff = Array.ofDim[Int](n)
    var failedIterations = 0L

    trace.foreach(f => f(individual, lambda))

    while (!problem.isOptimumFitness(fitness) && math.max(evaluations, iterations) < evaluationLimit) {
      mutation.setProbability(tuning.mutationProbabilityQuotient * lambda / n)
      crossover.setProbability(tuning.crossoverProbabilityQuotient * 1 / lambda)

      val firstLambdaInt = math.max(1, (lambda * tuning.firstPopulationSizeQuotient).toInt)
      val secondLambdaInt = math.max(1, (lambda * tuning.secondPopulationSizeQuotient).toInt)

      val (firstChildFitness, firstChildDiffCount) = runFirstPhase(problem, individual, fitness, mutation, firstChildDiff, firstLambdaInt)
      evaluations += firstLambdaInt

      System.arraycopy(firstChildDiff, 0, secondChildDiff, 0, firstChildDiffCount)
      val (secondChildFitness, secondChildDiffCount, newEvaluations) = runSecondPhase(problem, individual, fitness, firstChildDiff, firstChildDiffCount,
          crossover, secondChildDiff, firstChildFitness, firstChildDiffCount, secondLambdaInt, 0)
      evaluations += newEvaluations

      lambda = if (ord.gt(secondChildFitness, fitness)) {
        failedIterations = 0
        math.max(minimalLambda, lambda * tuning.tuningMultipleOnSuccess)
      } else {
        failedIterations += 1
        val correctedQuot = if (useAutoTune) {
          val extraPow = math.pow(0.5, math.max(0, failedIterations - 4))
          math.pow(tuning.tuningMultipleOnFailure, extraPow)
        } else {
          tuning.tuningMultipleOnFailure
        }
        math.min(math.min(n, maximalLambda), lambda * correctedQuot)
      }
      maxSeenLambda = math.max(maxSeenLambda, lambda)
      if (ord.gteq(secondChildFitness, fitness)) {
        fitness = secondChildFitness
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
