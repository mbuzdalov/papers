package ru.ifmo.cma

import breeze.linalg.DenseVector

trait CMALike {
  def minimize(
    problem: Problem,
    initial: DenseVector[Double],
    sigma: Double,
    iterations: Int,
    fitnessThreshold: Double
  ): (DenseVector[Double], Double)
}
