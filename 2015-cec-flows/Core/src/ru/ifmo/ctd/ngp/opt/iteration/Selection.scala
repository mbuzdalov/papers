package ru.ifmo.ctd.ngp.opt.iteration

import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.{RandomSource, CodomainComparator, Evaluated}
import ru.ifmo.ctd.ngp.opt.types.{WorkingSetType, CodomainType, DomainType}

/**
 * A base class for selection operators which extract `Evaluated` objects from the working set.
 *
 * @author Maxim Buzdalov
 */
abstract class Selection[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType] {
  /**
   * Extracts the specified number of `Evaluated` objects from the given working set.
   * @param workingSet the working set.
   * @return the selected `Evaluated` objects in an `IndexedSeq`.
   */
  def apply(workingSet: W[D, C]): IndexedSeq[Evaluated[D, C]]
}

object Selection {
  class Detected[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType] {
    /**
     * Returns a `Selection` object using the given function as its "apply" method.
     * @param function the function to use.
     * @return the `Selection` object.
     */
    def using(function: W[D, C] => IndexedSeq[Evaluated[D, C]]) = new Selection[D, C, W] {
      override def apply(workingSet: W[D, C]) = function(workingSet)
    }
    /**
     * Returns a `Selection` object that returns all `Evaluated` objects in the working set,
     * provided that the working set has a conversion to `TraversableOnce`.
     * The `Selection` object will ignore the second argument to the apply method, count.
     * @param workingSet2TraversableOnce the conversion of the working set to `TraversableOnce` of `Evaluated` objects.
     * @return the `Selection` object.
     */
    def all(
      implicit workingSet2TraversableOnce: W[D, C] => TraversableOnce[Evaluated[D, C]]
    ) = new Selection[D, C, W] {
      override def apply(workingSet: W[D, C]) = workingSet2TraversableOnce(workingSet).toIndexedSeq
    }
    /**
     * Returns a `Selection` object that returns all `Evaluated` objects in the working set,
     * each in the given number of copies,
     * provided that the working set has a conversion to `TraversableOnce`.
     * The `Selection` object will ignore the second argument to the apply method, count.
     * @param numberOfCopies how many copies of each object to select
     * @param workingSet2TraversableOnce the conversion of the working set to `TraversableOnce` of `Evaluated` objects.
     * @return the `Selection` object.
     */
    def allCopied(numberOfCopies: Int = 1)(
      implicit workingSet2TraversableOnce: W[D, C] => TraversableOnce[Evaluated[D, C]]
    ) = new Selection[D, C, W] {
      override def apply(workingSet: W[D, C]) = {
        val orig = workingSet2TraversableOnce(workingSet).toIndexedSeq
        if (numberOfCopies == 1) orig else {
          orig.flatMap(i => IndexedSeq.fill(numberOfCopies)(i))
        }
      }
    }
    /**
     * Returns a `Selection` object that returns the required number of `Evaluated` objects
     * randomly selected from the working set,
     * provided that the working set has a conversion to `IndexedSeq`.
     * This `Selection` object also uses a random number generator.
     * @param workingSet2IndexedSeq the conversion of the working set to `IndexedSeq` of `Evaluated` objects.
     * @param random the random number generator.
     * @return the `Selection` object.
     */
    def random(count: Int)(
      implicit workingSet2IndexedSeq: W[D, C] => IndexedSeq[Evaluated[D, C]],
               random: RandomSource
    ) = new Selection[D, C, W] {
      override def apply(workingSet: W[D, C]) = {
        val is = workingSet2IndexedSeq(workingSet)
        val rng = random()
        IndexedSeq.fill(count)(is(rng.nextInt(is.size)))
      }
    }
    /**
     * Returns a `Selection` object that returns the required number of `Evaluated` objects
     * selected using a simple tournament selection algorithm
     * provided that the working set has a conversion to `IndexedSeq`
     * and the codomain comparator is available.
     * This `Selection` object also uses a random number generator.
     * @param workingSet2IndexedSeq the conversion of the working set to `IndexedSeq` of `Evaluated` objects.
     * @param codomainComparator the codomain comparator to select the better `Evaluated` objects.
     * @param random the random number generator.
     * @return the `Selection` object.
     */
    def tournament(count: Int)(
      implicit workingSet2IndexedSeq: W[D, C] => IndexedSeq[Evaluated[D, C]],
               codomainComparator: CodomainComparator[C],
               random: RandomSource
    ) = new Selection[D, C, W] {
      override def apply(workingSet: W[D, C]) = {
        val is = workingSet2IndexedSeq(workingSet)
        def better(a: Evaluated[D, C], b: Evaluated[D, C]) = if (codomainComparator(a.output, b.output) >= 0) a else b
        val rng = random()
        IndexedSeq.fill(count)(better(is(rng.nextInt(is.size)), is(rng.nextInt(is.size))))
      }
    }
    /**
     * Returns a `Selection` object that returns the required number of `Evaluated` objects
     * selected using an olympic tournament selection algorithm
     * provided that the working set has a conversion to `IndexedSeq`
     * and the codomain comparator is available.
     * This `Selection` object also uses a random number generator.
     * @param workingSet2IndexedSeq the conversion of the working set to `IndexedSeq` of `Evaluated` objects.
     * @param codomainComparator the codomain comparator to select the better `Evaluated` objects.
     * @param random the random number generator.
     * @return the `Selection` object.
     */
    def tournamentOlympic(count: Int, depth: Int, strongerWins: Double)(
      implicit workingSet2IndexedSeq: W[D, C] => IndexedSeq[Evaluated[D, C]],
              codomainComparator: CodomainComparator[C],
              random: RandomSource
    ) = new Selection[D, C, W] {
      override def apply(workingSet: W[D, C]) = {
        val is = workingSet2IndexedSeq(workingSet)
        val rng = random()
        def select(depth: Int): Evaluated[D, C] = if (depth == 0) {
          is(rng.nextInt(is.size))
        } else {
          val a = select(depth - 1)
          val b = select(depth - 1)
          if (rng.nextDouble() < strongerWins) {
            if (codomainComparator(a.output, b.output) >= 0) a else b
          } else {
            if (codomainComparator(a.output, b.output) >= 0) b else a
          }
        }
        IndexedSeq.fill(count)(select(depth))
      }
    }
    /**
     * Returns a `Selection` object that returns the required number of best `Evaluated` objects
     * according to the codomain comparator
     * provided that the working set has a conversion to `IndexedSeq`.
     * If the required number of `Evaluated` objects is too big, all available objects are returned.
     * @param workingSet2IndexedSeq the conversion of the working set to `IndexedSeq` of `Evaluated` objects.
     * @param codomainComparator the codomain comparator to select the better `Evaluated` objects.
     * @return the `Selection` object.
     */
    def best(count: Int)(
      implicit workingSet2IndexedSeq: W[D, C] => IndexedSeq[Evaluated[D, C]],
               codomainComparator: CodomainComparator[C]
    ) = new Selection[D, C, W] {
      override def apply(workingSet: W[D, C]) = {
        val is = workingSet2IndexedSeq(workingSet).sorted(codomainComparator.evaluatedOrdering)
        is.takeRight(math.min(count, is.size))
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
