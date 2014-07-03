package proteins.encodings

import proteins.{SinCos, MotionHelper, MatrixEncoding}

/**
 * An encoding that uses sine-cosine pairs for angles to encode the conformation.
 * 
 * @author Maxim Buzdalov
 */
trait SinCosEncoding extends MatrixEncoding { needs: MotionHelper =>
  def optimizedAngleThreshold = 0.03

  protected val torsionCount = src.torsion.size
  protected val angleDifferences = (0 until torsionCount) map { i =>
    val l = src.torsion(i)
    val r = trg.torsion(i)
    math.abs((l - r).angle)
  }
  protected val needOptimize = angleDifferences.map(_  > optimizedAngleThreshold)

  protected val optimizedAnglesForModel = needOptimize.count(identity)

  protected val hIndices = {
    val builder = IndexedSeq.newBuilder[Int]
    var used = 0
    for (i <- 0 until torsionCount) {
      if (needOptimize(i)) {
        builder += used
        used += 1
      } else {
        builder += -1
      }
    }
    builder.result()
  }
  protected def index(atom: Int, model: Int) = 2 * (hIndices(atom) + model * optimizedAnglesForModel)
  protected val functions = {
    for (model <- 0 until intermediate) yield {
      for (atom <- 0 until torsionCount) yield {
        if (needOptimize(atom)) {
          val i = index(atom, model)
          m: IndexedSeq[Double] => SinCos(m(i), m(i + 1))
        } else {
          val linear = linearApproximation(model, atom)
          _: IndexedSeq[Double] => linear
        }
      }
    }
  }

  val matrixSize = 2 * optimizedAnglesForModel * intermediate
  def indexedSeq2torsion(seq: IndexedSeq[Double])(model: Int, atom: Int) = functions(model)(atom)(seq)
  def torsion2indexedSeq(angleFeeder: (Int, Int) => SinCos) = {
    val bld = Array.ofDim[Double](matrixSize)
    for (model <- 0 until intermediate) {
      for (atom <- 0 until torsionCount) {
        if (needOptimize(atom)) {
          val angle = angleFeeder(model, atom)
          val i = index(atom, model)
          bld(i) = angle.sin
          bld(i + 1) = angle.cos
        }
      }
    }
    bld.toIndexedSeq
  }

  protected val matrixIndexToTorsionIndex = {
    val arr = Array.fill(matrixSize)(-1)
    for (atom <- 0 until torsionCount) {
      if (needOptimize(atom)) {
        for (model <- 0 until intermediate) {
          arr(index(atom, model)) = atom
          arr(index(atom, model) + 1) = atom
        }
      }
    }
    assert(arr.forall(_ != -1))
    arr.toIndexedSeq
  }

  override def initializeMatrix(index: Int) = {
    val t = angleDifferences(matrixIndexToTorsionIndex(index))
    math.max(0.001, t * t)
  }
}
