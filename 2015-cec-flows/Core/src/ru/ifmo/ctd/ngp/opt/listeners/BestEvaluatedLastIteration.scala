package ru.ifmo.ctd.ngp.opt.listeners

import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.{CodomainComparator, Evaluated}
import ru.ifmo.ctd.ngp.opt.event.{IterationStartedEvent, EvaluationFinishedEvent}

/**
 * A listener which stores the best `Evaluated` generated by the optimization algorithm.
 *
 * @author Maxim Buzdalov
 */
class BestEvaluatedLastIteration[D, C](
  implicit evaluationFinished: EvaluationFinishedEvent[D, C],
           iterationStarted: IterationStartedEvent[_],
           codomainComparator: CodomainComparator[C]
) {
  private var best: Option[Evaluated[D, C]] = None
  def apply() = best
  def get = best.get

  IterationStartedEvent()   addListener { _ => best = None }
  EvaluationFinishedEvent() addListener { list =>
    list.foreach { t =>
      best = best match {
        case None      => Some(t)
        case o@Some(v) => if (codomainComparator(t.output, v.output) > 0) Some(t) else o
      }
    }
  }
}
