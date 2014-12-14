package ru.ifmo.ctd.ngp.opt.listeners

import ru.ifmo.ctd.ngp.opt.event.InitializationStartedEvent

/**
 * Provides time from start, in milliseconds.
 * Contains a method to reset the starting time manually.
 * @author Maxim Buzdalov
 */
class TimeFromStart(resetAtInitialization: Boolean)(implicit initializationStarted: InitializationStartedEvent) {
  private var time = System.currentTimeMillis()
  def apply() = System.currentTimeMillis() - time
  def reset() { time = System.currentTimeMillis() }

  if (resetAtInitialization) {
    InitializationStartedEvent() addListener { _ => reset() }
  }
}
