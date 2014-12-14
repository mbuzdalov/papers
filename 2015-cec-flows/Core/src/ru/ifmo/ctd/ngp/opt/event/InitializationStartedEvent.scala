package ru.ifmo.ctd.ngp.opt.event

/**
 * An event which is fired when the initialization is to be started by an `Initialization` object.
 *
 * The event's argument is the working set after the initialization.
 *
 * @author Maxim Buzdalov
 */
trait InitializationStartedEvent {
  protected val initializationStartedAccessor = new EventBackend.Accessor[Unit]
  val initializationStartedEvent = initializationStartedAccessor.event
}

object InitializationStartedEvent {
  def apply()(implicit event: InitializationStartedEvent) = event.initializationStartedEvent
}