package proteins

/**
 * A 3D point with coordinates of type Double.
 *
 * @author Maxim Buzdalov
 */
case class Point3(x: Double, y: Double, z: Double) {
  def + (that: Point3) = Point3(x + that.x, y + that.y, z + that.z)
  def - (that: Point3) = Point3(x - that.x, y - that.y, z - that.z)
  def * (that: Point3) = x * that.x + y * that.y + z * that.z
  def ^ (that: Point3) = Point3(y * that.z - z * that.y, z * that.x - x * that.z, x * that.y - y * that.x)
  def * (that: Double) = Point3(x * that, y * that, z * that)
  def / (that: Double) = Point3(x / that, y / that, z / that)
  def unary_- = Point3(-x, -y, -z)

  def normalized = this / length
  def length2 = x * x + y * y + z * z
  def length = math.sqrt(length2)
}
