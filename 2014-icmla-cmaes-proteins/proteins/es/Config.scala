package proteins.es

import java.io.PrintWriter

import ru.ifmo.ctd.ngp.opt._
import ru.ifmo.ctd.ngp.opt.algorithms.SepCMAES
import ru.ifmo.ctd.ngp.opt.listeners.{TimeFromStart, BestEvaluatedLastIteration, BestEvaluated, IterationCount}
import ru.ifmo.ctd.ngp.opt.termination.{Crash, IterationLimit, CovarianceMatrixDegeneration}
import ru.ifmo.ctd.ngp.opt.event.{IterationFinishedEvent, InitializationStartedEvent, BestEvaluatedUpdatedEvent}

import proteins.{Intersector, MatrixEncoding, MotionHelper}

/**
 * Optimization configuration.
 *
 * @author Maxim Buzdalov
 */
class Config(
  mh: MotionHelper with MatrixEncoding with Intersector,
  namePrefix: String,
  iterationLimit: Int,
  initialVector: IndexedSeq[Double],
  initialStepSize: Double,
  intersectionWeight: Double,
  stopOnNoIntersections: Boolean,
  beParallel: Boolean,
  log: PrintWriter = new PrintWriter(System.out, true)
) extends OptConfiguration[IndexedSeq[Double], (Double, Double, Double)] {
  implicit val parallel       = if (beParallel) SequenceExecutor.scalaParallel else SequenceExecutor.sequential
  implicit val comparator     = CodomainComparator().by(v => v._1 + intersectionWeight * v._3).decreasing
  implicit val evaluator      = Evaluator().usingFunction(z => mh.evaluate(z, intersectionWeight > 0))
  implicit val (ws, init, itr) = SepCMAES().forIndexedSeqDomain(
    initialVector           = initialVector,
    initialStepSize         = initialStepSize,
    minimumStepSize         = 0,
    weights                 = SepCMAES.logarithmicWeights(64, 32),
    initialCovarianceMatrix = IndexedSeq.tabulate(mh.matrixSize)(mh.initializeMatrix)
  )
  implicit val termination     = new Termination.Pluggable
  implicit val optimizer       = Optimizer().simple
  implicit val iterationCount  = new IterationCount()
  implicit val bestEvaluated   = new BestEvaluated()
  implicit val lastEvaluated   = new BestEvaluatedLastIteration()
  implicit val timeFromStart   = new TimeFromStart(resetAtInitialization = true)

  private def print(model: domain.Type, path: String) {
    val result = new PrintWriter(path)
    result.println(mh.buildPDB(mh.indexedSeq2torsion(model)))
    result.close()
  }

  case object ZeroIntersections extends Termination.Reason {
    def reasonText = "No intersections found"
    var found = false

    def register() {
      InitializationStartedEvent().addListener(_ => found = false)
      BestEvaluatedUpdatedEvent().addListener(ev => found |= ev.output._3 == 0)
      termination += { ws => if (found) Some(this) else None }
    }
  }

  CovarianceMatrixDegeneration().forDiagonalCMAES(1e-250)
  if (stopOnNoIntersections) {
    ZeroIntersections.register()
  } else {
    IterationLimit().register(iterationLimit)
  }

  IterationFinishedEvent().addListener { ws =>
    val newBest = bestEvaluated().get
    val iterations = iterationCount()
    log.println(s"$iterations: ${newBest.output}, current: ${lastEvaluated().get.output} in ${timeFromStart()} ms")
    if (iterations % 500 == 0) {
      print(newBest.input, f"$namePrefix-intermediate-$iterations%06d.pdb")
    }
  }

  def run() = {
    val result = optimizer() match {
      case Optimizer.Result(_, IterationLimit)               => bestEvaluated().get
      case Optimizer.Result(_, CovarianceMatrixDegeneration) => bestEvaluated().get
      case Optimizer.Result(_, ZeroIntersections)            => bestEvaluated().get
      case Optimizer.Result(_, Crash(th))                    => throw th
      case _                                                 => throw new AssertionError()
    }
    result.input
  }
}
