package ru.ifmo.ctd.ngp.opt.iteration

import scala.language.higherKinds
import ru.ifmo.ctd.ngp.opt.{CodomainComparator, Evaluated}
import ru.ifmo.ctd.ngp.opt.types.{WorkingSetType, CodomainType, DomainType}

/**
 * A base class for update operators which merge the old working set and `Evaluated` objects to make a new working set.
 *
 * @author Maxim Buzdalov
 */
abstract class Update[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType] {
  /**
   * Updates given the working set by the given `Evaluated` objects, returning the new working set.
   * @param workingSet the working set.
   * @param evaluated the sequence of `Evaluated` objects.
   * @return the new working set.
   */
  def apply(workingSet: W[D, C], evaluated: IndexedSeq[Evaluated[D, C]]): W[D, C]
}

object Update {
  class Detected[D: DomainType, C: CodomainType, W[+_D, +_C]: WorkingSetType] {
    /**
     * Returns an `Update` object using the given function as its "apply" method.
     * @param function the function to use.
     * @return the `Update` object.
     */
    def using(function: (W[D, C], IndexedSeq[Evaluated[D, C]]) => W[D, C]) = new Update[D, C, W] {
      override def apply(workingSet: W[D, C], evaluated: IndexedSeq[Evaluated[D, C]]) = function(workingSet, evaluated)
    }
    /**
     * Returns an update operator which discards the old working set.
     * @param indexedSeq2WorkingSet the conversion from `IndexedSeq` of `Evaluated` objects to the working set.
     * @return the `Update` object.
     */
    def allNew(implicit indexedSeq2WorkingSet: IndexedSeq[Evaluated[D, C]] => W[D, C]) = new Update[D, C, W] {
      override def apply(workingSet: W[D, C], evaluated: IndexedSeq[Evaluated[D, C]]) = indexedSeq2WorkingSet(evaluated)
    }
    /**
     * Returns an update operator which saves the given ratio of best `Evaluated` objects from old working set.
     * The size of the resulting working set is defined by the size of old working set.
     * @param eliteRatio the elite ratio, must be not less than 0 and not greater than 1.
     * @param indexedSeq2WorkingSet the conversion from `IndexedSeq` of `Evaluated` objects to the working set.
     * @param workingSet2IndexedSeq the conversion from working set to the `IndexedSeq` of `Evaluated` objects.
     * @param codomainComparator the codomain comparator.
     * @return the `Update` object.
     */
    def elitist(eliteRatio: Double)(
      implicit indexedSeq2WorkingSet: IndexedSeq[Evaluated[D, C]] => W[D, C],
               workingSet2IndexedSeq: W[D, C] => IndexedSeq[Evaluated[D, C]],
               codomainComparator: CodomainComparator[C]
    ) = {
      require(eliteRatio >= 0 && eliteRatio <= 1)
      new Update[D, C, W] {
        override def apply(workingSet: W[D, C], evaluated: IndexedSeq[Evaluated[D, C]]) = {
          implicit val o = codomainComparator.evaluatedOrdering
          val ws = workingSet2IndexedSeq(workingSet).sorted
          val size = ws.size
          val elite = (size * eliteRatio).ceil.toInt
          val others = math.min(evaluated.size, size - elite)
          val realElite = size - others
          indexedSeq2WorkingSet(ws.takeRight(realElite) ++ evaluated.sorted.takeRight(others))
        }
      }
    }
    /**
     * Returns an update operator which selects approximately equal number of individuals for each
     * fitness value (differing as by codomain comparator).
     * @param indexedSeq2WorkingSet the conversion from `IndexedSeq` of `Evaluated` objects to the working set.
     * @param workingSet2IndexedSeq the conversion from working set to the `IndexedSeq` of `Evaluated` objects.
     * @param codomainComparator the codomain comparator.
     * @return the `Update` object.
     */
    def equalForFitnessInstance(
      implicit indexedSeq2WorkingSet: IndexedSeq[Evaluated[D, C]] => W[D, C],
               workingSet2IndexedSeq: W[D, C] => IndexedSeq[Evaluated[D, C]],
               codomainComparator: CodomainComparator[C]
    ) = {
      new Update[D, C, W] {
        override def apply(workingSet: W[D, C], evaluated: IndexedSeq[Evaluated[D, C]]) = {
          val wsAsSeq = workingSet2IndexedSeq(workingSet)
          val sorted = (evaluated ++ wsAsSeq).sorted(codomainComparator.evaluatedOrdering)
          val byFitness = {
            val builder = IndexedSeq.newBuilder[IndexedSeq[Evaluated[D, C]]]
            val latest = IndexedSeq.newBuilder[Evaluated[D, C]]
            latest += sorted.last
            for (i <- sorted.size - 2 to 0 by -1) {
              if (codomainComparator(sorted(i + 1).output, sorted(i).output) != 0) {
                builder += latest.result()
                latest.clear()
              }
              latest += sorted(i)
            }
            builder += latest.result()
            builder.result().take(wsAsSeq.size)
          }
          val selected = IndexedSeq.newBuilder[Evaluated[D, C]]
          val indices = Array.ofDim[Int](byFitness.size)
          var curr, count = 0
          while (count < wsAsSeq.size) {
            if (indices(curr) < byFitness(curr).size) {
              selected += byFitness(curr)(indices(curr))
              indices(curr) += 1
              count += 1
            }
            curr = (curr + 1) % indices.size
          }
          indexedSeq2WorkingSet(selected.result())
        }
      }
    }
    /**
     * Returns an update operator which saves the best `Evaluated` objects from both old working set and the new ones.
     * The size of the resulting working set is defined by the size of old working set.
     * @param indexedSeq2WorkingSet the conversion from `IndexedSeq` of `Evaluated` objects to the working set.
     * @param workingSet2IndexedSeq the conversion from working set to the `IndexedSeq` of `Evaluated` objects.
     * @param codomainComparator the codomain comparator.
     * @return the `Update` object.
     */
    def best(
      implicit indexedSeq2WorkingSet: IndexedSeq[Evaluated[D, C]] => W[D, C],
               workingSet2IndexedSeq: W[D, C] => IndexedSeq[Evaluated[D, C]],
               codomainComparator: CodomainComparator[C]
    ) = new Update[D, C, W] {
      override def apply(workingSet: W[D, C], evaluated: IndexedSeq[Evaluated[D, C]]) = {
        val old = workingSet2IndexedSeq(workingSet)
        indexedSeq2WorkingSet((old ++ evaluated).sorted(codomainComparator.evaluatedOrdering).takeRight(old.size))
      }
    }
    /**
     * Returns an update operator which saves the best `Evaluated` objects from the new ones.
     * The size of the resulting working set is defined by the size of old working set.
     * @param indexedSeq2WorkingSet the conversion from `IndexedSeq` of `Evaluated` objects to the working set.
     * @param workingSet2IndexedSeq the conversion from working set to the `IndexedSeq` of `Evaluated` objects.
     * @param codomainComparator the codomain comparator.
     * @return the `Update` object.
     */
    def bestNew(
      implicit indexedSeq2WorkingSet: IndexedSeq[Evaluated[D, C]] => W[D, C],
               workingSet2IndexedSeq: W[D, C] => IndexedSeq[Evaluated[D, C]],
               codomainComparator: CodomainComparator[C]
    ) = new Update[D, C, W] {
      override def apply(workingSet: W[D, C], evaluated: IndexedSeq[Evaluated[D, C]]) = {
        val old = workingSet2IndexedSeq(workingSet)
        indexedSeq2WorkingSet(evaluated.sorted(codomainComparator.evaluatedOrdering).takeRight(old.size))
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
