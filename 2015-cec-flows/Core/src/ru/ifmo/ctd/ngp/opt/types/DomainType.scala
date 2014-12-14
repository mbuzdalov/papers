package ru.ifmo.ctd.ngp.opt.types

/**
 * A descriptor of the domain of the function to be optimized.
 *
 * @author Maxim Buzdalov
 */
final class DomainType[+D] private ()

/**
 * A companion object for the `DomainType`.
 */
object DomainType {
  private val instance = new DomainType[Nothing]
  /**
   * Returns the domain type tag for the given domain type.
   * @tparam D the domain type.
   * @return the domain type tag.
   */
  def apply[D]: DomainType[D] = instance
}
