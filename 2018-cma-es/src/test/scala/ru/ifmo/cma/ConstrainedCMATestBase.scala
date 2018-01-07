package ru.ifmo.cma

import breeze.linalg.DenseVector
import org.scalatest.{FlatSpec, Matchers}
import ru.ifmo.cma.problems.Sphere

abstract class ConstrainedCMATestBase extends FlatSpec with Matchers {
  def name: String
  def newCMA(problem: Problem): CMA

  def validateCMA(problem: ProblemWithKnownOptimum, tolerance: Double): Unit = {
    val toleranceSq = math.sqrt(tolerance)
    val cma = newCMA(problem)
    val (point, value) = cma.minimize(DenseVector.zeros(problem.dimension), 1, 1000, tolerance)
    val optimum = problem.knownOptimumLocation
    val expectedValue = problem.knownOptimum.get
    (value - expectedValue) should (be >= -tolerance and be <= tolerance)
    (point - optimum).foreach(_ should (be >= -toleranceSq and be <= toleranceSq))
  }

  (name + " on unconstrained functions") should "optimize a 2D sphere function" in validateCMA(Sphere(DenseVector(2.0, 3.0)), 0)
  it should "optimize a 3D sphere function" in validateCMA(Sphere(DenseVector(4.0, 5.0, 6.0)), 0)
  it should "optimize a 5D sphere function" in validateCMA(Sphere(DenseVector.fill(5, -11.3)), 1e-15)
  it should "optimize a 20D sphere function" in validateCMA(Sphere(DenseVector.fill(20, 42.42)), 1e-9)

  (name + " on constrained 2D sphere functions") should "work with 0 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(2.0, 3.0),
      DenseVector(1.0, 2.0),
      DenseVector(4.0, 3.5)
    ), 0)
  }
  it should "work with 1 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(2.0, 3.0),
      DenseVector(1.0, 2.0),
      DenseVector(4.0, 3.0)
    ), 0)
  }
  it should "work with 2 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(2.0, 3.0),
      DenseVector(1.0, 2.0),
      DenseVector(2.0, 3.0)
    ), 0)
  }

  (name + " on constrained 5D sphere functions") should "work with 0 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(5.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(1.0, 1.0, 1.0, 1.0, 0.0),
      DenseVector(10.0, 10.0, 10.0, 10.0, 10.0)
    ), 1e-15)
  }

  it should "work with 1 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(5.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(1.0, 1.0, 1.0, 1.0, 1.0),
      DenseVector(10.0, 10.0, 10.0, 10.0, 10.0)
    ), 1e-15)
  }

  it should "work with 2 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(5.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(1.0, 1.0, 1.0, 2.0, 1.0),
      DenseVector(10.0, 10.0, 10.0, 10.0, 10.0)
    ), 1e-15)
  }

  it should "work with 3 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(5.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(1.0, 1.0, 3.0, 2.0, 1.0),
      DenseVector(10.0, 10.0, 10.0, 10.0, 10.0)
    ), 1e-15)
  }

  it should "work with 4 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(5.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(1.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(10.0, 10.0, 10.0, 10.0, 10.0)
    ), 1e-15)
  }

  it should "work with 5 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(5.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(5.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(10.0, 10.0, 10.0, 10.0, 10.0)
    ), 1e-15)
  }

  (name + " on constrained 20D sphere functions") should "work with 0 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(5, 5, 5, 5,  5, 5, 5, 5,  5, 5, 5, 5,  5, 5, 5, 5,  5, 5, 5, 5),
      DenseVector(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
      DenseVector(7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7)
    ), 1e-9)
  }

  it should "work with 4 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(1, 7, 7, 1,  5, 5, 5, 5,  5, 5, 5, 5,  5, 5, 5, 5,  5, 5, 5, 5),
      DenseVector(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
      DenseVector(7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7)
    ), 1e-9)
  }

  it should "work with 8 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(1, 7, 7, 1,  7, 1, 7, 1,  5, 5, 5, 5,  5, 5, 5, 5,  5, 5, 5, 5),
      DenseVector(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
      DenseVector(7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7)
    ), 1e-9)
  }

  it should "work with 12 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(1, 7, 7, 1,  7, 1, 7, 1,  1, 1, 7, 1,  5, 5, 5, 5,  5, 5, 5, 5),
      DenseVector(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
      DenseVector(7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7)
    ), 1e-9)
  }

  it should "work with 16 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(1, 7, 7, 1,  7, 1, 7, 1,  1, 1, 7, 1,  7, 7, 7, 1,  5, 5, 5, 5),
      DenseVector(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
      DenseVector(7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7)
    ), 1e-9)
  }

  it should "work with 20 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(1, 7, 7, 1,  7, 1, 7, 1,  1, 1, 7, 1,  7, 7, 7, 1,  1, 7, 7, 7),
      DenseVector(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
      DenseVector(7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7)
    ), 1e-9)
  }
}
