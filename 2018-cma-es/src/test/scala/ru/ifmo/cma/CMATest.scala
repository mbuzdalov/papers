package ru.ifmo.cma

import breeze.linalg.DenseVector
import org.scalatest._
import ru.ifmo.cma.problems._

class CMATest extends FlatSpec with Matchers {
  def validateCMA(problem: ProblemWithKnownOptimum, tolerance: Double): Unit = {
    val toleranceSq = math.sqrt(tolerance)
    val (point, value) = new CMA(problem).minimize(DenseVector.zeros(problem.dimension), 1, 1000, tolerance)
    val optimum = problem.knownOptimumLocation
    val expectedValue = problem.knownOptimum.get
    (value - expectedValue) should (be >= -tolerance and be <= tolerance)
    (point - optimum).foreach(_ should (be >= -toleranceSq and be <= toleranceSq))
  }

  "CMA" should "optimize a 2D sphere function" in validateCMA(Sphere(DenseVector(2.0, 3.0)), 0)
     it should "optimize a 3D sphere function" in validateCMA(Sphere(DenseVector(4.0, 5.0, 6.0)), 0)
     it should "optimize a 5D sphere function" in validateCMA(Sphere(DenseVector.fill(5, -11.3)), 1e-15)
     it should "optimize a 20D sphere function" in validateCMA(Sphere(DenseVector.fill(20, 42.42)), 1e-9)
}
