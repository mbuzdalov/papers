package ru.ifmo.cma.algorithms

import breeze.linalg.DenseVector
import breeze.stats.distributions.Rand
import ru.ifmo.cma.{CMALike, Problem}

import scala.annotation.tailrec

class CMAWithResampling protected (problem: Problem) extends CMA(problem) {
  @tailrec
  private[this] def sampleImpl(meanVector: Vector, bd: Matrix, sigma: Double): (Vector, Double, Vector) = {
    val z = Vector.rand(problem.dimension, Rand.gaussian)
    val x = meanVector + sigma * (bd * z)
    if (problem.canApply(x)) {
      val y = problem(x)
      (x, y, z)
    } else {
      sampleImpl(meanVector, bd, sigma)
    }
  }

  override protected def sampleXYZ(meanVector: Vector, bd: Matrix, sigma: Double): (Vector, Double, Vector) = {
    sampleImpl(meanVector, bd, sigma)
  }
}

object CMAWithResampling extends CMALike {
  override def name: String = "CMA with Resampling"

  override def minimize(
    problem: Problem,
    initial: DenseVector[Double],
    sigma: Double,
    iterations: Int,
    fitnessThreshold: Double
  ): (DenseVector[Double], Double, Seq[Double]) = {
    val cma = new CMAWithResampling(problem)
    val (point, value) = cma.minimize(initial, sigma, iterations, fitnessThreshold)
    (point, value, cma.fitnessHistory)
  }
}
