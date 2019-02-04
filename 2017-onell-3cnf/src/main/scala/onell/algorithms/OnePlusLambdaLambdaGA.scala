package onell.algorithms

import java.util.concurrent.ThreadLocalRandom

import onell.{Algorithm, Mutation, MutationAwarePseudoBooleanProblem}

import scala.annotation.tailrec

/**
  * The (1+(L,L))-GA by Doerr, Doerr, Ebel
  * with subsequent "implementation-aware" modifications.
  */
class OnePlusLambdaLambdaGA[
  @specialized(Specializable.BestOfBreed) T: Ordering
](val lambdaTuning: OnePlusLambdaLambdaGA.LambdaTuningFactory,
  val constantTuning: OnePlusLambdaLambdaGA.ConstantTuning = OnePlusLambdaLambdaGA.defaultTuning,
  evaluationLimit: Long = Long.MaxValue
) extends Algorithm[T] {
  // rev3: don't count ignored fitness evaluations.
  // rev4: restart mutation/crossover when neutral
  override def revision: String = "rev4"

  override def name: String = s"(1+LL)[${lambdaTuning.lambdaTuningDescription}]"
  override def pgfPlotLegend: String = lambdaTuning.lambdaTuningDescriptionLaTeX
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

  def solve[P <: MutationAwarePseudoBooleanProblem.Instance[T]](
    problem: P,
    trace: Option[OnePlusLambdaLambdaGA.Tracer[P]]
  ): Seq[Double] = {
    val rng = ThreadLocalRandom.current()
    val n = problem.problemSize
    val lambdaTuner = lambdaTuning.newTuning(n)
    val mutation = new Mutation(n, 0.5, rng)  // these probabilities will be
    val crossover = new Mutation(n, 0.5, rng) // overwritten in the loop.

    val individual = Array.fill(n)(rng.nextBoolean())
    var fitness = problem(individual)
    var iterations = 1L
    var evaluations = 1L
    var maxSeenLambda = lambdaTuner.lambda
    val firstChildDiff = Array.ofDim[Int](n)
    val secondChildDiff = Array.ofDim[Int](n)

    trace.foreach(_.trace(problem, individual, lambdaTuner.lambda, evaluations, iterations))

    while (!problem.isOptimumFitness(fitness) && math.max(evaluations, iterations) < evaluationLimit) {
      val lambda = lambdaTuner.lambda
      mutation.setProbability(constantTuning.mutationProbabilityQuotient * lambda / n)
      crossover.setProbability(constantTuning.crossoverProbabilityQuotient * 1 / lambda)

      val firstLambdaInt = math.max(1, (lambda * constantTuning.firstPopulationSizeQuotient).toInt)
      val secondLambdaInt = math.max(1, (lambda * constantTuning.secondPopulationSizeQuotient).toInt)

      val (firstChildFitness, firstChildDiffCount) = runFirstPhase(problem, individual, fitness, mutation, firstChildDiff, firstLambdaInt)
      evaluations += firstLambdaInt

      System.arraycopy(firstChildDiff, 0, secondChildDiff, 0, firstChildDiffCount)
      val (secondChildFitness, secondChildDiffCount, newEvaluations) = runSecondPhase(problem, individual, fitness, firstChildDiff, firstChildDiffCount,
          crossover, secondChildDiff, firstChildFitness, firstChildDiffCount, secondLambdaInt, 0)
      evaluations += newEvaluations

      val cmp = ord.compare(secondChildFitness, fitness)
      if (cmp == 0) {
        lambdaTuner.notifyChildIsEqual()
      } else if (cmp > 0) {
        lambdaTuner.notifyChildIsBetter()
      } else /* cmp < 0 */ {
        lambdaTuner.notifyChildIsWorse()
      }
      maxSeenLambda = math.max(maxSeenLambda, lambdaTuner.lambda)
      if (cmp >= 0) {
        fitness = secondChildFitness
        var i = 0
        while (i < secondChildDiffCount) {
          individual(secondChildDiff(i)) ^= true
          i += 1
        }
        trace.foreach(_.traceChange(problem, individual, lambdaTuner.lambda, evaluations, iterations, secondChildDiff, secondChildDiffCount))
      } else {
        trace.foreach(_.trace(problem, individual, lambdaTuner.lambda, evaluations, iterations))
      }
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
  trait Tracer[-P] {
    def trace(problem: P, individual: Array[Boolean], lambda: Double, evaluations: Long, iterations: Long): Unit
    def traceChange(problem: P, individual: Array[Boolean], lambda: Double, evaluations: Long, iterations: Long, diffFromPrevious: Array[Int], diffSize: Int): Unit
  }

  trait LambdaTuningFactory {
    def newTuning(n: Int): LambdaTuning
    def lambdaTuningDescription: String
    def lambdaTuningDescriptionLaTeX: String
  }

  trait LambdaTuning {
    def lambda: Double
    def notifyChildIsBetter(): Unit
    def notifyChildIsEqual(): Unit
    def notifyChildIsWorse(): Unit
  }

  class FixedLambda(theLambda: Double) extends LambdaTuningFactory {
    private val lambdaText = if (theLambda.toLong == theLambda) theLambda.toLong.toString else theLambda.toString
    private val theTuning = new LambdaTuning {
      override def lambda: Double = theLambda
      override def notifyChildIsBetter(): Unit = {}
      override def notifyChildIsEqual(): Unit = {}
      override def notifyChildIsWorse(): Unit = {}
    }

    override def newTuning(n: Int): LambdaTuning = theTuning
    override val lambdaTuningDescription: String = lambdaText
    override val lambdaTuningDescriptionLaTeX: String = s"$$\\lambda = $lambdaText$$"
  }

  abstract class DefaultAdaptiveLambda(limitText: String, limitTextLaTeX: String, onSuccess: Double, onFailure: Double) extends LambdaTuningFactory {
    override def newTuning(n: Int): LambdaTuning = new LambdaTuning {
      private[this] val maxLambda = generateLambdaLimit(n)
      private[this] var myLambda: Double = 1
      override def lambda: Double = myLambda
      override def notifyChildIsBetter(): Unit = myLambda = math.min(maxLambda, math.max(1, myLambda * onSuccess))
      override def notifyChildIsEqual(): Unit = notifyChildIsWorse()
      override def notifyChildIsWorse(): Unit = myLambda = math.min(maxLambda, math.max(1, myLambda * onFailure))
    }

    protected def generateLambdaLimit(n: Int): Double
    override val lambdaTuningDescription: String = s"1;$limitText"
    override val lambdaTuningDescriptionLaTeX: String = s"$$\\lambda \\le $limitTextLaTeX$$"
  }

  final val OneFifthOnSuccess = 1 / 1.5
  final val OneFifthOnFailure = math.pow(1.5, 0.25)

  def adaptiveDefault(onSuccess: Double = OneFifthOnSuccess, onFailure: Double = OneFifthOnFailure): LambdaTuningFactory = {
    new DefaultAdaptiveLambda("n", "n", onSuccess, onFailure) {
      override protected def generateLambdaLimit(n: Int): Double = n
    }
  }

  def adaptiveLog(onSuccess: Double = OneFifthOnSuccess, onFailure: Double = OneFifthOnFailure): LambdaTuningFactory = {
    new DefaultAdaptiveLambda("ln n", "2 \\ln n", onSuccess, onFailure) {
      override protected def generateLambdaLimit(n: Int): Double = 2 * math.log(n + 1)
    }
  }

  case class ConstantTuning(mutationProbabilityQuotient: Double,
                            crossoverProbabilityQuotient: Double,
                            firstPopulationSizeQuotient: Double,
                            secondPopulationSizeQuotient: Double)
  val defaultTuning = ConstantTuning(1.0, 1.0, 1.0, 1.0)
}
