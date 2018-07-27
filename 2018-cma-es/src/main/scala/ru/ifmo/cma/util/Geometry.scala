package ru.ifmo.cma.util

object Geometry {
  def sharpBound(v: Double, lower: Double, upper: Double): Double = math.max(lower, math.min(upper, v))

  def interpolate(v: Double, originalLower: Double, originalUpper: Double, newLower: Double, newUpper: Double): Double = {
    val rv = (v - originalLower) / (originalUpper - originalLower) * (newUpper - newLower) + newLower
    sharpBound(rv, newLower, newUpper)
  }

  def mirror(v: Double, lowerBound: Double, upperBound: Double): Double = {
    val normalized = (v - lowerBound) / (upperBound - lowerBound)
    val inside0 = normalized % 2
    val inside1 = if (inside0 < 0) inside0 + 2 else inside0
    val mirrored = if (inside1 > 1) 2 - inside1 else inside1
    val result = mirrored * (upperBound - lowerBound) + lowerBound
    sharpBound(result, lowerBound, upperBound)
  }
}
