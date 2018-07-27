package ru.ifmo.cma.algorithms

import breeze.linalg.DenseVector
import org.scalatest._
import ru.ifmo.cma.problems._
import ru.ifmo.cma.{CMALike, ProblemWithKnownOptimum}

abstract class UnconstrainedCMATestBase extends FlatSpec with Matchers {
  protected val eps: Double = 1e-12

  def cma: CMALike

  def validateCMA(problem: ProblemWithKnownOptimum): Unit = {
    val toleranceSq = math.sqrt(eps)
    val expectedValue = problem.knownOptimum
    val (point, value, _) = cma.minimize(problem, DenseVector.fill(problem.dimension)(5.55), 1, 5000, expectedValue + eps)
    val optimum = problem.knownOptimumLocation
    (value - expectedValue) should (be >= -eps and be <= eps)
    (point - optimum).foreach(_ should (be >= -toleranceSq and be <= toleranceSq))
  }

  (cma.name + " on unconstrained functions") should "optimize a 2D sphere function" in validateCMA(Sphere(2))
  it should "optimize a 3D sphere function" in validateCMA(Sphere(3))
  it should "optimize a 5D sphere function" in validateCMA(Sphere(5))
  it should "optimize a 20D sphere function" in validateCMA(Sphere(20))

  it should "optimize a 2D ellipsoid function" in validateCMA(Ellipsoid(2))
  it should "optimize a 3D ellipsoid function" in validateCMA(Ellipsoid(3))
  it should "optimize a 5D ellipsoid function" in validateCMA(Ellipsoid(5))
  it should "optimize a 20D ellipsoid function" in validateCMA(Ellipsoid(20))
}
