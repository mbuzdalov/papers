package ru.ifmo.ctd.ngp.opt.termination

import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.Termination
import ru.ifmo.ctd.ngp.opt.Termination.Pluggable
import ru.ifmo.ctd.ngp.opt.listeners.IterationCountSinceBestUpdate
import ru.ifmo.ctd.ngp.opt.types.{DomainType, CodomainType, WorkingSetType}

/**
 * A termination criterion which says 'stop' when the number of iterations since last update of a best evaluated object
 * exceeded a certain limit.
 *
 * @author Maxim Buzdalov
 */
case object IterationLimitSinceBestUpdate extends Termination.Reason {
  def reasonText = "evaluation limit since update of the best exceeded"

  class Detected[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType] {
    def register(limit: Long)(
      implicit pluggable: Pluggable[D, C, W],
               iterationCount: IterationCountSinceBestUpdate[D, C]
    ) {
      pluggable += { w =>
        if (iterationCount() >= limit) {
          Some(IterationLimitSinceBestUpdate)
        } else {
          None
        }
      }
    }
  }
  def apply[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType]() = new Detected()
}
