package ru.ifmo.ctd.ngp.opt

import ru.ifmo.ctd.ngp.util.FastRandom

/**
 * A source of randomness.
 * This is packed into a separate class to provide the class through implicits,
 * so no need to passing around a (=> Random) thing. This is helpful in multi-threaded
 * environment.
 *
 * @author Maxim Buzdalov
 */
abstract class RandomSource {
  def apply(): java.util.Random
}

object RandomSource {
  val fastRandom = new RandomSource {
    def apply() = FastRandom.threadLocal()
  }
}
