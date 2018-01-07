package ru.ifmo.cma

import scala.annotation.tailrec
import breeze.stats.distributions.Rand

class CMAWithResampling(problem: Problem) extends CMA(problem) {
  @tailrec
  private[this] def sampleImpl(meanVector: Vector, bd: Matrix, sigma: Double): (Vector, Double, Vector) = {
    val z = Vector.rand(problem.dimension, Rand.gaussian)
    val x = meanVector + sigma * z
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
