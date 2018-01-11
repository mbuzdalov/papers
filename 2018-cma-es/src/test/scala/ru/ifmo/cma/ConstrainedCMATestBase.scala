package ru.ifmo.cma

import breeze.linalg.DenseVector
import ru.ifmo.cma.problems.Sphere

abstract class ConstrainedCMATestBase extends UnconstrainedCMATestBase {
  (name + " on constrained 2D sphere functions") should "work with 0 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(2.0, 3.0),
      DenseVector(1.0, 2.0),
      DenseVector(4.0, 3.5)
    ))
  }
  it should "work with 1 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(2.0, 3.0),
      DenseVector(1.0, 2.0),
      DenseVector(4.0, 3.0)
    ))
  }
  it should "work with 2 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(2.0, 3.0),
      DenseVector(1.0, 2.0),
      DenseVector(2.0, 3.0)
    ))
  }

  (name + " on constrained 5D sphere functions") should "work with 0 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(5.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(1.0, 1.0, 1.0, 1.0, 0.0),
      DenseVector(10.0, 10.0, 10.0, 10.0, 10.0)
    ))
  }

  it should "work with 1 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(5.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(1.0, 1.0, 1.0, 1.0, 1.0),
      DenseVector(10.0, 10.0, 10.0, 10.0, 10.0)
    ))
  }

  it should "work with 2 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(5.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(1.0, 1.0, 1.0, 2.0, 1.0),
      DenseVector(10.0, 10.0, 10.0, 10.0, 10.0)
    ))
  }

  it should "work with 3 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(5.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(1.0, 1.0, 3.0, 2.0, 1.0),
      DenseVector(10.0, 10.0, 10.0, 10.0, 10.0)
    ))
  }

  it should "work with 4 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(5.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(1.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(10.0, 10.0, 10.0, 10.0, 10.0)
    ))
  }

  it should "work with 5 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(5.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(5.0, 4.0, 3.0, 2.0, 1.0),
      DenseVector(10.0, 10.0, 10.0, 10.0, 10.0)
    ))
  }

  (name + " on constrained 20D sphere functions") should "work with 0 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(5, 5, 5, 5,  5, 5, 5, 5,  5, 5, 5, 5,  5, 5, 5, 5,  5, 5, 5, 5),
      DenseVector(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
      DenseVector(7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7)
    ))
  }

  it should "work with 4 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(1, 7, 7, 1,  5, 5, 5, 5,  5, 5, 5, 5,  5, 5, 5, 5,  5, 5, 5, 5),
      DenseVector(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
      DenseVector(7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7)
    ))
  }

  it should "work with 8 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(1, 7, 7, 1,  7, 1, 7, 1,  5, 5, 5, 5,  5, 5, 5, 5,  5, 5, 5, 5),
      DenseVector(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
      DenseVector(7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7)
    ))
  }

  it should "work with 12 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(1, 7, 7, 1,  7, 1, 7, 1,  1, 1, 7, 1,  5, 5, 5, 5,  5, 5, 5, 5),
      DenseVector(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
      DenseVector(7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7)
    ))
  }

  it should "work with 16 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(1, 7, 7, 1,  7, 1, 7, 1,  1, 1, 7, 1,  7, 7, 7, 1,  5, 5, 5, 5),
      DenseVector(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
      DenseVector(7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7)
    ))
  }

  it should "work with 20 active constraint(s)" in {
    validateCMA(Sphere(
      DenseVector(1, 7, 7, 1,  7, 1, 7, 1,  1, 1, 7, 1,  7, 7, 7, 1,  1, 7, 7, 7),
      DenseVector(1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1),
      DenseVector(7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7,  7, 7, 7, 7)
    ))
  }
}
