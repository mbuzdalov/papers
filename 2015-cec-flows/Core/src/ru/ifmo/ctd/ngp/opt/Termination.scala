package ru.ifmo.ctd.ngp.opt

import scala.collection.mutable.ArrayBuffer
import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.types.{WorkingSetType, CodomainType, DomainType}
import ru.ifmo.ctd.ngp.opt.event.TerminationStartedEvent

/**
 * A base class for termination predicate for an optimization algorithm.
 *
 * @author Maxim Buzdalov
 */
abstract class Termination[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType]
  extends TerminationStartedEvent {
  /**
   * Tests the working set for the termination conditions.
   * This method delegates to `terminationReason` and, if there is some reason, fires a `TerminationStartedEvent`.
   * @param ws the working set to be tested.
   * @return None if there is no reason to terminate, Some(reason) if there is one.
   */
  final def apply(ws: W[D, C]) = terminationReason(ws) match {
    case v@Some(r) => terminationStartedAccessor.fire(r); v
    case None => None
  }
  /**
   * Tests the working set for the termination conditions.
   * @param ws the working set to be tested.
   * @return None if there is no reason to terminate, Some(reason) if there is one.
   */
  protected def terminationReason(ws: W[D, C]): Option[Termination.Reason]
}

/**
 * A companion object for `Termination`.
 */
object Termination {
  /**
   * A marker trait for termination reasons.
   */
  trait Reason {
    def reasonText: String
  }

  /**
   * A termination module which supports plugins.
   *
   * @tparam D the domain type.
   * @tparam C the codomain type.
   * @tparam W the working set type.
   */
  case class Pluggable[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType]() extends Termination[D, C, W] {
    private val plugins = new ArrayBuffer[W[D, C] => Option[Termination.Reason]]()
    /**
     * Adds a plugin.
     * @param plugin the plugin to add.
     */
    def += (plugin: W[D, C] => Option[Termination.Reason]) { plugins += plugin }
    /**
     * Removes a plugin.
     * @param plugin the plugin to remove.
     */
    def -= (plugin: W[D, C] => Option[Termination.Reason]) { plugins -= plugin }

    protected def terminationReason(ws: W[D, C]) = plugins.view.flatMap(_(ws)).headOption
  }
}
