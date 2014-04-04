package proteins.intersection

import proteins.Point3

/**
 * A pair of segments to be intersected
 * @author Maxim Buzdalov
 */
case class SegmentPair(index: Int, p1: Point3, p2: Point3, p3: Point3, p4: Point3) {
  val minX = math.min(math.min(p1.x, p2.x), math.min(p3.x, p4.x))
  val minY = math.min(math.min(p1.y, p2.y), math.min(p3.y, p4.y))
  val minZ = math.min(math.min(p1.z, p2.z), math.min(p3.z, p4.z))
  val maxX = math.max(math.max(p1.x, p2.x), math.max(p3.x, p4.x))
  val maxY = math.max(math.max(p1.y, p2.y), math.max(p3.y, p4.y))
  val maxZ = math.max(math.max(p1.z, p2.z), math.max(p3.z, p4.z))
}
