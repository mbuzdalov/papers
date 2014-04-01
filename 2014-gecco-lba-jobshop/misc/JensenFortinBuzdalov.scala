package ru.ifmo.ctd.ngp.opt.multicriteria.nds

import java.util.{Arrays => JUArrays, TreeSet => JTreeSet, Iterator => JIterator, Comparator}

import scala.annotation.tailrec

import ru.ifmo.ctd.ngp.opt.Evaluated
import ru.ifmo.ctd.ngp.opt.multicriteria.{NonDominatedSorting, MultipleCriteria}
import ru.ifmo.ctd.ngp.opt.types.{CodomainType, DomainType}
import ru.ifmo.ctd.ngp.opt.util._

/**
 * An implementation of a non-dominated sorting algorithm according to Jensen
 * with patches from Fortin et al and Buzdalov.
 *
 * ==================================================================================================
 * This is a file provided for reference only, it will not compile without its dependencies.
 * The compiled version, along with all needed files, is located in the jar file "lib/ngp-core.jar".
 * The source code of the library is available on request.
 *
 * Future versions of the library will be located at https://github.com/mbuzdalov/ngp.
 * ==================================================================================================
 *
 * @author Maxim Buzdalov
 */
class JensenFortinBuzdalov[D : DomainType, C : CodomainType : MultipleCriteria] extends NonDominatedSorting[D, C] {
  type E = Evaluated[D, C]
  private val mc = implicitly[MultipleCriteria[C]]
  private val M = mc.numberOfCriteria
  @inline
  private def cmp(a: E, b: E, index: Int) = {
    mc.orderingForCriterion(index).compare(a.output, b.output)
  }
  private def sortIndices(lex: Array[E], front: Array[Int], idx: Array[Int], eqComp: Array[Int]) {
    val swap1, swap2, swap3 = Array.ofDim[Int](idx.length)
    @inline
    def cmpI(a: Int, b: Int, index: Int) = cmp(lex(a), lex(b), index)
    val comp = new Comparator[Int] {
      def compare(o1: Int, o2: Int) = cmpI(o1, o2, 1) ifZeroThen cmpI(o1, o2, 0)
    }
    @tailrec
    def cleanup(itr: JIterator[Int], thresh: Int) {
      if (itr.hasNext) {
        val v = itr.next()
        if (front(v) <= thresh) {
          itr.remove()
          cleanup(itr, thresh)
        }
      }
    }
    def updateFront(target: Int, source: Int) {
      val add = if (eqComp(target) == eqComp(source)) 0 else 1
      front(target) = math.max(front(target), add + front(source))
    }
    def sweepA(from: Int, until: Int) {
      val set = new JTreeSet[Int](comp)
      for (t <- from until until) {
        val curr = idx(t)
        val tailItr = set.tailSet(curr, true).iterator()
        if (tailItr.hasNext) {
          updateFront(curr, tailItr.next())
        }
        cleanup(set.headSet(curr, true).descendingIterator(), front(curr))
        set.add(curr)
      }
    }
    def sweepB(hFrom: Int, hUntil: Int, lFrom: Int, lUntil: Int) {
      val set = new JTreeSet[Int](comp)
      var ih = hFrom
      for (il <- lFrom until lUntil; curr = idx(il)) {
        while (ih < hUntil && idx(il) > idx(ih)) {
          val hCurr = idx(ih)
          val tailItr = set.tailSet(hCurr, true).iterator()
          if (!tailItr.hasNext || front(tailItr.next()) < front(hCurr)) {
            cleanup(set.headSet(hCurr, true).descendingIterator(), front(hCurr))
            set.add(hCurr)
          }
          ih += 1
        }
        val tailItr = set.tailSet(curr, true).iterator()
        if (tailItr.hasNext) {
          updateFront(curr, tailItr.next())
        }
      }
    }
    def medianImpl(from: Int, until: Int, k: Int) = {
      val rng = FastRandom.threadLocal()
      val index = (from + until) >>> 1
      @tailrec
      def impl(left: Int, right: Int) {
        assert(left <= index && index <= right)
        if (left < right) {
          val mid = swap1(rng.nextInt(right - left + 1) + left)
          var l = left
          var r = right
          while (l <= r) {
            while (cmpI(swap1(l), mid, k) < 0) l += 1
            while (cmpI(swap1(r), mid, k) > 0) r -= 1
            if (l <= r) {
              val tmp = swap1(l)
              swap1(l) = swap1(r)
              swap1(r) = tmp
              l += 1
              r -= 1
            }
          }
          if (index <= r) {
            impl(left, r)
          } else if (index >= l) {
            impl(l, right)
          }
        }
      }
      impl(from, until - 1)
      swap1(index)
    }
    def median(from: Int, until: Int, k: Int) = {
      System.arraycopy(idx, from, swap1, from, until - from)
      medianImpl(from, until, k)
    }
    def median2(hFrom: Int, hUntil: Int, lFrom: Int, lUntil: Int, k: Int) = {
      System.arraycopy(idx, hFrom, swap1, 0, hUntil - hFrom)
      System.arraycopy(idx, lFrom, swap1, hUntil - hFrom, lUntil - lFrom)
      medianImpl(0, lUntil - lFrom + hUntil - hFrom, k)
    }
    def splitBy(from: Int, until: Int, k: Int, median: Int) = {
      var less, equal, greater = 0
      for (i <- from until until) {
        val v = cmpI(idx(i), median, k)
        if (v < 0) {
          swap1(less) = idx(i)
          less += 1
        } else if (v == 0) {
          swap2(equal) = idx(i)
          equal += 1
        } else {
          swap3(greater) = idx(i)
          greater += 1
        }
      }
      var trg = from
      for (i <- 0 until greater) { idx(trg) = swap3(i); trg += 1 }
      for (i <- 0 until equal)   { idx(trg) = swap2(i); trg += 1 }
      for (i <- 0 until less)    { idx(trg) = swap1(i); trg += 1 }
      (greater, equal, less)
    }
    def merge(hFrom: Int, hUntil: Int, lFrom: Int, lUntil: Int) {
      assert(hUntil == lFrom)
      var hp = hFrom
      var lp = lFrom
      var sw = hFrom
      while (hp < hUntil && lp < lUntil) {
        if (idx(hp) <= idx(lp)) {
          swap1(sw) = idx(hp)
          hp += 1
        } else {
          swap1(sw) = idx(lp)
          lp += 1
        }
        sw += 1
      }
      while (hp < hUntil) {
        swap1(sw) = idx(hp)
        hp += 1
        sw += 1
      }
      while (lp < lUntil) {
        swap1(sw) = idx(lp)
        lp += 1
        sw += 1
      }
      for (i <- hFrom until lUntil) {
        idx(i) = swap1(i)
      }
    }
    def dominatesK(l: Int, r: Int, k: Int) = {
      var forall = true
      for (i <- 0 to k) {
        val v = cmpI(l, r, i)
        forall &= v >= 0
      }
      forall
    }
    def helperA(from: Int, until: Int, k: Int) {
      require(k > 0)
      if (until - from == 2) {
        val (l, r) = (idx(from), idx(from + 1))
        if (dominatesK(l, r, k)) {
          updateFront(r, l)
        }
      } else if (until - from > 2) {
        if (k == 1) {
          sweepA(from, until)
        } else if ((from until until - 1).mapAnd(i => cmpI(idx(i), idx(i + 1), k) == 0)) {
          helperA(from, until, k - 1)
        } else {
          val (g, e, _) = splitBy(from, until, k, median(from, until, k))
          val (mid1, mid2) = (from + g, from + g + e)
          helperA(from, mid1, k)
          helperB(from, mid1, mid1, mid2, k - 1)
          helperA(mid1, mid2, k)
          merge(from, mid1, mid1, mid2)
          helperB(from, mid2, mid2, until, k - 1)
          helperA(mid2, until, k)
          merge(from, mid2, mid2, until)
        }
      }
    }
    def helperB(hFrom: Int, hUntil: Int, lFrom: Int, lUntil: Int, k: Int) {
      require(k > 0)
      if (hFrom < hUntil && lFrom < lUntil) {
        if (hFrom + 1 == hUntil || lFrom + 1 == lUntil) {
          for (h <- hFrom until hUntil; ih = idx(h); l <- lFrom until lUntil; il = idx(l)) {
            if (dominatesK(ih, il, k)) {
              updateFront(il, ih)
            }
          }
        } else if (k == 1) {
          sweepB(hFrom, hUntil, lFrom, lUntil)
        } else {
          val minH = (hFrom until hUntil).mapMin(i => lex(idx(i)).output)(mc.orderingForCriterion(k))
          val maxL = (lFrom until lUntil).mapMax(i => lex(idx(i)).output)(mc.orderingForCriterion(k))
          if (mc.orderingForCriterion(k).compare(maxL, minH) > 0) {
            val med = median2(hFrom, hUntil, lFrom, lUntil, k)
            val (hG, hE, _) = splitBy(hFrom, hUntil, k, med)
            val (lG, lE, _) = splitBy(lFrom, lUntil, k, med)
            val (hMid1, hMid2) = (hFrom + hG, hFrom + hG + hE)
            val (lMid1, lMid2) = (lFrom + lG, lFrom + lG + lE)

            helperB(hFrom, hMid1, lFrom, lMid1, k)
            helperB(hFrom, hMid1, lMid1, lMid2, k - 1)
            helperB(hMid1, hMid2, lMid1, lMid2, k - 1)
            merge(hFrom, hMid1, hMid1, hMid2)
            merge(lFrom, lMid1, lMid1, lMid2)
            helperB(hFrom, hMid2, lMid2, lUntil, k - 1)
            helperB(hMid2, hUntil, lMid2, lUntil, k)
            merge(hFrom, hMid2, hMid2, hUntil)
            merge(lFrom, lMid2, lMid2, lUntil)
          } else {
            helperB(hFrom, hUntil, lFrom, lUntil, k - 1)
          }
        }
      }
    }
    if (M == 1) {
      System.arraycopy(idx, 0, front, 0, idx.length)
    } else {
      helperA(0, front.length, M - 1)
    }
  }
  private val lexComp = new Comparator[E] {
    def compare(l: E, r: E) = {
      @tailrec
      def impl(index: Int): Int = if (index == M) 0 else {
        val v = -cmp(l, r, index)
        if (v == 0) impl(index + 1) else v
      }
      impl(0)
    }
  }
  def apply(data: IndexedSeq[E]) = {
    val lexData = data.toArray
    JUArrays.sort(lexData, lexComp)
    val front = Array.ofDim[Int](lexData.size)
    val indices = Array.tabulate(lexData.size)(identity)
    val eqComp = Array.ofDim[Int](lexData.size)
    for (i <- 1 until lexData.size) {
      eqComp(i) = eqComp(i - 1)
      if (lexComp.compare(lexData(i - 1), lexData(i)) != 0) {
        eqComp(i) += 1
      }
    }
    sortIndices(lexData, front, indices, eqComp)
    val builders = IndexedSeq.fill(front.max + 1)(IndexedSeq.newBuilder[E])
    for (i <- 0 until front.size) {
      builders(front(i)) += lexData(i)
    }
    builders.map(_.result())
  }
}
