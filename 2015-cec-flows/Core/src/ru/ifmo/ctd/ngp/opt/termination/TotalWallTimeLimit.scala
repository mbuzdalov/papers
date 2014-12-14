package ru.ifmo.ctd.ngp.opt.termination

import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.Termination
import ru.ifmo.ctd.ngp.opt.Termination.Pluggable
import ru.ifmo.ctd.ngp.opt.types.{DomainType, CodomainType, WorkingSetType}
import ru.ifmo.ctd.ngp.opt.listeners.TimeFromStart

/**
 * A termination criterion which says 'stop' when the number of evaluations exceeded a certain limit.
 *
 * @author Maxim Buzdalov
 */
case object TotalWallTimeLimit extends Termination.Reason {
  def reasonText = "total wall time limit exceeded"

  class Detected[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType] {
    def register(limit: Long)(
      implicit pluggable: Pluggable[D, C, W],
               time: TimeFromStart
    ) {
      pluggable += { w =>
        if (time() >= limit) {
          Some(TotalWallTimeLimit)
        } else {
          None
        }
      }
    }
  }
  def apply[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType]() = new Detected()
}
