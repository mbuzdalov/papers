package ru.ifmo.ctd.ngp.opt.event

import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.Evaluated

/**
 * An event which is fired when the best `Evaluated` object is updated.
 *
 * @author Maxim Buzdalov
 */
trait BestEvaluatedUpdatedEvent[D, C] {
  protected val bestEvaluatedUpdatedAccessor = new EventBackend.Accessor[Evaluated[D, C]]
  val bestEvaluatedUpdatedEvent = bestEvaluatedUpdatedAccessor.event
}

object BestEvaluatedUpdatedEvent {
  def apply[D, C]()(implicit event: BestEvaluatedUpdatedEvent[D, C]) = event.bestEvaluatedUpdatedEvent
}