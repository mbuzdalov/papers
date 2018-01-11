package ru.ifmo.cma.problems

import breeze.linalg.DenseVector
import ru.ifmo.cma.ProblemWithKnownOptimum

case class Sphere(
  lowerBounds: DenseVector[Double],
  upperBounds: DenseVector[Double]
) extends ProblemWithKnownOptimum {
  assert(lowerBounds.length == upperBounds.length)
  override protected def applyImpl(arg: DenseVector[Double]): Double = arg dot arg
  override def knownOptimumLocation: DenseVector[Double] = DenseVector.zeros(lowerBounds.length)
  override def name: String = "Sphere"
}

object Sphere {
  def apply(dimension: Int): Sphere = Sphere(
    DenseVector.fill(dimension)(Double.NegativeInfinity),
    DenseVector.fill(dimension)(Double.PositiveInfinity)
  )
}
