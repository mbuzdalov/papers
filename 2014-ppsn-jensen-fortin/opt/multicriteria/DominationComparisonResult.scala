package opt.multicriteria

/**
 * A trait for possible domination comparison result.
 *
 * @author Maxim Buzdalov
 */
sealed trait DominationComparisonResult {}

object DominationComparisonResult {
  case object Greater extends DominationComparisonResult
  case object Less extends DominationComparisonResult
  case object Equal extends DominationComparisonResult
  case object Incomparable extends DominationComparisonResult
}
