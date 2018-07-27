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

      def findExcess(i: Int, v: Double) = {
        val lv = lower(i)
        val uv = upper(i)
        if (v < lv) {
          (lv - v) / (uv - lv)
        } else if (v > uv) {
          (v - uv) / (uv - lv)
        } else 0.0
      }

      def mirror(i: Int, v: Double) = Geometry.mirror(v, lower(i), upper(i))

      val excessLength = norm(x.mapPairs(findExcess)) * math.sqrt(problem.dimension)
      val realX = x.mapPairs(mirror)

      assert(problem.canApply(realX), s"x = $x, realX = $realX, lower = $lower, upper = $upper")

      // Penalties must contain an additive component.
      // Reason: 1) in exact optima, any excess length can be suppressed
      //         2) what if fitness is negative?
      val fitness = problem(realX)
      (x, fitness + (1 + fitness * fitness) * excessLength, z)
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
