package knapsack

import scala.collection.JavaConverters._
import scala.util.{Try, Failure, Success}

import ru.ifmo.ctd.ngp.opt._
import ru.ifmo.ctd.ngp.opt.iteration.{Mutation, Update, Selection}
import ru.ifmo.ctd.ngp.opt.listeners._
import ru.ifmo.ctd.ngp.opt.termination.EvaluationLimit
import ru.ifmo.ctd.ngp.opt.Evaluated

/**
 * A configuration for evolutionary test generation for knapsack problem.
 *
 * @author Maxim Buzdalov
 */
abstract class Config(generationSize: Int, limits: KnapsackRunLimits, solver: KnapsackSolver)
  extends OptConfiguration[List[Item], Long]
{
  def newElement(): Item
  def mutateElement(item: Item): Item

  private def crossoverTwo(first: domain.Type, second: domain.Type) = {
    val rng = random()
    val xSize = 1 + rng.nextInt(math.min(first.size, second.size) - 1)
    val fOffset = rng.nextInt(first.size - xSize + 1)
    val sOffset = rng.nextInt(second.size - xSize + 1)
    val (fP, fR) = first.splitAt(fOffset)
    val (fI, fS) = fR.splitAt(xSize)
    val (sP, sR) = second.splitAt(sOffset)
    val (sI, sS) = sR.splitAt(xSize)
    (fP ::: sI ::: fS, sP ::: fI ::: sS)
  }

  private def mutate(source: domain.Type) = {
    val ar = source.toArray
    val rng = random()
    do {
      val idx = rng.nextInt(ar.length)
      ar(idx) = mutateElement(ar(idx))
    } while (rng.nextBoolean())
    ar.toList
  }

  implicit val comparator = CodomainComparator().byComparable.increasing
  implicit val evaluator  = Evaluator().usingFunction { (genotype: List[Item]) =>
    val problem = new ProblemInstance(genotype.asJava)
    Try {
      solver.solve(problem)
    } match {
      case Success(result) => result.operationCount
      case Failure(th) => throw new AssertionError("Unexpected runtime error: " + th.getMessage)
    }
  }
  implicit lazy val initialization  = Initialization().useDomainGenerator(List.fill(limits.maxN)(newElement()), generationSize)
  implicit val selection       = Selection().tournamentOlympic(generationSize, 3, 0.9)
  implicit val update          = Update().elitist(0.2)
  implicit val mutation        = Mutation().usingCrossoverTwoAndMutation(crossoverTwo, 1.0, mutate, 0.1)
  implicit val iteration       = Iteration().fromSelectionMutationEvaluateUpdate
  implicit val termination     = Termination.Pluggable()
  implicit val optimizer       = Optimizer().simple
  implicit val bestEvaluated   = new BestEvaluated()
  implicit val iterationCount  = new IterationCount()
  implicit val evaluationCount = new EvaluationCount()
  implicit val iterationLast   = new IterationCountSinceBestUpdate()
  implicit val wallTime        = new TimeFromStart(resetAtInitialization = false)

  EvaluationLimit().register(limits.evaluationLimit)

  def runEvolution() = {
    optimizer() match {
      case Optimizer.Result(_, EvaluationLimit) =>
        val ev = bestEvaluated().get
        Config.Result(ev.input, ev.output)
      case _ => throw new AssertionError()
    }
  }

  def runRandom() = {
    type Ev = Evaluated[domain.Type, codomain.Type]
    var best: Option[Ev] = None
    var count = 0L
    while (count < limits.evaluationLimit) {
      initialization().foreach { v =>
        count += 1
        best = best match {
          case None => Some(v)
          case Some(x) => if (comparator.evaluatedOrdering.compare(x, v) > 0) best else Some(v)
        }
      }

    }
    Config.Result(best.get.input, best.get.output)
  }
}

object Config {
  case class Result(genotype: List[Item], fitness: Long)
  private def bound(value: Int, min: Int, max: Int) = math.max(math.min(value, max), min)

  class TwoEqual(generationSize: Int, limits: KnapsackRunLimits, solver: KnapsackSolver)
    extends Config(generationSize, limits, solver)
  {
    import limits._

    override lazy val initialization = Initialization()(domain, codomain, workingSet).useDomainGenerator({
      val rng = random()
      val n1 = rng.nextInt(maxN - 1) + 1
      val e1 = new Item(1 + rng.nextInt(maxWeight), 1 + rng.nextInt(maxValue))
      val e2 = new Item(1 + rng.nextInt(maxWeight), 1 + rng.nextInt(maxValue))
      List.fill(n1)(e1) ::: List.fill(maxN - n1)(e2)
    }, generationSize)
    override def newElement(): Item = throw new UnsupportedOperationException("")
    override def mutateElement(item: Item): Item = throw new UnsupportedOperationException("")
  }

  class AnyData(generationSize: Int, limits: KnapsackRunLimits, solver: KnapsackSolver)
    extends Config(generationSize, limits, solver)
  {
    import limits.maxWeight
    import limits.maxValue
    def newElement() = new Item(1 + random().nextInt(maxWeight), 1 + random().nextInt(maxValue))
    def mutateElement(item: Item) = {
      new Item(
        bound(item.weight + (maxWeight * random().nextGaussian() / 3).toInt, 1, maxWeight),
        bound(item.value + (maxValue * random().nextGaussian() / 3).toInt, 1, maxValue)
      )
    }
  }

  class SubsetSum(generationSize: Int, limits: KnapsackRunLimits, solver: KnapsackSolver)
    extends Config(generationSize, limits, solver)
  {
    private val max = math.min(limits.maxWeight, limits.maxValue)
    def newElement() = {
      val wv = 1 + random().nextInt(max)
      new Item(wv, wv)
    }
    def mutateElement(item: Item) = {
      val wv = bound(item.weight + (max * random().nextGaussian() / 3).toInt, 1, max)
      new Item(wv, wv)
    }
  }

  class StronglyCorrelated(generationSize: Int, limits: KnapsackRunLimits, solver: KnapsackSolver)
    extends Config(generationSize, limits, solver)
  {
    private val max = math.min(limits.maxWeight, limits.maxValue)
    def newElement() = {
      val wv = 1 + random().nextInt(max - 5)
      new Item(wv, wv + 5)
    }
    def mutateElement(item: Item) = {
      val wv = bound(item.weight + (max * random().nextGaussian() / 3).toInt, 1, max - 5)
      new Item(wv, wv + 5)
    }
  }
}
