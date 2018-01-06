package ru.ifmo.cma

import breeze.linalg.DenseVector
import org.scalatest._

class CMATest extends FlatSpec with Matchers {
  trait ProblemWithKnownOptimum extends Problem {
    override final val dimension: Int = knownOptimum.length
    def knownOptimum: DenseVector[Double]
  }

  case class Sphere(knownOptimum: DenseVector[Double]) extends ProblemWithKnownOptimum {
    override def apply(arg: DenseVector[Double]): Double = {
      val diff = arg - knownOptimum
      diff dot diff
    }
  }

  def validateCMA(problem: ProblemWithKnownOptimum): Unit = {
    val (point, value) = new CMA(problem).optimize(DenseVector.zeros(problem.dimension), 1, 1000)
    val optimum = problem.knownOptimum
    val expectedValue = problem(optimum)
    value should (be >= expectedValue - 1e-9 and be <= expectedValue + 1e-9)
    (point - optimum).foreach(_ should (be >= -1e-5 and be <= 1e-5))
  }

  "CMA" should "find optimum of a 2D square function" in validateCMA(Sphere(DenseVector(2.0, 3.0)))
     it should "find optimum of a 3D square function" in validateCMA(Sphere(DenseVector(4.0, 5.0, 6.0)))
     it should "find optimum of a 5D square function" in validateCMA(Sphere(DenseVector(21.0, 42.0, 15.0, -12.5, -14.2)))
     it should "find optimum of a 20D square function" in validateCMA(Sphere(DenseVector.fill(20, 42.42)))
}
