package ru.ifmo.cma.problems

import breeze.linalg.DenseVector
import ru.ifmo.cma.ProblemWithKnownOptimum

case class Sphere(
  knownOptimumLocation: DenseVector[Double],
  lowerBounds: DenseVector[Double],
  upperBounds: DenseVector[Double]
) extends ProblemWithKnownOptimum {
  override def applyImpl(arg: DenseVector[Double]): Double = {
    val diff = arg - knownOptimumLocation
    diff dot diff
  }
}

object Sphere {
  def apply(knownOptimumLocation: DenseVector[Double]): Sphere = Sphere(
    knownOptimumLocation,
    DenseVector.fill(knownOptimumLocation.length)(Double.NegativeInfinity),
    DenseVector.fill(knownOptimumLocation.length)(Double.PositiveInfinity)
  )
}
