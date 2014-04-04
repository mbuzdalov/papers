package proteins

/**
 * A trait that can convert torsion angles to matrix and vice versa.
 * @author Maxim Buzdalov
 */
trait MatrixEncoding { needs: MotionHelper =>
  def indexedSeq2torsion(seq: IndexedSeq[Double])(model: Int, atom: Int): SinCos
  def matrixSize: Int
  def torsion2indexedSeq(angleFeeder: (Int, Int) => SinCos): IndexedSeq[Double]
  def initializeMatrix(index: Int): Double
}
