package ru.ifmo.cma

import breeze.linalg.{DenseVector, all}

trait Problem {
  final def canApply(arg: DenseVector[Double]): Boolean = {
    all(lowerBounds <:= arg) && all(upperBounds >:= arg)
  }
  final def apply(arg: DenseVector[Double]): Double = {
    require(canApply(arg))
    applyImpl(arg)
  }
  protected def applyImpl(arg: DenseVector[Double]): Double
  def name: String
  def dimension: Int
  def lowerBounds: DenseVector[Double]
  def upperBounds: DenseVector[Double]
}
