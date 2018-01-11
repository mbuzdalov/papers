package ru.ifmo.cma

import breeze.linalg.DenseVector

trait ProblemWithKnownOptimum extends Problem {
  override final val dimension: Int = knownOptimumLocation.length
  val knownOptimum: Double = apply(knownOptimumLocation)
  def knownOptimumLocation: DenseVector[Double]
}
