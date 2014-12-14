package ru.ifmo.ctd.ngp.opt.termination

import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.{CodomainComparator, Evaluated, Termination}
import ru.ifmo.ctd.ngp.opt.Termination.Pluggable
import ru.ifmo.ctd.ngp.opt.listeners.BestEvaluated
import ru.ifmo.ctd.ngp.opt.types.{WorkingSetType, CodomainType, DomainType}

/**
 * A termination reason which says 'stop' if there is an element in the working set
 * whose codomain value is equal or better than the specified threshold according to the codomain comparator.
 *
 * @author Maxim Buzdalov
 */
case object CodomainThreshold extends Termination.Reason {
  def reasonText = "codomain threshold reached"

  class Detected[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType] {
    def register(threshold: C)(
      implicit cc: CodomainComparator[C],
               pluggable: Pluggable[D, C, W],
               asEvaluatedCollection: W[D, C] => TraversableOnce[Evaluated[D, C]]
    ) {
      pluggable += { w =>
        if (asEvaluatedCollection(w).exists(t => cc(t.output, threshold) >= 0)) {
          Some(CodomainThreshold)
        } else {
          None
        }
      }
    }
    def registerUsingBestEvaluated(threshold: C)(
      implicit cc: CodomainComparator[C],
               pluggable: Pluggable[D, C, W],
               bestEvaluated: BestEvaluated[D, C]
    ) {
      pluggable += { w =>
        if (bestEvaluated().exists(t => cc(t.output, threshold) >= 0)) {
          Some(CodomainThreshold)
        } else {
          None
        }
      }
    }
    def register[A](conv: C => A, threshold: A)(
       implicit pluggable: Pluggable[D, C, W],
                asEvaluatedCollection: W[D, C] => TraversableOnce[Evaluated[D, C]],
                orderingOnA: Ordering[A]
    ) {
      pluggable += { w =>
        if (asEvaluatedCollection(w).exists(t => orderingOnA.gteq(conv(t.output), threshold))) {
          Some(CodomainThreshold)
        } else {
          None
        }
      }
    }
    def registerUsingBestEvaluated[A](conv: C => A, threshold: A)(
      implicit pluggable: Pluggable[D, C, W],
               bestEvaluated: BestEvaluated[D, C],
               orderingOnA: Ordering[A]
    ) {
      pluggable += { w =>
        if (bestEvaluated().exists(t => orderingOnA.gteq(conv(t.output), threshold))) {
          Some(CodomainThreshold)
        } else {
          None
        }
      }
    }
  }
  def apply[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType]() = new Detected()
}
