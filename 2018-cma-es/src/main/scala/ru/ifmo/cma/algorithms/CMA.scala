package ru.ifmo.cma.algorithms

import breeze.linalg.{*, DenseMatrix, DenseVector, eigSym, max, min, norm, sum}
import breeze.numerics.{log, sqrt}
import breeze.stats.distributions.Rand
import ru.ifmo.cma.{CMALike, Problem}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

class CMA protected (protected val problem: Problem) {
  protected type Vector = DenseVector[Double]
  protected type Matrix = DenseMatrix[Double]

  protected val Vector: DenseVector.type = DenseVector
  protected val Matrix: DenseMatrix.type = DenseMatrix

  private[this] val N = problem.dimension
  private[this] val popSize = 4 + (3 * math.log(N)).toInt
  private[this] val mu = popSize / 2

  private[this] val weights0 = math.log(mu + 0.5) - log(Vector.tabulate(mu)(_ + 1.0))
  private[this] val effectiveMu = {
    val sumW = sum(weights0)
    val sumW2 = weights0 dot weights0
    sumW * sumW / sumW2
  }
  private[this] val weights = weights0 / sum(weights0)
  private[this] val chiN = math.sqrt(N) * (1 - 1 / (4.0 * N) + 1 / (21.0 * N * N))
  private[this] val cs = (effectiveMu + 2.0) / (N + effectiveMu + 3.0)
  private[this] val cc = (4.0 + effectiveMu / N) / (N + 4 + 2.0 * effectiveMu / N)
  private[this] val damps = 1 + 2 * math.max(0, math.sqrt((effectiveMu - 1.0) / (N + 1.0)) - 1) + cs
  private[this] val ccov1 = 2 / ((N + 1.3) * (N + 1.3) + effectiveMu)
  private[this] val ccovmu = 2 * (effectiveMu - 2 + 1.0 / effectiveMu) / ((N + 2.0) * (N + 2.0) + effectiveMu)

  private[this] val psQuot = math.sqrt(cs * (2 - cs) * effectiveMu)
  private[this] val pcQuot = math.sqrt(cc * (2 - cc) * effectiveMu)

  private[this] val fitnessTracker = IndexedSeq.newBuilder[Double]
  private[this] val sigmaTracker = IndexedSeq.newBuilder[Double]
  private[this] var eigensAlreadyFailed = false

  protected def sampleXYZ(meanVector: Vector, bd: Matrix, sigma: Double): (Vector, Double, Vector) = {
    val z = Vector.rand(N, Rand.gaussian)
    val x = meanVector + sigma * (bd * z)
    val y = problem(x)
    (x, y, z)
  }

  protected def decomposeAndHandleErrors(matrix: Matrix): (Matrix, eigSym.EigSym[Vector, Matrix]) = {
    val symmetric = (matrix + matrix.t) / 2.0
    val eig = eigSym(symmetric)
    val minEigenvalue = min(eig.eigenvalues)
    val maxEigenvalue = max(eig.eigenvalues)
    if (minEigenvalue <= 0 || maxEigenvalue / minEigenvalue > 1e14) {
      if (!eigensAlreadyFailed) {
        eigensAlreadyFailed = true
        System.err.println(s"[WARNING]: eigenvalues are: [$minEigenvalue ... $maxEigenvalue]")
      }
      val addend = maxEigenvalue / 1e14 - math.max(0, minEigenvalue)
      val newMatrix = symmetric + addend * Matrix.eye[Double](N)
      (newMatrix, eigSym(newMatrix))
    } else {
      (symmetric, eig)
    }
  }

