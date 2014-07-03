package proteins

/**
 * A sine-cosine pair.
 *
 * @author Maxim Buzdalov
 */
class SinCos private (val sin: Double, val cos: Double) {
  def angle = math.atan2(sin, cos)
  def + (that: SinCos) = new SinCos(sin * that.cos + cos * that.sin, cos * that.cos - sin * that.sin)
  def - (that: SinCos) = new SinCos(sin * that.cos - cos * that.sin, cos * that.cos + sin * that.sin)
}

object SinCos {
  def apply(sin: Double, cos: Double) = {
    val invSumSq = 1.0 / math.sqrt(sin * sin + cos * cos)
    new SinCos(sin * invSumSq, cos * invSumSq)
  }
  def fromAngle(angle: Double) = new SinCos(math.sin(angle), math.cos(angle))
}