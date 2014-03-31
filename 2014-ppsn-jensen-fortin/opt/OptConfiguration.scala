package opt

import opt.types.{DomainType, CodomainType}

/**
 * A root configuration for all convenience configurations.
 *
 * @author Maxim Buzdalov
 */
class OptConfiguration[D, C] {
  /**
   * Defines the domain type tag.
   */
  implicit val domain = DomainType[D]
  /**
   * Defines the codomain type tag.
   */
  implicit val codomain = CodomainType[C]
}
