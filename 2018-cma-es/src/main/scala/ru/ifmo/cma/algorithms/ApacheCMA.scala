package ru.ifmo.cma.algorithms

import scala.collection.JavaConverters._

import breeze.linalg.DenseVector

import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.CMAESOptimizer
import org.apache.commons.math3.optim.nonlinear.scalar.{GoalType, ObjectiveFunction}
import org.apache.commons.math3.optim.{InitialGuess, MaxEval, SimpleBounds}
import org.apache.commons.math3.random.MersenneTwister

import ru.ifmo.cma.{CMALike, Problem}

object ApacheCMA extends CMALike {
  override def name: String = "Apache CMA"

  override def minimize(
    problem: Problem,
    initial: DenseVector[Double],
    sigma: Double,
    iterations: Int,
    fitnessThreshold: Double
  ): (DenseVector[Double], Double, Seq[Double]) = {
    val realInitial = if (problem.canApply(initial)) initial else (problem.lowerBounds + problem.upperBounds) / 2.0
    val cma = new CMAESOptimizer(
      iterations,
      fitnessThreshold,
      false,
      0,
      10,
      new MersenneTwister(),
      true,
      (iteration, _, current) => iteration >= iterations || current.getValue <= fitnessThreshold
    )
    val populationSize = 4 + (3 * math.log(problem.dimension)).toInt
    val result = cma.optimize(
      new ObjectiveFunction(point => problem(DenseVector(point :_*))),
      GoalType.MINIMIZE,
      new MaxEval((iterations + 1) * populationSize),
      new SimpleBounds(problem.lowerBounds.toArray, problem.upperBounds.toArray),
      new InitialGuess(realInitial.toArray),
      new CMAESOptimizer.PopulationSize(populationSize),
      new CMAESOptimizer.Sigma(Array.fill(problem.dimension)(sigma))
    )
    val stat = cma.getStatisticsFitnessHistory.asScala.toIndexedSeq.map(_.toDouble)
    (DenseVector(result.getPoint :_*), result.getValue, stat)
  }
}
