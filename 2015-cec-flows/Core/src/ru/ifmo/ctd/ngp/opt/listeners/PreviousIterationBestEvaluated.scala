package ru.ifmo.ctd.ngp.opt.listeners

import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.{CodomainComparator, Evaluated}
import ru.ifmo.ctd.ngp.opt.event.{InitializationStartedEvent, EvaluationFinishedEvent}

/**
 * A listener which stores the best `Evaluated` from the previous iteration.
 *
 * @author Maxim Buzdalov
 */
@deprecated(message = "We need a best evaluated history instead of this", since = "the birth of the class")
class PreviousIterationBestEvaluated[D, C](
  implicit evaluationFinished: EvaluationFinishedEvent[D, C],
           initializationStarted: InitializationStartedEvent,
           codomainComparator: CodomainComparator[C]
) {
  private var best, prevBest: Option[Evaluated[D, C]] = None
  def apply() = prevBest
  def get = prevBest.get

  InitializationStartedEvent() addListener { _ => best = None; prevBest = None }
  EvaluationFinishedEvent()    addListener { evaluated =>
    prevBest = best
    evaluated.foreach { t =>
      best = best match {
        case None      => Some(t)
        case o@Some(v) => if (codomainComparator(t.output, v.output) > 0) Some(t) else o
      }
    }
  }
}
