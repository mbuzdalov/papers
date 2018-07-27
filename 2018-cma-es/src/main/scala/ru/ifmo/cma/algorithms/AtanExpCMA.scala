package ru.ifmo.cma.algorithms

import breeze.linalg.DenseVector
import ru.ifmo.cma.util.Geometry
import ru.ifmo.cma.{CMALike, Problem}

object AtanExpCMA extends CMALike {
  override def name: String = "CMA with atan/exp mapping"

  override def minimize(problem: Problem,
                        initial: DenseVector[Double],
                        sigma: Double,
                        iterations: Int,
                        fitnessThreshold: Double): (DenseVector[Double], Double, Seq[Double]) = {
    val realInitial = if (problem.canApply(initial)) initial else (problem.lowerBounds + problem.upperBounds) / 2.0

    val lower = problem.lowerBounds
    val upper = problem.upperBounds

    val halfPi = math.Pi / 2

    // Maps the unrestricted value `v` to a bounded value between `lower(i)` and `upper(i)`.
    def mapDomain(i: Int, v: Double): Double = {
      val lv = lower(i)
      val uv = upper(i)
      if (lv.isNegInfinity && uv.isPosInfinity) {
        v
      } else if (lv.isNegInfinity) {
        -math.exp(-v) + uv
      } else if (uv.isPosInfinity) {
        math.exp(v) + lv
      } else {
        val atan = math.atan(v)
        Geometry.interpolate(atan, -halfPi, halfPi, lv, uv)
      }
    }

    // Maps back the bounded value between `lower(i)` and `upper(i)` to an unrestricted value.
    // mapBack o mapDomain = identity
    def mapBack(i: Int, v: Double): Double = {
      val lv = lower(i)
      val uv = upper(i)
      if (lv.isNegInfinity && uv.isPosInfinity) {
        v
      } else if (lv.isNegInfinity) {
        -math.log(uv - v)
      } else if (uv.isPosInfinity) {
        math.log(v - lv)
      } else {
        math.tan(Geometry.interpolate(v, lv, uv, -halfPi, halfPi))
      }
    }

    val mappedInitial = realInitial.mapPairs(mapBack)

    val (point, value, history) = CMA.minimize(
      problem = new Problem {
        override protected def applyImpl(arg: DenseVector[Double]): Double = problem.apply(arg.mapPairs(mapDomain))
        override val name: String = problem.name + " (atan/exp mapped)"
        override val dimension: Int = problem.dimension
        override val lowerBounds: DenseVector[Double] = DenseVector.fill(dimension)(Double.NegativeInfinity)
        override val upperBounds: DenseVector[Double] = DenseVector.fill(dimension)(Double.PositiveInfinity)
      },
      initial = mappedInitial,
      sigma = sigma,
      iterations = iterations,
      fitnessThreshold = fitnessThreshold
    )

    val pointInOriginal = point.mapPairs(mapDomain)
    (pointInOriginal, value, history)
  }
}
