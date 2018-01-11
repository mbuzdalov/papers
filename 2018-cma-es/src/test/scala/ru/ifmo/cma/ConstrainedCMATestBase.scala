package ru.ifmo.cma

import breeze.linalg.DenseVector
import ru.ifmo.cma.problems.Sphere

abstract class ConstrainedCMATestBase extends UnconstrainedCMATestBase {
  (cma.name + " on constrained 2D sphere functions") should "work with 0 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(-1.0, -1.0),
      DenseVector(2.0, 0.5)
    ))
  }
  it should "work with 1 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(-1.0, -1.0),
      DenseVector(2.0, 0.0)
    ))
  }
  it should "work with 2 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(-1.0, -1.0),
      DenseVector(0.0, 0.0)
    ))
  }

  (cma.name + " on constrained 5D sphere functions") should "work with 0 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(-4.0, -3.0, -2.0, -1.0, -1.0),
      DenseVector(5.0, 6.0, 7.0, 8.0, 9.0)
    ))
  }

  it should "work with 1 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(-4.0, -3.0, -2.0, -1.0, 0.0),
      DenseVector(5.0, 6.0, 7.0, 8.0, 9.0)
    ))
  }

  it should "work with 2 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(-4.0, -3.0, -2.0, 0.0, 0.0),
      DenseVector(5.0, 6.0, 7.0, 8.0, 9.0)
    ))
  }

  it should "work with 3 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(-4.0, -3.0, 0.0, 0.0, 0.0),
      DenseVector(5.0, 6.0, 7.0, 8.0, 9.0)
    ))
  }

  it should "work with 4 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(-4.0, 0.0, 0.0, 0.0, 0.0),
      DenseVector(5.0, 6.0, 7.0, 8.0, 9.0)
    ))
  }

  it should "work with 5 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(0.0, 0.0, 0.0, 0.0, 0.0),
      DenseVector(5.0, 6.0, 7.0, 8.0, 9.0)
    ))
  }

  (cma.name + " on constrained 20D sphere functions") should "work with 0 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(-4, -4, -4, -4,  -4, -4, -4, -4,  -4, -4, -4, -4,  -4, -4, -4, -4,  -4, -4, -4, -4),
      DenseVector( 2,  2,  2,  2,   2,  2,  2,  2,   2,  2,  2,  2,   2,  2,  2,  2,   2,  2,  2,  2)
    ))
  }

  it should "work with 4 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector( 0, -4, -4,  0,  -4, -4, -4, -4,  -4, -4, -4, -4,  -4, -4, -4, -4,  -4, -4, -4, -4),
      DenseVector( 2,  0,  0,  2,   2,  2,  2,  2,   2,  2,  2,  2,   2,  2,  2,  2,   2,  2,  2,  2)
    ))
  }

  it should "work with 8 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector( 0, -4, -4,  0,   0,  0, -4,  0,  -4, -4, -4, -4,  -4, -4, -4, -4,  -4, -4, -4, -4),
      DenseVector( 2,  0,  0,  2,   2,  2,  0,  2,   2,  2,  2,  2,   2,  2,  2,  2,   2,  2,  2,  2)
    ))
  }

  it should "work with 12 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector( 0, -4, -4,  0,   0,  0, -4,  0,  -4,  0, -4, -4,  -4, -4, -4, -4,  -4, -4, -4, -4),
      DenseVector( 2,  0,  0,  2,   2,  2,  0,  2,   0,  2,  0,  0,   2,  2,  2,  2,   2,  2,  2,  2)
    ))
  }

  it should "work with 16 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector( 0, -4, -4,  0,   0,  0, -4,  0,  -4,  0, -4, -4,   0, -4,  0, -4,  -4, -4, -4, -4),
      DenseVector( 2,  0,  0,  2,   2,  2,  0,  2,   0,  2,  0,  0,   2,  0,  2,  0,   2,  2,  2,  2)
    ))
  }

  it should "work with 20 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector( 0, -4, -4,  0,   0,  0, -4,  0,  -4,  0, -4, -4,   0, -4,  0, -4,  -4, -4, -4,  0),
      DenseVector( 2,  0,  0,  2,   2,  2,  0,  2,   0,  2,  0,  0,   2,  0,  2,  0,   0,  0,  0,  2)
    ))
  }
}
