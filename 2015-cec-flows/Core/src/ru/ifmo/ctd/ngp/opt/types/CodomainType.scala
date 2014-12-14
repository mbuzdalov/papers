package ru.ifmo.ctd.ngp.opt.types

/**
 * A descriptor of the codomain of the function to be optimized.
 *
 * @author Maxim Buzdalov
 */
final class CodomainType[+C] private ()

/**
 * A companion object for the `CodomainType`.
 */
object CodomainType {
  private val instance = new CodomainType[Nothing]
  /**
   * Returns the codomain type tag for the given codomain type.
   * @tparam C the codomain type.
   * @return the codomain type tag.
   */
  def apply[C]: CodomainType[C] = instance
}
