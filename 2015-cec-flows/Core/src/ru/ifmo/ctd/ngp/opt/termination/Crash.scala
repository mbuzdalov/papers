package ru.ifmo.ctd.ngp.opt.termination

import ru.ifmo.ctd.ngp.opt.Termination

/**
 * A termination reason meaning an exception is thrown.
 *
 * @author Maxim Buzdalov
 */
case class Crash(th: Throwable) extends Termination.Reason {
  def reasonText = th.getMessage
}
