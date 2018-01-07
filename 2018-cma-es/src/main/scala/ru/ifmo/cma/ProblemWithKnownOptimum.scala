package ru.ifmo.cma

import breeze.linalg.DenseVector

trait ProblemWithKnownOptimum extends Problem {
  override final val dimension: Int = knownOptimumLocation.length
  override final val knownOptimum: Some[Double] = Some(apply(knownOptimumLocation))
  def knownOptimumLocation: DenseVector[Double]
}
