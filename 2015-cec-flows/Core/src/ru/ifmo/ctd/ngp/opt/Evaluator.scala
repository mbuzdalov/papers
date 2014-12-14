package ru.ifmo.ctd.ngp.opt

import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.types.{CodomainType, DomainType}
import ru.ifmo.ctd.ngp.opt.event.EvaluationFinishedEvent
import ru.ifmo.ctd.ngp.util._

/**
 * A base class for function evaluators.
 *
 * @author Maxim Buzdalov
 */
abstract class Evaluator[D: DomainType, C: CodomainType] extends EvaluationFinishedEvent[D, C] {
  /**
   * Computes the output values (from the codomain) for the given inputs (from the domain)
   * and wraps them into the specified `Evaluated` object type.
   *
   * The method delegates its argument to `evaluate` and then fires an `EvaluationFinishedEvent`.
   * @param inputs the sequence of inputs.
   * @return the sequence of `Evaluated` objects.
   */
  final def apply(inputs: IndexedSeq[D]) = inputs |> evaluate |> evaluationFinishedAccessor.fire
  /**
   * Computes the output values (from the codomain) for the given inputs (from the domain)
   * and wraps them into the specified `Evaluated` object type.
   * @param inputs the sequence of inputs.
   * @return the sequence of `Evaluated` objects.
   */
  protected def evaluate(inputs: IndexedSeq[D]): IndexedSeq[Evaluated[D, C]]
}

/**
 * A human-readable DSL for the most common operations for creating evaluators.
 */
object Evaluator {
  class Detected[D: DomainType, C: CodomainType] {
    /**
     * Constructs an evaluator which uses the domain-codomain mapping. The sequence executor is used to
     * compute multiple instances.
     * @param function the function which maps domain to codomain.
     * @param sequenceExecutor the sequence executor used to (possibly) parallel the computation.
     * @return the evaluator.
     */
    def usingFunction(function: D => C)(implicit sequenceExecutor: SequenceExecutor) = {
      new Evaluator[D, C]() {
        def evaluate(inputs: IndexedSeq[D]) = sequenceExecutor.map(inputs, (d: D) => Evaluated(d, function(d)))
      }
    }
  }

  /**
   * Defines the domain and codomain and allows to select options further.
   * @tparam D the domain type.
   * @tparam C the codomain type.
   * @return the object with functions to select more options.
   */
  def apply[D: DomainType, C: CodomainType]() = new Detected
}
