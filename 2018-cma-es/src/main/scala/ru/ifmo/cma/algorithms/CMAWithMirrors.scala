package ru.ifmo.cma.algorithms

import breeze.linalg.{DenseVector, norm}
import breeze.stats.distributions.Rand
import ru.ifmo.cma.util.Geometry
import ru.ifmo.cma.{CMALike, Problem}

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

      def mirror(i: Int, v: Double) = Geometry.mirror(v, lower(i), upper(i))

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
