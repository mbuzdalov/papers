package opt

/**
 * A pair of an input (a value the function domain) and the output computed for it.
 *
 * @author Maxim Buzdalov
 */
case class Evaluated[+Domain, +Codomain](input: Domain, output: Codomain)
