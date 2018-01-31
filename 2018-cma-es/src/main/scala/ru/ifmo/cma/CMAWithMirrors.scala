package ru.ifmo.cma

import breeze.linalg.{DenseVector, norm}
import breeze.stats.distributions.Rand

class CMAWithMirrors protected (problem: Problem) extends CMA(problem) {
  private[this] def sampleImpl(meanVector: Vector, bd: Matrix, sigma: Double): (Vector, Double, Vector) = {
    val z = Vector.rand(problem.dimension, Rand.gaussian)
    val x = meanVector + sigma * (bd * z)
    if (problem.canApply(x)) {
      val y = problem(x)
      (x, y, z)
    } else {
      val lower = problem.lowerBounds
      val upper = problem.upperBounds

      def findExcess(i: Int, v: Double) = if (v < lower(i)) {
        lower(i) - v
      } else if (v > upper(i)) {
        v - upper(i)
      } else 0.0

      // 1. Normalize boundaries to [0; 1].
      // 2. Parity determiner p = floor(v).
      // 3. Location determiner q = v % 1.    -- IEEEremainder
      // 4. If floor(x) is even, retain x, otherwise set it to 1 - x.
      // 5. Get it back to [lower; upper].
      def mirror(i: Int, v: Double) = {
        val lv = lower(i)
        val uv = upper(i)
        val normalized = (v - lv) / (uv - lv)
        val parity = normalized.toInt
        val remainder = normalized.abs % 1
        val mirrored = if ((parity & 1) == 1) 1 - remainder else remainder
        val result = mirrored * (uv - lv) + lv
        math.max(lv, math.min(uv, result))
      }

      // Scale is for penalties.
      // Penalties must be additive, not multiplicative.
      // Thus, the penalty must be something like "(1 - scale) * norm(x)" or so.
      val excessLength = norm(x.mapPairs(findExcess)) * 1e9
      val realX = x.mapPairs(mirror)

      assert(problem.canApply(realX), s"x = $x, realX = $realX, lower = $lower, upper = $upper")
      (x, problem(realX) + excessLength, z)
    }
  }

  override protected def sampleXYZ(meanVector: Vector, bd: Matrix, sigma: Double): (Vector, Double, Vector) = {
    sampleImpl(meanVector, bd, sigma)
  }
}

object CMAWithMirrors extends CMALike {
  override def name: String = "CMA with Mirrors"

  override def minimize(
    problem: Problem,
    initial: DenseVector[Double],
    sigma: Double,
    iterations: Int,
    fitnessThreshold: Double
  ): (DenseVector[Double], Double, Seq[Double]) = {
    val cma = new CMAWithMirrors(problem)
    val (point, value) = cma.minimize(initial, sigma, iterations, fitnessThreshold)
    (point, value, cma.fitnessHistory)
  }
}
