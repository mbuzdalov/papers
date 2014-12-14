package ru.ifmo.ctd.ngp.util

/**
 * Utilities for statistic
 */
object StatUtil {
  def ranks[T: Numeric](seq: Iterable[T]): IndexedSeq[Double] = {
    val sortedWithIndex: IndexedSeq[(T, Iterable[(T, Int)])] = seq.zipWithIndex.groupBy(_._1).toIndexedSeq.sortBy(_._1)
    val rankOfIndex = IndexedSeq.newBuilder[(Int, Double)]

    def process(idx: Int, soFar: Int): Unit = {
      if (idx < sortedWithIndex.size) {
        val (_, itr) = sortedWithIndex(idx)
        val rank = soFar + (1.0 + itr.size) / 2
        for ((_, i) <- itr) {
          rankOfIndex += i -> rank
        }
        process(idx + 1, soFar + itr.size)
      }
    }
    process(0, 0)
    rankOfIndex.result().sortBy(_._1).map(_._2)
  }

  def frequencies[T: Numeric](seq: Iterable[T]): IndexedSeq[Int] = seq.groupBy(identity).map(_._2.size).toIndexedSeq
}
