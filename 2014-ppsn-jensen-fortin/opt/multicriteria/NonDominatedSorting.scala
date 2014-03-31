package opt.multicriteria

import scala.language.higherKinds

import opt.Evaluated
import opt.types.{CodomainType, DomainType}
import opt.multicriteria.nds.{JensenFortinBuzdalov, JensenFortin, DebLinearMemory}

/**
 * A class which encapsulates an algorithm for non-dominated sorting.
 *
 * @author Maxim Buzdalov
 */
abstract class NonDominatedSorting[D : DomainType, C : CodomainType : MultipleCriteria] {
  /**
   * Performs a non-dominated sort of the given `Evaluated` objects.
   * Returns a sequence of layers: layer 0 contains the objects that are not dominated
   * by anything, layer 1 contains the objects dominated by the layer 0 only and so on.
   * @param data the objects to be sorted.
   * @return the layers starting with the non-dominated one.
   */
  def apply(data: IndexedSeq[Evaluated[D, C]]): IndexedSeq[IndexedSeq[Evaluated[D, C]]]
}

object NonDominatedSorting {
  class Detected[D : DomainType, C : CodomainType : MultipleCriteria] {
    val debLinearMemorySorting:      NonDominatedSorting[D, C] = new DebLinearMemory
    val jensenFortinSorting:         NonDominatedSorting[D, C] = new JensenFortin
    val jensenFortinBuzdalovSorting: NonDominatedSorting[D, C] = new JensenFortinBuzdalov
  }

  def apply[D : DomainType, C : CodomainType : MultipleCriteria]() = new Detected()
}
