package ru.ifmo.ctd.ngp.opt.event

import scala.language.higherKinds
import ru.ifmo.ctd.ngp.opt.Evaluated

/**
 * An event which is fired when evaluation of a number of domain points is finished by `Evaluator` object.
 *
 * The event's argument is an `IndexedSeq` of proper subclasses of `Evaluated` objects.
 *
 * @author Maxim Buzdalov
 */
trait EvaluationFinishedEvent[D, C] {
  protected val evaluationFinishedAccessor = new EventBackend.Accessor[IndexedSeq[Evaluated[D, C]]]
  val evaluationFinishedEvent = evaluationFinishedAccessor.event
}

object EvaluationFinishedEvent {
  def apply[D, C]()(implicit event: EvaluationFinishedEvent[D, C]) = event.evaluationFinishedEvent
}