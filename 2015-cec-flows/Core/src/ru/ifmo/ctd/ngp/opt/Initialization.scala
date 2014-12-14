package ru.ifmo.ctd.ngp.opt

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.types.{DomainType, CodomainType, WorkingSetType}
import ru.ifmo.ctd.ngp.opt.event.{InitializationStartedEvent, InitializationFinishedEvent}
import ru.ifmo.ctd.ngp.util._

/**
 * A base class for initialization of an optimization algorithm.
 *
 * @author Maxim Buzdalov
 */
abstract class Initialization[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType]
  extends InitializationStartedEvent with InitializationFinishedEvent[W[D, C]] {
  /**
   * Performs an initialization of an optimization algorithm.
   *
   * Fires an `InitializationStartedEvent`,
   * delegates its argument to `performInitialization` and then fires an `InitializationFinishedEvent`.
   * @return the working set after the initialization.
   */
  final def apply() = {
    initializationStartedAccessor.fire((): Unit); performInitialization |> initializationFinishedAccessor.fire
  }
  /**
   * Performs an initialization of an optimization algorithm.
   * @return the working set after the initialization.
   */
  protected def performInitialization(): W[D, C]
}

/**
 * A human-readable DSL for the most common operations for creating initializations.
 */
object Initialization {
  class Detected[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType] {
    /**
     * Constructs an `Initialization` object which initializes the algorithm from known domain values.
     * The method requires a conversion from `IndexedSeq` of `Evaluated` objects to the working set
     * as well as the `Evaluator` object, to be available implicitly.
     * @param it the iterable of domain values.
     * @return the `Initialization` object
     */
    def fromDomains(it: Iterable[D])(
      implicit indexedSeq2WorkingSet: IndexedSeq[Evaluated[D, C]] => W[D, C],
               evaluator: Evaluator[D, C]
    ) = new Initialization[D, C, W]() {
      protected def performInitialization() = indexedSeq2WorkingSet(evaluator(it.toIndexedSeq))
    }
    /**
     * Constructs an `Initialization` object which initializes the algorithm from known values.
     * The method requires a conversion from `IndexedSeq` of `Evaluated` objects to the working set
     * to be available implicitly.
     * @param it the iterable of `Evaluated` objects.
     * @return the `Initialization` object
     */
    def fromEvaluated(it: Iterable[Evaluated[D, C]])(
      implicit indexedSeq2WorkingSet: IndexedSeq[Evaluated[D, C]] => W[D, C]
    ) = new Initialization[D, C, W]() {
      protected def performInitialization() = indexedSeq2WorkingSet(it.toIndexedSeq)
    }
    /**
     * Constructs an `Initialization` object from a function from nothing to working set.
     * @param fun the function from nothing to working set.
     * @return the `Initialization` object whose `performInitialization` method calls the given function.
     */
    def use(fun: => W[D, C]): Initialization[D, C, W] = new Initialization[D, C, W]() {
      protected def performInitialization() = fun
    }
    /**
     * Uses a function that generates values from the domain, the number of values to generate
     * and implicitly available evaluator and conversion from `IndexedSeq` of `Evaluated` objects to the working set.
     * @param fun the function to generate values from the domain.
     * @param count the number of values to generate.
     * @param evaluator the evaluator.
     * @param indexedSeq2WorkingSet the conversion from `IndexedSeq` of `Evaluated` objects to the working set.
     * @return the `Initialization` object
     */
    def useDomainGenerator(fun: => D, count: Int)(
      implicit evaluator: Evaluator[D, C],
               indexedSeq2WorkingSet: IndexedSeq[Evaluated[D, C]] => W[D, C]
    ) = new Initialization[D, C, W] {
      protected def performInitialization() = indexedSeq2WorkingSet(evaluator(IndexedSeq.fill(count)(fun)))
    }
    /**
     * Uses a function that assumes that the domain is a sequence
     * and a function that generates elements of that sequence,
     * the number of elements in a sequence,
     * the number of values to generate
     * and implicitly available evaluator and conversion from `IndexedSeq` of `Evaluated` objects to the working set.
     * @param fun the function to generate values from the domain.
     * @param count the number of values to generate.
     * @param evaluator the evaluator.
     * @param indexedSeq2WorkingSet the conversion from `IndexedSeq` of `Evaluated` objects to the working set.
     * @return the `Initialization` object
     */
    def useElementGenerator[T](fun: => T, size: Int, count: Int)(
      implicit evaluator: Evaluator[D, C],
               indexedSeq2WorkingSet: IndexedSeq[Evaluated[D, C]] => W[D, C],
               canBuildFrom: CanBuildFrom[_, T, D]
    ) = new Initialization[D, C, W] {
      protected def performInitialization() = indexedSeq2WorkingSet(evaluator(IndexedSeq.fill(count) {
        val builder = canBuildFrom()
        builder.sizeHint(size)
        for (_ <- 0 until size) builder += fun
        builder.result()
      }))
    }
  }

  /**
   * Detects the domain, codomain, `Evaluated` object type and the working set type, and allows to select more options.
   * @tparam D the domain type.
   * @tparam C the codomain type.
   * @tparam W the working set type.
   * @return the object for more options to build an `Initialization` object.
   */
  def apply[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType]() = new Detected
}