  @tailrec
  private[this] def iterate(
    countIterations: Int,
    maxIterations: Int,
    bestArgument: Vector,
    bestValue: Double,
    meanVector: Vector,
    matrix: Matrix,
    sigma: Double,
    pc: Vector,
    ps: Vector,
    fitnessThreshold: Double
  ): (Vector, Double) = {
    import CMA.matrix2diag
    if (countIterations == maxIterations || bestValue <= fitnessThreshold) {
      (bestArgument, bestValue)
    } else {
      Try {
        decomposeAndHandleErrors(matrix)
      } match {
        case Success((actualMatrix, eigSym.EigSym(eigValues, eigVectors))) =>
          val bd = eigVectors *\ sqrt(eigValues)
          val (x, y, z) = IndexedSeq.fill(popSize)(sampleXYZ(meanVector, bd, sigma)).sortBy(_._2).take(mu).unzip3
          val (newBestArgument, newBestValue) = if (y.head < bestValue) (x.head, y.head) else (bestArgument, bestValue)
          val xMatrix = Matrix(x :_*).t
          val xMean = xMatrix * weights
          val zMean = Matrix(z :_*).t * weights
          val newPS = (1 - cs) * ps + psQuot * (eigVectors * zMean)
          val normPS = norm(newPS)
          val hSigB = normPS / math.sqrt(1 - math.pow(1 - cs, 2 * (countIterations + 1))) / chiN < 1.4 + 2 / (N + 1.0)
          val hSig = if (hSigB) 1.0 else 0.0
          val newPC = (1 - cc) * pc + (pcQuot * hSig / sigma) * (xMean - meanVector)
          val centeredIndividuals = (xMatrix(::, *) - meanVector) / sigma
          val rankOne = (ccov1 * newPC) * newPC.t
          val rankMu = (centeredIndividuals *\ (ccovmu * weights)) * centeredIndividuals.t
          val newMatrix = (1 - ccov1 - ccovmu + (1 - hSig) * ccov1 * cc * (2 - cc)) * actualMatrix + rankOne + rankMu
          val newSigma = sigma * math.exp(math.min(1.0, (normPS / chiN - 1) * cs / damps))

          fitnessTracker += y.head
          sigmaTracker += newSigma

          iterate(countIterations + 1, maxIterations, newBestArgument, newBestValue,
            xMean, newMatrix, newSigma, newPC, newPS, fitnessThreshold)
        case Failure(th) =>
          System.err.println(s"[ERROR]: ${th.getClass.getName}: ${th.getMessage}")
          (bestArgument, bestValue)
      }
    }
  }

  def fitnessHistory: IndexedSeq[Double] = fitnessTracker.result()
  def sigmaHistory: IndexedSeq[Double] = sigmaTracker.result()

  def minimize(
    initial: DenseVector[Double],
    sigma: Double,
    iterations: Int,
    fitnessThreshold: Double
  ): (DenseVector[Double], Double) = {
    val realInitial = if (problem.canApply(initial)) initial else (problem.lowerBounds + problem.upperBounds) / 2.0
    val initialFitness = problem(realInitial)

    fitnessTracker.clear()
    sigmaTracker.clear()
    eigensAlreadyFailed = false

    fitnessTracker += initialFitness
    sigmaTracker += sigma

    iterate(
      countIterations = 0,
      maxIterations = iterations,
      bestArgument = realInitial,
      bestValue = initialFitness,
      meanVector = realInitial,
      matrix = DenseMatrix.eye(problem.dimension),
      sigma = sigma,
      pc = DenseVector.zeros(N),
      ps = DenseVector.zeros(N),
      fitnessThreshold = fitnessThreshold
    )
  }
}

object CMA extends CMALike {
  private[CMA] implicit class matrix2diag(val m: DenseMatrix[Double]) extends AnyVal {
    // same result as m * diag(d) but faster
    def *\ (d: DenseVector[Double]): DenseMatrix[Double] = m(*, ::) * d
  }

  override def name: String = "CMA"

  override def minimize(
    problem: Problem,
    initial: DenseVector[Double],
    sigma: Double,
    iterations: Int,
    fitnessThreshold: Double
  ): (DenseVector[Double], Double, Seq[Double]) = {
    val cma = new CMA(problem)
    val (point, value) = cma.minimize(initial, sigma, iterations, fitnessThreshold)
    (point, value, cma.fitnessHistory)
  }
}
