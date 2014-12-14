package ru.ifmo.ctd.ngp.opt.event

/**
 * An event which is fired when the iteration is completed by an `Iteration` object.
 *
 * The event's argument is the working set after the iteration.
 *
 * @author Maxim Buzdalov
 */
trait IterationFinishedEvent[W] {
  protected val iterationFinishedAccessor = new EventBackend.Accessor[W]
  val iterationFinishedEvent = iterationFinishedAccessor.event
}

object IterationFinishedEvent {
  def apply[W]()(implicit event: IterationFinishedEvent[W]) = event.iterationFinishedEvent
}