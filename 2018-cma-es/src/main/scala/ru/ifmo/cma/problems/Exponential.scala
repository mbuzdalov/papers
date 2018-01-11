package ru.ifmo.cma.problems

import breeze.linalg.{DenseVector, sum}
import ru.ifmo.cma.ProblemWithKnownOptimum

case class Exponential(
  lowerBounds: DenseVector[Double],
  upperBounds: DenseVector[Double]
) extends ProblemWithKnownOptimum {
  assert(lowerBounds.length == upperBounds.length)

  private[this] lazy val N = lowerBounds.length

  override def knownOptimumLocation: DenseVector[Double] = DenseVector.zeros(N)
  override protected def applyImpl(arg: DenseVector[Double]): Double = {
    sum(arg.map(x => if (x > 1) math.exp(20 * (x - 1)) else x * x))
  }

  override def name: String = "Exponential"
}

object Exponential {
  def apply(dimension: Int): Exponential = Exponential(
    DenseVector.fill(dimension)(Double.NegativeInfinity),
    DenseVector.fill(dimension)(Double.PositiveInfinity)
  )
}
