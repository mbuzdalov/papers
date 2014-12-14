package ru.ifmo.ctd.ngp.opt.termination

import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.Termination
import ru.ifmo.ctd.ngp.opt.Termination.Pluggable
import ru.ifmo.ctd.ngp.opt.types.{DomainType, CodomainType, WorkingSetType}
import ru.ifmo.ctd.ngp.opt.listeners.EvaluationCount

/**
 * A termination criterion which says 'stop' when the number of evaluations exceeded a certain limit.
 *
 * @author Maxim Buzdalov
 */
case object EvaluationLimit extends Termination.Reason {
  def reasonText = "evaluation limit exceeded"

  class Detected[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType] {
    def register(limit: Long)(
      implicit pluggable: Pluggable[D, C, W],
               evaluationCount: EvaluationCount[D, C]
    ) {
      pluggable += { w =>
        if (evaluationCount() >= limit) {
          Some(EvaluationLimit)
        } else {
          None
        }
      }
    }
  }
  def apply[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType]() = new Detected()
}
