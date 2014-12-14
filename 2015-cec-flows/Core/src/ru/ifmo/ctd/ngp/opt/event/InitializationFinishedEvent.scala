package ru.ifmo.ctd.ngp.opt.event

/**
 * An event which is fired when the initialization is completed by an `Initialization` object.
 *
 * The event's argument is the working set after the initialization.
 *
 * @author Maxim Buzdalov
 */
trait InitializationFinishedEvent[W] {
  protected val initializationFinishedAccessor = new EventBackend.Accessor[W]
  val initializationFinishedEvent = initializationFinishedAccessor.event
}

object InitializationFinishedEvent {
  def apply[W]()(implicit event: InitializationFinishedEvent[W]) = event.initializationFinishedEvent
}