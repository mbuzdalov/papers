package ru.ifmo.cma.problems

import breeze.linalg.DenseVector
import ru.ifmo.cma.ProblemWithKnownOptimum

case class Sphere(knownOptimumLocation: DenseVector[Double]) extends ProblemWithKnownOptimum {
  override def apply(arg: DenseVector[Double]): Double = {
    val diff = arg - knownOptimumLocation
    diff dot diff
  }
}
