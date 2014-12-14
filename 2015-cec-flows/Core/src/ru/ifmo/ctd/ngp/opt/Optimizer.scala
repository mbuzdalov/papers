package ru.ifmo.ctd.ngp.opt

import scala.annotation.tailrec
import scala.language.higherKinds
import scala.util.{Failure, Success, Try}

import ru.ifmo.ctd.ngp.opt.termination.Crash
import ru.ifmo.ctd.ngp.opt.types.{WorkingSetType, CodomainType, DomainType}

/**
 * A base class for optimization algorithms.
 *
 * @author Maxim Buzdalov
 */
abstract class Optimizer[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType] {
  /**
   * Performs optimization of the given working set
   * using the constructor-supplied `Iteration` object to advance the optimization
   * and `Termination` object to check if we need to stop.
   * @return the optimization result.
   */
  def apply(workingSet: W[D, C]): Optimizer.Result[W[D, C]]
  /**
   * Performs optimization of the working set supplied by the implicit `Initialization` object
   * using the constructor-supplied `Iteration` object to advance the optimization
   * and `Termination` object to check if we need to stop.
   * @param initialization the object which creates working sets.
   * @return the optimization result.
   */
  def apply()(implicit initialization: Initialization[D, C, W]): Optimizer.Result[W[D, C]]
}

object Optimizer {
  /**
   * The result of optimization.
   * @param workingSet the working set which was the last before termination.
   * @param terminationReason the termination reason.
   */
  case class Result[WS](workingSet: WS, terminationReason: Termination.Reason)

  class Detected[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType] {
    /**
     * Returns a "simple" optimizer which uses only the `Iteration` object to make optimization iterations
     * and `Termination` which tells when to stop.
     * @param iteration the iteration.
     * @param termination the termination.
     * @return the new optimizer.
     */
    def simple(implicit iteration: Iteration[D, C, W], termination: Termination[D, C, W]) = {
      new Optimizer[D, C, W]() {
        @tailrec
        final def apply(workingSet: W[D, C]): Result[W[D, C]] = {
          Try {
            termination(workingSet)
          } match {
            case Failure(th) => Result(workingSet, Crash(th))
            case Success(Some(v)) => Result(workingSet, v)
            case Success(None) => Try {
              iteration(workingSet)
            } match {
              case Success(i) => apply(i)
              case Failure(th) => Result(workingSet, Crash(th))
            }
          }
        }
        final def apply()(implicit initialization: Initialization[D, C, W]): Result[W[D, C]] = {
          Try {
            initialization()
          } match {
            case Success(v) => apply(v)
            case Failure(th) => Result(throw th, Crash(th))
          }
        }
      }
    }
  }

  /**
   * Detects the domain, codomain, `Evaluated` object type and the working set type, and allows to select more options.
   * @tparam D the domain type.
   * @tparam C the codomain type.
   * @tparam W the working set type.
   * @return the object for more options to build an `Iteration` object.
   */
  def apply[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType]() = new Detected
}
