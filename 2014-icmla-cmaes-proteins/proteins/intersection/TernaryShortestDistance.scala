package proteins.intersection

import scala.annotation.tailrec

import proteins.{Point3, ShortestDistanceForSegments}
import proteins.IntersectionUtils._

/**
 * Uses an exact distance for a pair of segments for fixed t. Then uses splitting of [0; 1] segment for t into 10 parts,
 * after that uses ternary search for the most promising ones.
 *
 * @author Maxim Buzdalov
 */
trait TernaryShortestDistance extends ShortestDistanceForSegments {
  def computeShortestDistanceForSegments(pStart1: Point3, pStart2: Point3, pFinish1: Point3, pFinish2: Point3,
                              qStart1: Point3, qStart2: Point3, qFinish1: Point3, qFinish2: Point3,
                              seekDistancesBelow: Double) = {
    val p31 = pFinish1 - pStart1
    val p42 = pFinish2 - pStart2
    val q31 = qFinish1 - qStart1
    val q42 = qFinish2 - qStart2

    @inline
    def fun(t: Double) = distanceBetweenSegments(pStart1 + p31 * t, pStart2 + p42 * t,
      qStart1 + q31 * t, qStart2 + q42 * t)

    @tailrec
    def ternary(left: Double, right: Double, remains: Int = 20): Double = {
      if (remains == 0) fun((left + right) / 2) else {
        val midL = (left * 2 + right) / 3
        val midR = (left + 2 * right) / 3
        if (fun(midL) < fun(midR)) {
          ternary(left, midR, remains - 1)
        } else {
          ternary(midL, right, remains - 1)
        }
      }
    }

    val intervals = 10
    val (minDist, minLoc) = (0 to intervals) map (t => (fun(t.toDouble / intervals), t)) minBy (_._1)
    if (minDist > 1.5 * seekDistancesBelow) {
      minDist
    } else math.min(
      if (minLoc == 0)         Double.PositiveInfinity else ternary((minLoc - 1.0) / intervals, (minLoc - 0.0) / intervals),
      if (minLoc == intervals) Double.PositiveInfinity else ternary((minLoc + 0.0) / intervals, (minLoc + 1.0) / intervals)
    )
  }
}
