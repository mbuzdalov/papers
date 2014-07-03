package proteins

/**
 * Utilities useful for protein intersection programming.
 *
 * @author Maxim Buzdalov
 */
object IntersectionUtils {
  val eps = 1e-9

  def distanceBetweenSegments(p1: Point3, p2: Point3, q1: Point3, q2: Point3) = {
    val (u, v, w) = (p2 - p1, q2 - q1, p1 - q1)
    val (a, b, c, d, e) = (u * u, u * v, v * v, u * w, v * w)
    val det = a * c - b * b
    if (math.abs(det) < eps) {
      val (q1p1, q1p2, q2p1, q2p2) = (q1 - p1, q1 - p2, q2 - p1, q2 - p2)
      math.sqrt(
        math.min(math.min(math.min(
          if ((q1p1 * v) * (q2p1 * v) >= 0) Double.PositiveInfinity else (q1p1 ^ q2p1).length2 / v.length2,
          if ((q1p2 * v) * (q2p2 * v) >= 0) Double.PositiveInfinity else (q1p2 ^ q2p2).length2 / v.length2
        ), math.min(
          if ((q1p1 * u) * (q1p2 * u) >= 0) Double.PositiveInfinity else (q1p1 ^ q1p2).length2 / u.length2,
          if ((q2p1 * u) * (q2p2 * u) >= 0) Double.PositiveInfinity else (q2p1 ^ q2p2).length2 / u.length2
        )), math.min(
          math.min(q1p1.length2, q2p1.length2), math.min(q1p2.length2, q2p2.length2)
        ))
      )
    } else {
      val (sc, tc) = ((b * e - c * d) / det, (a * e - b * d) / det)
      val ps = if (sc <= eps) p1 else if (sc >= 1 - eps) p2 else p1 + u * sc
      val pt = if (tc <= eps) q1 else if (tc >= 1 - eps) q2 else q1 + v * tc
      (ps - pt).length
    }
  }
}
