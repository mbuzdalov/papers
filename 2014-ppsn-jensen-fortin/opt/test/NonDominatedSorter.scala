package opt.test

import opt._
import opt.multicriteria._

/**
 * A configuration for non-dominated sorters.
 *
 * @author Maxim Buzdalov
 */
abstract class NonDominatedSorter(criteria: Int) extends OptConfiguration[Null, IndexedSeq[Int]] {
  implicit val multiple = MultipleCriteria.fromIndexedSeqWithElementOrdering((0 until criteria).map(_.toString) :_*)
  implicit val sorting = sortingImpl

  def doSorting(coll: IndexedSeq[IndexedSeq[Int]]) = {
    sorting(coll.map(e => Evaluated(null, e))).map(_.map(_.output))
  }

  protected def sortingImpl: NonDominatedSorting[domain.Type, codomain.Type]
}

object NonDominatedSorter {
  type IS[+A] = IndexedSeq[A]
  val IS = IndexedSeq
  val seqOrdering = new Ordering[IndexedSeq[Int]] {
    def compare(x: IndexedSeq[Int], y: IndexedSeq[Int]) = {
      def impl(index: Int): Int = {
        if (index == x.size) 0 else {
          val cmp = x(index).compareTo(y(index))
          if (cmp == 0) {
            impl(index + 1)
          } else cmp
        }
      }
      impl(0)
    }
  }
  def sortNDSResults(res: IndexedSeq[IndexedSeq[IndexedSeq[Int]]]) = res.map(_.sorted(seqOrdering))

  def createDeb(criteria: Int): NonDominatedSorter = new NonDominatedSorter(criteria) {
    protected def sortingImpl = NonDominatedSorting().debLinearMemorySorting
  }
  def createJensenFortin(criteria: Int): NonDominatedSorter = new NonDominatedSorter(criteria) {
    protected def sortingImpl = NonDominatedSorting().jensenFortinSorting
  }
  def createJensenFortinBuzdalov(criteria: Int): NonDominatedSorter = new NonDominatedSorter(criteria) {
    protected def sortingImpl = NonDominatedSorting().jensenFortinBuzdalovSorting
  }
}
