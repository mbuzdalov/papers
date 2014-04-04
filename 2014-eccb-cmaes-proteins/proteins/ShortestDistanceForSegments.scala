package proteins

/**
 * This is a trait for methods to compute the shortest distance between two pairs of segments.
 *
 * @author Maxim Buzdalov
 */
trait ShortestDistanceForSegments {
  def computeShortestDistanceForSegments(pStart1: Point3, pStart2: Point3, pFinish1: Point3, pFinish2: Point3,
                                         qStart1: Point3, qStart2: Point3, qFinish1: Point3, qFinish2: Point3,
                                         seekDistancesBelow: Double): Double
}
