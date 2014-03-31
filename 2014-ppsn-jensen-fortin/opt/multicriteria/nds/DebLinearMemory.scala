package opt.multicriteria.nds

import opt.types.{CodomainType, DomainType}
import opt.multicriteria.{DominationComparisonResult, NonDominatedSorting, MultipleCriteria}
import opt.Evaluated

/**
 * An implementation of a non-dominated sorting algorithm according to Deb.
 * It fits in linear memory, but performs twice as much comparisons as the original version.
 *
 * @author Maxim Buzdalov
 */
final class DebLinearMemory[D : DomainType, C : CodomainType : MultipleCriteria] extends NonDominatedSorting[D, C] {
  def apply(data: IndexedSeq[Evaluated[D, C]]) = {
    val multiple = implicitly[MultipleCriteria[C]]
    val sz = data.size
    val inDegree = Array.fill(sz)(0)
    val layer = Array.fill(sz)(-1)
    val queue = Array.fill(sz)(-1)
    var head, tail = 0

    for (l <- 0 until sz - 1) {
      for (r <- l + 1 until sz) {
        val cmp = multiple.domination(data(l), data(r))
        if (cmp == DominationComparisonResult.Greater) {
          inDegree(r) += 1
        } else if (cmp == DominationComparisonResult.Less) {
          inDegree(l) += 1
        }
      }
    }
    for (i <- 0 until sz if inDegree(i) == 0) {
      queue(head) = i
      layer(i) = 0
      head += 1
    }

    var maxLayer = 0
    while (head > tail) {
      val cur = queue(tail)
      maxLayer = math.max(maxLayer, layer(cur))
      tail += 1
      for (i <- 0 until sz if inDegree(i) > 0) {
        if (multiple.domination(data(cur), data(i)) == DominationComparisonResult.Greater) {
          layer(i) = math.max(layer(i), layer(cur) + 1)
          inDegree(i) -= 1
          if (inDegree(i) == 0) {
            queue(head) = i
            head += 1
          }
        }
      }
    }

    val arrays = IndexedSeq.tabulate(maxLayer + 1)(c => IndexedSeq.newBuilder[Evaluated[D, C]])
    for (i <- 0 until sz) {
      arrays(layer(i)) += data(i)
    }
    arrays.map(_.result())
  }
}
