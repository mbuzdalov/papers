package ru.ifmo.cma

import breeze.linalg.DenseVector
import org.scalatest._

class CMATest extends FlatSpec with Matchers {
  trait ProblemWithKnownOptimum extends Problem {
    override final val dimension: Int = knownOptimumLocation.length
    override final val knownOptimum: Some[Double] = Some(apply(knownOptimumLocation))
    def knownOptimumLocation: DenseVector[Double]
  }

  case class Sphere(knownOptimumLocation: DenseVector[Double]) extends ProblemWithKnownOptimum {
    override def apply(arg: DenseVector[Double]): Double = {
      val diff = arg - knownOptimumLocation
      diff dot diff
    }
  }

  def validateCMA(problem: ProblemWithKnownOptimum, tolerance: Double): Unit = {
    val toleranceSq = math.sqrt(tolerance)
    val (point, value) = new CMA(problem).minimize(DenseVector.zeros(problem.dimension), 1, 1000, tolerance)
    val optimum = problem.knownOptimumLocation
    val expectedValue = problem.knownOptimum.get
    (value - expectedValue) should (be >= -tolerance and be <= tolerance)
    (point - optimum).foreach(_ should (be >= -toleranceSq and be <= toleranceSq))
  }

  "CMA" should "find optimum of a 2D sphere function" in validateCMA(Sphere(DenseVector(2.0, 3.0)), 0)
     it should "find optimum of a 3D sphere function" in validateCMA(Sphere(DenseVector(4.0, 5.0, 6.0)), 0)
     it should "find optimum of a 5D sphere function" in validateCMA(Sphere(DenseVector(21.0, 42.0, 15.0, -12.5, -14.2)), 1e-15)
     it should "find optimum of a 20D sphere function" in validateCMA(Sphere(DenseVector.fill(20, 42.42)), 1e-9)
}
