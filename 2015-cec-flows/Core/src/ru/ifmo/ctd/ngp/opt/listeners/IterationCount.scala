package ru.ifmo.ctd.ngp.opt.listeners

import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.event.{InitializationStartedEvent, EvaluationFinishedEvent}

/**
 * A thing that listens to `EvaluationFinishedEvent`s and counts the number of iterations, including initialization.
 *
 * @author Maxim Buzdalov
 */
class IterationCount[D, C](
  implicit evaluationFinished: EvaluationFinishedEvent[D, C],
           initializationStarted: InitializationStartedEvent
) {
  private var count = 0L
  def apply() = count

  InitializationStartedEvent() addListener { _ => count = 0  }
  EvaluationFinishedEvent()    addListener { _ => count += 1 }
}
