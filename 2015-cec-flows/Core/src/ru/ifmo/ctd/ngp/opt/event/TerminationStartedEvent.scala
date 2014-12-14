package ru.ifmo.ctd.ngp.opt.event

import scala.language.higherKinds
import ru.ifmo.ctd.ngp.opt.{Termination, Evaluated}

/**
 * An event which is fired when termination of the algorithm is started.
 *
 * The event's argument is an `IndexedSeq` of proper subclasses of `Evaluated` objects.
 *
 * @author Maxim Buzdalov
 */
trait TerminationStartedEvent {
  protected val terminationStartedAccessor = new EventBackend.Accessor[Termination.Reason]
  val terminationStartedEvent = terminationStartedAccessor.event
}

object TerminationStartedEvent {
  def apply()(implicit event: TerminationStartedEvent) = event.terminationStartedEvent
}