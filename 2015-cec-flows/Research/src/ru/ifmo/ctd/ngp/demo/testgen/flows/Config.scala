package ru.ifmo.ctd.ngp.demo.testgen.flows

import java.io.PrintWriter
import java.lang.{Long => JLong}
import java.util.{Map => JMap}

import scala.collection.JavaConversions.asJavaIterable

import ru.ifmo.ctd.ngp.opt._
import ru.ifmo.ctd.ngp.opt.termination.{Crash, EvaluationLimit, CodomainThreshold}
import ru.ifmo.ctd.ngp.opt.iteration.{Mutation, Update, Selection}
import ru.ifmo.ctd.ngp.opt.listeners.{BestEvaluated, EvaluationCount}
import ru.ifmo.ctd.ngp.opt.event.IterationFinishedEvent

/**
 * A configuration for generating tests against maximum flow algorithms.
 * @author Maxim Buzdalov
 */
class Config(
  solver: MaxFlowSolver,
  maxV: Int,
  maxE: Int,
  maxC: Int,
  criterion: String,
  maxEvaluations: Int,
  generationSize: Int,
  useShiftCrossover: Boolean,
  increasingEdge: Boolean,
  output: PrintWriter
) extends OptConfiguration[IndexedSeq[EdgeRec], JMap[String, JLong]] {
  override implicit def sequenceExecutor = SequenceExecutor.scalaParallel

  @inline private def ne: EdgeRec = {
    val rng = random()
    if (increasingEdge) {
      val a, b = rng.nextInt(maxV)
      if (a == b) ne else new EdgeRec(math.min(a, b), math.max(a, b), rng.nextInt(maxC) + 1)
    } else {
      new EdgeRec(rng.nextInt(maxV), rng.nextInt(maxV), rng.nextInt(maxC) + 1)
    }
  }

  def crossoverTwo(first: IndexedSeq[EdgeRec], second: IndexedSeq[EdgeRec]) = {
    val fun = if (useShiftCrossover) {
      Mutation.Standard.Seq.twoPointCrossoverWithShift()
    } else {
      Mutation.Standard.Seq.singlePointCrossover()
    }
    val (a, b) = fun(first, second)
    IndexedSeq(a, b)
  }
  def mutate(source: IndexedSeq[EdgeRec]): IndexedSeq[EdgeRec] = {
    val rng = random()
    source.map(t => if (rng.nextInt(source.size) == 0) ne else t)
  }

  implicit val evaluator       = Evaluator().usingFunction(g => solver.solve(g, 0, maxV - 1, 5000))
  implicit val comparator      = CodomainComparator().by(_.get(criterion)).increasing
  implicit val evaluatedOrd    = comparator.evaluatedOrdering
  implicit val initialization  = Initialization().useDomainGenerator(IndexedSeq.fill(maxE)(ne), generationSize)
  implicit val termination     = Termination.Pluggable()
  implicit val selection       = Selection().tournamentOlympic(generationSize, 3, 0.9)
  implicit val update          = Update().elitist(0.1)
  implicit val mutation        = Mutation().usingDomainsToDomains {
    _.grouped(2).flatMap(t => if (t.size == 2) crossoverTwo(t(0), t(1)) else t).map(mutate).toIndexedSeq
  }
  implicit val iteration       = Iteration().fromSelectionMutationEvaluateUpdate
  implicit val optimizer       = Optimizer().simple
  implicit val evaluationCount = new EvaluationCount()
  implicit val bestEvaluated   = new BestEvaluated()

  CodomainThreshold().register(_.get("time").longValue(), Long.MaxValue)
  EvaluationLimit().register(maxEvaluations)

  IterationFinishedEvent().addListener { ws =>
    val bf = bestEvaluated().get.output
    output.println(s"${evaluationCount()} FF ${bf.get(criterion)} time ${bf.get("time")}")
  }

  def evolve() = {
    val Optimizer.Result(ws, reason) = optimizer()
    reason match {
      case Crash(th) => throw th
      case _ => ws.max
    }
  }
}
