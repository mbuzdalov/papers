package ru.ifmo.cma

import breeze.linalg.DenseVector
import org.scalatest._
import ru.ifmo.cma.problems._

abstract class UnconstrainedCMATestBase extends FlatSpec with Matchers {
  protected val eps: Double = 1e-12

  def name: String
  def cma: CMALike

  def validateCMA(problem: ProblemWithKnownOptimum): Unit = {
    val toleranceSq = math.sqrt(eps)
    val expectedValue = problem.knownOptimum
    val (point, value) = cma.minimize(problem, DenseVector.fill(problem.dimension)(5.55), 1, 5000, expectedValue + eps)
    val optimum = problem.knownOptimumLocation
    (value - expectedValue) should (be >= -eps and be <= eps)
    (point - optimum).foreach(_ should (be >= -toleranceSq and be <= toleranceSq))
  }

  (name + " on unconstrained functions") should "optimize a 2D sphere function" in validateCMA(Sphere(DenseVector(2.0, 3.0)))
  it should "optimize a 3D sphere function" in validateCMA(Sphere(DenseVector(4.0, 5.0, 6.0)))
  it should "optimize a 5D sphere function" in validateCMA(Sphere(DenseVector.fill(5, -11.3)))
  it should "optimize a 20D sphere function" in validateCMA(Sphere(DenseVector.fill(20, 42.42)))

  it should "optimize a 2D ellipsoid function" in validateCMA(Ellipsoid(2))
  it should "optimize a 3D ellipsoid function" in validateCMA(Ellipsoid(3))
  it should "optimize a 5D ellipsoid function" in validateCMA(Ellipsoid(5))
  it should "optimize a 20D ellipsoid function" in validateCMA(Ellipsoid(20))
}
