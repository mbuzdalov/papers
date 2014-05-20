package timus1394

import java.lang.{Integer => JInt}
import java.util.{List => JList}

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import scala.language.implicitConversions

import ru.ifmo.ctd.ngp.opt._
import ru.ifmo.ctd.ngp.opt.multicriteria.MultipleCriteria
import ru.ifmo.ctd.ngp.opt.iteration.{Update, Selection, Mutation}
import ru.ifmo.ctd.ngp.opt.termination.{IterationLimitSinceBestUpdate, IterationLimit, Crash, CodomainThreshold}
import ru.ifmo.ctd.ngp.opt.listeners.{IterationCountSinceBestUpdate, IterationCount, BestEvaluated}
import ru.ifmo.ctd.ngp.learning.reinforce.Agent
import ru.ifmo.ctd.ngp.opt.misc.earl.{EARLConfiguration, EARLCodomainComparator}
import ru.ifmo.ctd.ngp.opt.event.EvaluationFinishedEvent

import Config.{Result, Codomain}


/**
 * @author Maxim Buzdalov
 */
class Config(
  adapter: Adapter,
  testSize: Int,
  timeLimit: Long,
  useTimeoutChecker: Boolean
) extends OptConfiguration[IndexedSeq[Int], Codomain] {
  val criteria = "time" +: adapter.keys().toIndexedSeq.filterNot(_ == "time")

  private def toJListJInt(seq: Seq[Int]): JList[JInt] = seq.map(JInt.valueOf)

  def evaluate(in: IndexedSeq[Int]) = {
    val ships = {
      val shipsZ = in.filter(_ != 0).toArray
      if (shipsZ.length > 0) {
        val tmp = shipsZ(0)
        shipsZ(0) = shipsZ(shipsZ.length - 1)
        shipsZ(shipsZ.length - 1) = tmp
      }
      shipsZ.toIndexedSeq
    }
    val havens = {
      val builder = IndexedSeq.newBuilder[Int]
      var last = 0
      for (i <- in) {
        if (i == 0) {
          if (last > 0) builder += last
          last = 0
        } else {
          last += i
        }
      }
      if (last != 0) {
        builder += last
      }
      builder.result()
    }
    val fitness = if (havens.size < 2 || havens.size > 9 || ships.size < 2 || ships.size > 99) {
      IndexedSeq.fill(criteria.size)(0L)
    } else {
      val jMap = adapter.invoke(toJListJInt(ships), toJListJInt(havens), if (useTimeoutChecker) timeLimit else 0)
      IndexedSeq.tabulate(criteria.size)(i => jMap.get(criteria(i)).longValue())
    }
    Codomain(fitness, ships, havens)
  }

  def randomElement() = if (random().nextInt(5) == 0) 0 else random().nextInt(100) + 1
  def randomInput() = IndexedSeq.fill(testSize)(randomElement())

  def reward(s: Seq[Evaluated[IndexedSeq[Int], Codomain]]) = {
    def extract(v: Evaluated[IndexedSeq[Int], Codomain]) = v.output.values(0)
    s.view.map(extract).max
  }

  implicit val codomainIsSeqNumeric = Config.codomain2IndexedSeqLong _
  implicit val multiple             = MultipleCriteria.fromIndexedSeqOfNumeric(criteria :_*)
  implicit val mutation             = Mutation().usingCrossoverTwoAndMutation(
    Mutation.Standard.Seq.twoPointCrossoverWithShift(), 1.0,
    Mutation.Standard.Seq.independentPointMutation((i: Int) => randomElement(), 1.0 / testSize), 1.0
  )

  def runSingle(
    pw: java.io.PrintWriter,
    generationSize: Int,
    iterationLimit: Long,
    stagnationLimit: Long,
    eliteRate: Double,
    criterion: Int
  ): Result = {
    @tailrec
    def impl(iterationsSoFar: Long): Result = {
      implicit val evaluator       = Evaluator().usingFunction(evaluate)
      implicit val comparator      = multiple.projection(criterion)
      implicit val initialization  = Initialization().useDomainGenerator(randomInput(), generationSize)
      implicit val selection       = Selection().tournament(generationSize)
      implicit val update          = Update().elitist(eliteRate)
      implicit val iteration       = Iteration().fromSelectionMutationEvaluateUpdate
      implicit val termination     = Termination.Pluggable()
      implicit val bestEvaluated   = new BestEvaluated()
      implicit val iterationCount  = new IterationCount()
      implicit val iterationCount2 = new IterationCountSinceBestUpdate()

      CodomainThreshold().register(_.values(0), timeLimit)
      IterationLimit().register(iterationLimit - iterationsSoFar)
      IterationLimitSinceBestUpdate().register(stagnationLimit)
      EvaluationFinishedEvent().addListener { evs =>
        val ev = evs.maxBy(_.output.values(criterion))
        val evStr = criteria zip ev.output.values mkString ";"
        pw.println(s"${iterationsSoFar + iterationCount()}: $evStr")
      }

      val optimizer = Optimizer().simple
      optimizer() match {
        case Optimizer.Result(_, CodomainThreshold) =>
          Result(iterationCount(), successful = true)
        case Optimizer.Result(_, IterationLimit) =>
          Result(iterationCount(), successful = false)
        case Optimizer.Result(_, IterationLimitSinceBestUpdate) =>
          impl(iterationsSoFar + iterationCount())
        case Optimizer.Result(_, Crash(th)) =>
          throw th
      }
    }
    impl(0)
  }

  def runSingleEARL(
    pw: java.io.PrintWriter,
    generationSize: Int,
    iterationLimit: Long,
    eliteRate: Double,
    agent: Agent[Int, Int]
  ): Result = {
    implicit val evaluator       = Evaluator().usingFunction(evaluate)
    implicit val comparator      = EARLCodomainComparator().fromMultipleCriteria(0)
    implicit val initialization  = Initialization().useDomainGenerator(randomInput(), generationSize)
    implicit val selection       = Selection().tournament(generationSize)
    implicit val update          = Update().elitist(eliteRate)
    implicit val iteration       = Iteration().fromSelectionMutationEvaluateUpdate
    implicit val termination     = Termination.Pluggable()
    implicit val iterationCount  = new IterationCount()

    CodomainThreshold().register(_.values(0), timeLimit)
    IterationLimit().register(iterationLimit)
    EARLConfiguration().registerOldWay(
      agent,
      ws => 0,
      (ows, nws) => reward(nws) - reward(ows)
    )
    EvaluationFinishedEvent().addListener { evs =>
      val ev = evs.maxBy(_.output.values(0))
      val evStr = criteria zip ev.output.values mkString ";"
      pw.println(s"${iterationCount()}: [${comparator.currentChoice}] $evStr")
    }

    val optimizer = Optimizer().simple
    optimizer() match {
      case Optimizer.Result(_, CodomainThreshold) =>
        Result(iterationCount(), successful = true)
      case Optimizer.Result(_, IterationLimit) =>
        Result(iterationCount(), successful = false)
      case Optimizer.Result(_, Crash(th)) =>
        throw th
    }
  }

  def runSingleAlg1(
    pw: java.io.PrintWriter,
    generationSize: Int,
    iterationLimit: Long,
    eliteRate: Double
  ): Result = {
    implicit val evaluator       = Evaluator().usingFunction(evaluate)
    implicit val comparator      = EARLCodomainComparator().fromMultipleCriteria(0)
    implicit val initialization  = Initialization().useDomainGenerator(randomInput(), generationSize)
    implicit val selection       = Selection().tournament(generationSize)
    implicit val update          = Update().elitist(eliteRate)
    implicit val iteration       = Iteration().fromSelectionMutationEvaluateUpdate
    implicit val termination     = Termination.Pluggable()
    implicit val iterationCount  = new IterationCount()

    CodomainThreshold().register(_.values(0), timeLimit)
    IterationLimit().register(iterationLimit)
    EARLConfiguration().registerUniversalCoOptimal()
    EvaluationFinishedEvent().addListener { evs =>
      val ev = evs.maxBy(_.output.values(0))
      val evStr = criteria zip ev.output.values mkString ";"
      pw.println(s"${iterationCount()}: [${comparator.currentChoice}] $evStr")
    }

    val optimizer = Optimizer().simple
    optimizer() match {
      case Optimizer.Result(_, CodomainThreshold) =>
        Result(iterationCount(), successful = true)
      case Optimizer.Result(_, IterationLimit) =>
        Result(iterationCount(), successful = false)
      case Optimizer.Result(_, Crash(th)) =>
        throw th
    }
  }

  def runSingleAlg2(
    pw: java.io.PrintWriter,
    generationSize: Int,
    iterationLimit: Long,
    stagnationLimit: Long,
    eliteRate: Double
  ): Result = {
    @tailrec
    def impl(fitnessIndex: Int, localLimit: Long, internalLimit: Long, passed: Long): Result = {
      implicit val evaluator       = Evaluator().usingFunction(evaluate)
      implicit val comparator      = multiple.projection(fitnessIndex)
      implicit val initialization  = Initialization().useDomainGenerator(randomInput(), generationSize)
      implicit val selection       = Selection().tournament(generationSize)
      implicit val update          = Update().elitist(eliteRate)
      implicit val iteration       = Iteration().fromSelectionMutationEvaluateUpdate
      implicit val termination     = Termination.Pluggable()
      implicit val bestEvaluated   = new BestEvaluated()
      implicit val iterationCount  = new IterationCount()
      implicit val iterationCount2 = new IterationCountSinceBestUpdate()

      CodomainThreshold().register(_.values(0), timeLimit)
      IterationLimit().register((iterationLimit - passed) min internalLimit)
      IterationLimitSinceBestUpdate().register(stagnationLimit)

      EvaluationFinishedEvent().addListener { evs =>
        val ev = evs.maxBy(_.output.values(fitnessIndex))
        val evStr = criteria zip ev.output.values mkString ";"
        pw.println(s"${passed + iterationCount()}: [$fitnessIndex] $evStr")
      }

      val optimizer = Optimizer().simple
      optimizer() match {
        case Optimizer.Result(_, CodomainThreshold) =>
          Result(iterationCount() + passed, successful = true)
        case Optimizer.Result(_, IterationLimitSinceBestUpdate) =>
          val newPassed = passed + iterationCount()
          if (newPassed >= iterationLimit) {
            Result(newPassed, successful = true)
          } else {
            impl(fitnessIndex, localLimit, internalLimit - newPassed, newPassed)
          }
        case Optimizer.Result(_, IterationLimit) =>
          val newPassed = passed + iterationCount()
          if (newPassed >= iterationLimit) {
            Result(newPassed, successful = true)
          } else if (fitnessIndex + 1 == multiple.numberOfCriteria) {
            impl(0, 2 * localLimit, 2 * localLimit, newPassed)
          } else {
            impl(fitnessIndex + 1, localLimit, localLimit, newPassed)
          }
        case Optimizer.Result(_, Crash(th)) =>
          throw th
      }
    }
    impl(0, 1, 1, 0)
  }
}

object Config {
  case class Codomain(values: IndexedSeq[Long], ships: IndexedSeq[Int], havens: IndexedSeq[Int])
  case class Result(evaluations: Long, successful: Boolean)
  implicit def codomain2IndexedSeqLong(c: Codomain) = c.values
}
