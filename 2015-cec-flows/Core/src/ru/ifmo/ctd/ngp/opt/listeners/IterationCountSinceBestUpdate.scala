package ru.ifmo.ctd.ngp.opt.listeners

import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.event.{BestEvaluatedUpdatedEvent, InitializationStartedEvent, EvaluationFinishedEvent}

/**
 * A thing that listens to `EvaluationFinishedEvent`s and counts the number of iterations, including initialization,
 * since last update of the best evaluated object.
 *
 * @author Maxim Buzdalov
 */
class IterationCountSinceBestUpdate[D, C](
  implicit evaluationFinished: EvaluationFinishedEvent[D, C],
           bestEvaluatedUpdate: BestEvaluatedUpdatedEvent[D, C],
           initializationStarted: InitializationStartedEvent
) {
  private var count = 0L
  def apply() = count

  InitializationStartedEvent() addListener { _ => count = 0  }
  EvaluationFinishedEvent()    addListener { _ => count += 1 }
  BestEvaluatedUpdatedEvent()  addListener { _ => count = 0  }
}
