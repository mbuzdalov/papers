package ru.ifmo.ctd.ngp.opt

import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.event.{IterationStartedEvent, IterationFinishedEvent}
import ru.ifmo.ctd.ngp.opt.types.{WorkingSetType, CodomainType, DomainType}
import ru.ifmo.ctd.ngp.opt.iteration.{Update, Mutation, Selection}
import ru.ifmo.ctd.ngp.util._

/**
 * A base class for iteration of an optimization algorithm.
 *
 * @author Maxim Buzdalov
 */
abstract class Iteration[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType]
  extends IterationStartedEvent[W[D, C]] with IterationFinishedEvent[W[D, C]] {
  /**
   * Performs an iteration of an optimization algorithm.
   *
   * Fires an `IterationStartedEvent`,
   * delegates its argument to `performIteration` and then fires an `IterationFinishedEvent`.
   * @param ws the working set before the iteration.
   * @return the working set after the iteration.
   */
  final def apply(ws: W[D, C]) = {
    ws |> iterationStartedAccessor.fire |> performIteration |> iterationFinishedAccessor.fire
  }
  /**
   * Performs an iteration of an optimization algorithm.
   * @param ws the working set before the iteration.
   * @return the working set after the iteration.
   */
  protected def performIteration(ws: W[D, C]): W[D, C]
}

/**
 * A human-readable DSL for the most common operations for creating iterations.
 */
object Iteration {
  class Detected[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType] {
    /**
     * Constructs an `Iteration` object from a function from working set to working set.
     * @param fun the function from working set to working set.
     * @return the `Iteration` object whose `performIteration` method calls the given function.
     */
    def use(fun: W[D, C] => W[D, C]): Iteration[D, C, W] = new Iteration[D, C, W]() {
      protected def performIteration(ws: W[D, C]) = fun(ws)
    }
    /**
     * Constructs an `Iteration` object from `Selection`, `Mutation`, `Evaluator` and `Update` objects
     * available through implicits.
     * @param selection the `Selection` object.
     * @param mutation the `Mutation` object.
     * @param evaluator the `Evaluator` object.
     * @param update the `Update` object.
     * @return
     */
    def fromSelectionMutationEvaluateUpdate(
      implicit selection: Selection[D, C, W],
               mutation: Mutation[D, C],
               evaluator: Evaluator[D, C],
               update: Update[D, C, W]
    ) = new Iteration[D, C, W] {
      protected def performIteration(ws: W[D, C]) = update(ws, evaluator(mutation(selection(ws))))
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
