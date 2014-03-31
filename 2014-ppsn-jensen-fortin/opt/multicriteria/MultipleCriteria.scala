package opt.multicriteria

import opt.{CodomainComparator, Evaluated}
import opt.types.CodomainType

/**
 * A class that gives some basic information for multiple criteria optimization.
 *
 * @author Maxim Buzdalov
 */
abstract class MultipleCriteria[C: CodomainType] {
  /**
   * Returns the number of criteria.
   * @return the number of criteria.
   */
  def numberOfCriteria: Int
  /**
   * Returns the name of the given criterion.
   * @param criterion the criterion index.
   * @return the name of the criterion.
   */
  def nameOfCriterion(criterion: Int): String
  /**
   * Returns an ordering for the criterion of the given index.
   * The index of criterion must be from 0 to `numberOfCriteria - 1`
   * Please do not create the comparators from the scratch, this method will be often used.
   * @param criterion the index of criterion.
   * @return the ordering.
   */
  def orderingForCriterion(criterion: Int): Ordering[C]
  /**
   * Tests two codomain points for the domination relation.
   * @param lhs the left-hand side point.
   * @param rhs the right-hand side point.
   * @return an instance of `DominationComparisonResult` for the result.
   */
  def domination(lhs: C, rhs: C): DominationComparisonResult = {
    var hasLess = false
    var hasGreater = false
    var hasEqual = false
    for (c <- 0 until numberOfCriteria) {
      val res = orderingForCriterion(c).compare(lhs, rhs)
      hasLess |= res < 0
      hasGreater |= res > 0
      hasEqual |= res == 0
    }
    if (hasLess && hasGreater) {
      DominationComparisonResult.Incomparable
    } else if (!hasLess && !hasGreater) {
      DominationComparisonResult.Equal
    } else if (hasLess) {
      DominationComparisonResult.Less
    } else {
      DominationComparisonResult.Greater
    }
  }

  def domination(lhs: Evaluated[_, C], rhs: Evaluated[_, C]): DominationComparisonResult = domination(lhs.output, rhs.output)

  def projection(index: Int): CodomainComparator[C] = {
    require(index >= 0 && index < numberOfCriteria, "index is negative or too large")
    CodomainComparator().byOrdering(orderingForCriterion(index)).increasing
  }

  def projection(indices: Seq[Int]): MultipleCriteria[C] = {
    require(indices.forall(i => i >= 0 && i < numberOfCriteria),
      "indices contain negative or too large numbers")
    require(indices.distinct.size == indices.size,
      "indices contain duplicate numbers")
    val implNumberOfCriteria = indices.size
    val implNameOfCriterion = indices.map(nameOfCriterion)
    val implOrderingForCriterion = indices.map(orderingForCriterion)
    new MultipleCriteria[C] {
      def orderingForCriterion(criterion: Int) = implOrderingForCriterion(criterion)
      def nameOfCriterion(criterion: Int) = implNameOfCriterion(criterion)
      def numberOfCriteria = implNumberOfCriteria
    }
  }
}

object MultipleCriteria {
  trait CriterionToDouble[C] {
    def criterionToDouble(codomain: C, criterion: Int): Double
  }
  /**
   * Creates a `MultipleCriteria` object from the given codomain type,
   * conversion from the codomain type to `IndexedSeq[T]` and an ordering on T.
   *
   * Note that the optimizer will maximize each criterion with regards to the ordering on T.
   *
   * @param names the names of criteria.
   * @param codomain the codomain type tag.
   * @param c2indexedSeq the conversion from codomain to `IndexedSeq[T]`
   * @param ordering the ordering on elements.
   * @tparam C the codomain type.
   * @tparam T the element type.
   * @return the `MultipleCriteria` object.
   */
  def fromIndexedSeqWithElementOrdering[C, T](names: String*)(
    implicit codomain: CodomainType[C],
             c2indexedSeq: C => IndexedSeq[T],
             ordering: Ordering[T]
  ): MultipleCriteria[C] = new MultipleCriteria[C] {
    private val criteria = names.size
    private val myNames = names.toIndexedSeq
    private val orderings = (0 until criteria) map { i =>
      new Ordering[C] {
        def compare(x: C, y: C) = ordering.compare(x(i), y(i))
      }
    }
    def nameOfCriterion(criterion: Int) = myNames(criterion)
    def numberOfCriteria = criteria
    def orderingForCriterion(criterion: Int) = orderings(criterion)
  }

  /**
   * Creates a `MultipleCriteria` object from the given codomain type which have a conversion to IndexedSeq[T],
   * where T is of the `Numeric` type class.
   *
   * Note that the optimizer will maximizer each criterion.
   *
   * The returned object will also mix in the `CriterionToDouble` trait.
   *
   * @param names the names of criteria.
   * @tparam C the codomain type.
   * @return the `MultipleCriteria` with `CriterionToDouble` object.
   */
  def fromIndexedSeqOfNumeric[C, T](names: String*)(
    implicit codomain: CodomainType[C],
             c2indexedSeq: C => IndexedSeq[T],
             numeric: Numeric[T]
  ) = {
    new MultipleCriteria[C] with CriterionToDouble[C] {
      private val criteria = names.size
      private val myNames = names.toIndexedSeq
      private val orderings = (0 until criteria) map { i =>
        new Ordering[C] {
          def compare(x: C, y: C) = numeric.compare(x(i), y(i))
        }
      }
      def numberOfCriteria = criteria
      def nameOfCriterion(criterion: Int) = myNames(criterion)
      def orderingForCriterion(criterion: Int) = orderings(criterion)
      def criterionToDouble(codomain: C, criterion: Int) = numeric.toDouble(codomain(criterion))
    }
  }

  /**
   * Creates a `MultipleCriteria` object from the given codomain type using an extractor function
   * that extracts a `Numeric` value from the codomain value and the criterion number.
   *
   * Note that the optimizer will maximizer each criterion. Also please note that the extractor function
   * will be called very often, so try your best to make it faster.
   *
   * The returned object will also mix in the `CriterionToDouble` trait.
   *
   * @param extractor the extractor function.
   * @param names the names of criteria.
   * @tparam C the codomain type.
   * @return the `MultipleCriteria` with `CriterionToDouble` object.
   */
  def fromExtractorOfNumeric[C: CodomainType, T: Numeric](extractor: (C, Int) => T, names: String*) = {
    new MultipleCriteria[C] with CriterionToDouble[C] {
      private val num = implicitly[Numeric[T]]
      private val criteria = names.size
      private val myNames = names.toIndexedSeq
      private val orderings = (0 until criteria) map { i =>
        new Ordering[C] {
          def compare(x: C, y: C) = num.compare(extractor(x, i), extractor(y, i))
        }
      }
      def numberOfCriteria = criteria
      def nameOfCriterion(criterion: Int) = myNames(criterion)
      def orderingForCriterion(criterion: Int) = orderings(criterion)
      def criterionToDouble(codomain: C, criterion: Int) = num.toDouble(extractor(codomain, criterion))
    }
  }
}
