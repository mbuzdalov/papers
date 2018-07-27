package ru.ifmo.cma.experiments

import breeze.linalg.DenseVector
import ru.ifmo.cma.ProblemWithKnownOptimum
import ru.ifmo.cma.algorithms._
import ru.ifmo.cma.problems.{Ellipsoid, Exponential, Sphere, TwoAxes}

object SakamotoAkimotoPaper {
  private[this] val N = 20
  private[this] val commonLowerBound = DenseVector.tabulate(N)(i => if (i % 2 == 0) -0.1 else 0.1)
  private[this] val commonUpperBound = commonLowerBound + 5.0
  private[this] val commonOptimumLocation = DenseVector.tabulate(N)(i => if (i % 2 == 0) 0.0 else 0.1)
  private[this] val commonInitial = (commonLowerBound + commonUpperBound) / 2.0

  private[this] trait MovedOptimum extends ProblemWithKnownOptimum {
    override def knownOptimumLocation: DenseVector[Double] = commonOptimumLocation
  }

  private[this] val problems = Seq(
    Sphere(20),
    new Sphere(commonLowerBound, commonUpperBound) with MovedOptimum,
    new Ellipsoid(commonLowerBound, commonUpperBound) with MovedOptimum,
    new TwoAxes(commonLowerBound, commonUpperBound) with MovedOptimum,
    new Exponential(commonLowerBound, commonUpperBound) with MovedOptimum
  )

  private[this] val algorithms = Seq(
    AtanExpCMA, ApacheCMA, CMAWithResampling, CMAWithMirrors
  )

  def main(args: Array[String]): Unit = {
    for (problem <- problems) {
      println(s"${problem.name}: best fitness is ${problem.knownOptimum}")
      for (algorithm <- algorithms) {
        println(s"  ${algorithm.name}")
        val (x, y, fh) = algorithm.minimize(problem, commonInitial, 1.25, 50000, problem.knownOptimum + 1e-8)
        println(s"    fitness = $y, iterations: ${fh.size}")
      }
    }
  }
}
