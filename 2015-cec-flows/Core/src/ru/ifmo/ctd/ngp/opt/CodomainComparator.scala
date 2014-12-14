package ru.ifmo.ctd.ngp.opt

import java.lang.{Double => JDouble, Float => JFloat}
import java.util.Comparator

import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.types.{DomainType, CodomainType}

/**
 * A base class for codomain comparators.
 * @author Maxim Buzdalov
 */
abstract class CodomainComparator[C: CodomainType] {
  /**
   * Returns 0 if `lhs` is equal to `rhs`, a negative value if `lhs` is less than `rhs`,
   * a positive value if `lhs` is greater than `rhs`.
   * @param lhs the left-hand side value of the codomain type.
   * @param rhs the right-hand side value of the codomain type.
   * @return the result of comparison.
   */
  def apply(lhs: C, rhs: C): Int

  /**
   * An ordering on the codomain which compares the elements the same way as the codomain comparator.
   */
  val codomainOrdering: Ordering[C] = new Ordering[C] {
    def compare(x: C, y: C) = apply(x, y)
  }

  /**
   * An ordering on the `Evaluated` objects which compares the elements by their codomain values (outputs)
   * the same way as the codomain comparator.
   */
  def evaluatedOrdering[D: DomainType] = new Ordering[Evaluated[D, C]] {
    def compare(x: Evaluated[D, C], y: Evaluated[D, C]) = apply(x.output, y.output)
  }
}

/**
 * A human-readable DSL for creating some default codomain comparators.
 * @author Maxim Buzdalov
 */
object CodomainComparator {
  class Detected[C: CodomainType] {
    type Type = CodomainComparator[C]
    /**
     * Uses the available (implicit) ordering on the codomain type.
     * @param ordering the ordering on the codomain type.
     * @return the `CodomainComparator.ID` object which allows to select either increasing or decreasing variant.
     */
    def byOrdering(implicit ordering: Ordering[C]) = new ID(ordering.compare)
    /**
     * Uses the specified comparator on the codomain type.
     * @param comparator the comparator on the codomain type.
     * @return the `CodomainComparator.ID` object which allows to select either increasing or decreasing variant.
     */
    def byComparator(comparator: Comparator[C]) = new ID(comparator.compare)
    /**
     * Uses the available (implicit) conversion to `Comparable` from the codomain type.
     * This also works if the codomain type is a `Comparable`.
     * @param c2comparable the conversion to `Comparable` from the codomain type.
     * @return the `CodomainComparator.ID` object which allows to select either increasing or decreasing variant.
     */
    def byComparable(implicit c2comparable: C => Comparable[_ >: C]) = new ID[C]((a, b) => c2comparable(a).compareTo(b))
    /**
     * Uses the available (implicit) conversion to `Double` from the codomain type.
     * This also works if the codomain type is `Double`.
     * @param c2double the conversion to `Double` from the codomain type.
     * @return the `CodomainComparator.ID` object which allows to select either increasing or decreasing variant.
     */
    def byDoubleValue(implicit c2double: C => Double) = new ID[C]((a, b) => JDouble.compare(c2double(a), c2double(b)))
    /**
     * Uses the available (implicit) conversion to `Float` from the codomain type.
     * This also works if the codomain type is `Float`.
     * @param c2float the conversion to `Float` from the codomain type.
     * @return the `CodomainComparator.ID` object which allows to select either increasing or decreasing variant.
     */
    def byFloatValue(implicit c2float: C => Float) = new ID[C]((a, b) => JFloat.compare(c2float(a), c2float(b)))
    /**
     * Uses the available (implicit) conversion to `Int` from the codomain type.
     * This also works if the codomain type is `Int`.
     * @param c2int the conversion to `Int` from the codomain type.
     * @return the `CodomainComparator.ID` object which allows to select either increasing or decreasing variant.
     */
    def byIntValue(implicit c2int: C => Int) = new ID[C]((a, b) => Integer.compare(c2int(a), c2int(b)))
    /**
     * Uses a function to convert the codomain values to something which has an `Ordering`.
     * Usually it is useful to compare objects by their fields.
     * @param fun the conversion function.
     * @tparam T the type of the codomain of the conversion function.
     * @return the `CodomainComparator.ID` object which allows to select either increasing or decreasing variant.
     */
    def by[T : Ordering](fun: C => T) = new ID[C]((a, b) => implicitly[Ordering[T]].compare(fun(a), fun(b)))
  }

  /**
   * An object which knows the comparing function and allows to construct either increasing or decreasing comparator.
   * @param cmp the comparing function.
   * @tparam C the codomain type tag.
   */
  class ID[C: CodomainType](cmp: (C, C) => Int) {
    /**
     * Returns an increasing codomain comparator.
     * @return the increasing codomain comparator.
     */
    def increasing = new CodomainComparator[C]() {
      def apply(lhs: C, rhs: C) = cmp(lhs, rhs)
    }
    /**
     * Returns a decreasing codomain comparator.
     * @return the decreasing codomain comparator.
     */
    def decreasing = new CodomainComparator[C]() {
      def apply(lhs: C, rhs: C) = cmp(rhs, lhs)
    }
  }

  /**
   * Defines the codomain type and allows to select options further.
   * @tparam C the codomain type.
   * @return the object with functions to select more options.
   */
  def apply[C: CodomainType]() = new Detected
}
