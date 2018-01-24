package ru.ifmo.cma

import breeze.linalg.{DenseVector, min}
import breeze.stats.distributions.Rand

class CMAWithRescalingToBoundary protected (problem: Problem) extends CMA(problem) {
  private[this] def sampleImpl(meanVector: Vector, bd: Matrix, sigma: Double): (Vector, Double, Vector) = {
    val z = Vector.rand(problem.dimension, Rand.gaussian)
    val x = meanVector + sigma * (bd * z)
    if (problem.canApply(x)) {
      val y = problem(x)
      (x, y, z)
    } else {
      val lower = problem.lowerBounds
      val upper = problem.upperBounds

      def fitSquare(i: Int, v: Double) = math.min(upper(i), math.max(lower(i), v))
      def findScale(i: Int, v: Double) = if (v < lower(i)) {
        (meanVector(i) - lower(i)) / (meanVector(i) - v)
      } else if (v > upper(i)) {
        (meanVector(i) - upper(i)) / (meanVector(i) - v)
      } else 1.0

      val scaleVector = x.mapPairs(findScale)
      val scale = min(scaleVector)
      val newZ = z * scale
      val newX = (meanVector + sigma * (bd * newZ)).mapPairs(fitSquare)
      assert(problem.canApply(newX), s"x = $newX, lower = $lower, upper = $upper")
      (newX, problem(newX), newZ)
    }
  }

  override protected def sampleXYZ(meanVector: Vector, bd: Matrix, sigma: Double): (Vector, Double, Vector) = {
    sampleImpl(meanVector, bd, sigma)
  }
}

object CMAWithRescalingToBoundary extends CMALike {
  override def name: String = "CMA with Rescaling to Boundary"

  override def minimize(
    problem: Problem,
    initial: DenseVector[Double],
    sigma: Double,
    iterations: Int,
    fitnessThreshold: Double
  ): (DenseVector[Double], Double, Seq[Double]) = {
    val cma = new CMAWithRescalingToBoundary(problem)
    val (point, value) = cma.minimize(initial, sigma, iterations, fitnessThreshold)
    (point, value, cma.fitnessHistory)
  }
}
