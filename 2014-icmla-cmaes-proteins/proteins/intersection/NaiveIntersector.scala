package proteins.intersection

import proteins.{ShortestDistanceForSegments, Point3, Intersector}

/**
 * Computes the intersections using a simple pair-to-pair algorithm with very little optimizations.
 *
 * @author Maxim Buzdalov
 */
trait NaiveIntersector extends Intersector {
  needs: ShortestDistanceForSegments =>

  def computeIntersectionPenalty(startChain: IndexedSeq[Point3], finishChain: IndexedSeq[Point3], distanceThreshold: Double) = {
    require(startChain.size == finishChain.size, "chains must have equal size")

    var result = 0.0

    val segments = 0 until startChain.size - 1 map { i =>
      SegmentPair(i, startChain(i), startChain(i + 1), finishChain(i), finishChain(i + 1))
    } sortBy (_.minX)

    for (oneIndex <- 0 until segments.size) {
      val s1 = segments(oneIndex)
      var twoIndex = oneIndex + 1
      while (twoIndex < segments.size && segments(twoIndex).minX < s1.maxX + distanceThreshold) {
        val s2 = segments(twoIndex)

        if (math.abs(s1.index - s2.index) > 2 &&
            s1.maxY + distanceThreshold > s2.minY && s1.maxZ + distanceThreshold > s2.minZ &&
            s2.maxY + distanceThreshold > s1.minY && s2.maxZ + distanceThreshold > s1.minZ) {
          val distance = computeShortestDistanceForSegments(s1.p1, s1.p2, s1.p3, s1.p4, s2.p1, s2.p2, s2.p3, s2.p4,
            distanceThreshold)
          val addend = if (distance >= distanceThreshold) 0.0 else {
            (distanceThreshold - distance) / distanceThreshold
          }
          result += addend
        }

        twoIndex += 1
      }
    }

    result
  }
}
