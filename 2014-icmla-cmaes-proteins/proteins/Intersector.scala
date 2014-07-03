package proteins

/**
 * A trait for a thing that computes intersections for two chains
 *
 * @author Maxim Buzdalov
 */
trait Intersector {
  def computeIntersectionPenalty(
    startChain: IndexedSeq[Point3],
    finishChain: IndexedSeq[Point3],
    seekForDistancesBelow: Double
  ): Double
}

object Intersector {
  trait Empty extends Intersector {
    def computeIntersectionPenalty(
      startChain: IndexedSeq[Point3],
      finishChain: IndexedSeq[Point3],
      seekForDistancesBelow: Double
    ) = 0
  }
}
