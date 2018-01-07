package ru.ifmo.cma

import breeze.linalg.DenseVector

trait Problem {
  def dimension: Int
  def apply(arg: DenseVector[Double]): Double
  def knownOptimum: Option[Double]
}
