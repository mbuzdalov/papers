package ru.ifmo.cma.problems

import breeze.linalg.{DenseVector, sum}
import ru.ifmo.cma.ProblemWithKnownOptimum

case class Ellipsoid(
  lowerBounds: DenseVector[Double],
  upperBounds: DenseVector[Double]
) extends ProblemWithKnownOptimum {
  assert(lowerBounds.length == upperBounds.length)

  private[this] lazy val N = lowerBounds.length
  private[this] lazy val weights = DenseVector.tabulate(lowerBounds.length)(i => math.pow(1e6, i / (N - 1)))

  override final def knownOptimumLocation: DenseVector[Double] = DenseVector.zeros(N)
  override protected def applyImpl(arg: DenseVector[Double]): Double = sum(arg * arg * weights)
}

object Ellipsoid {
  def apply(dimension: Int): Ellipsoid = Ellipsoid(
    DenseVector.fill(dimension)(Double.NegativeInfinity),
    DenseVector.fill(dimension)(Double.PositiveInfinity)
  )
}
