package proteins

/**
 * The result of the alignment.
 *
 * @author Maxim Buzdalov
 */
trait AlignResult {
  def weighedRMSD: Double
  def firstChain: IndexedSeq[Point3]
  def secondChain: IndexedSeq[Point3]
}
