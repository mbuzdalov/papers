package ru.ifmo.ctd.ngp.opt.event

import scala.collection.mutable.ArrayBuffer

/**
 * All events in NGP are implemented using the event backend. An event trait
 * declares a 'def traitNameEvent: EventBackend[T]' to reuse the listener handling code.
 *
 * @author Maxim Buzdalov
 */
sealed abstract class EventBackend[T] {
  private[EventBackend] val listeners = new ArrayBuffer[T => Any]
  private[EventBackend] def fire(arg: T) { listeners.foreach(_(arg)) }

  def addListener(listener: T => Any)    { listeners += listener }
  def removeListener(listener: T => Any) { listeners -= listener }
}

object EventBackend {
  class Accessor[T] {
    val event: EventBackend[T] = new EventBackend[T] {}
    def fire(arg: T): T = { event.fire(arg); arg }
  }
}
