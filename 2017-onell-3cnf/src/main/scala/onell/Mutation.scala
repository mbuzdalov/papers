package onell

import java.util.Random

import onell.util.ArrayIntSet

/**
  * A mutation operator which generates mutation indices
  * based on the problem size `n` and the mutation probability `p`.
  */
class Mutation(n: Int, initialP: Double, rng: Random) extends ArrayIntSet(n) {
  private[this] var p = initialP
  private[this] var log1p = math.log(1 - p)

  private[this] def offset() = if (p == 1) 1 else 1 + (math.log(rng.nextDouble()) / log1p).toInt

  def setProbability(newP: Double): Unit = {
    p = newP
    log1p = math.log(1 - p)
  }

  /**
    * Creates a mutation pattern by choosing random bits (with probability `p` given at the construction).
    * If `useSameIndexCount` is true, it chooses exactly this many bits as was chosen last time.
    * @param useSameIndexCount whether to choose the same number of bits as last time.
    */
  def createRandomBits(useSameIndexCount: Boolean): Unit = {
    if (!useSameIndexCount) {
      clear()
      var index = offset() - 1
      while (index < n) {
        this += index
        index += offset()
      }
    } else {
      val count = size
      clear()
      while (size < count) {
        this += rng.nextInt(n)
      }
    }
  }

  /**
    * Creates a mutation pattern by choosing random bits among the ones presented in the given array.
    * @param bitArray the array with bits.
    * @param bitCount the number of bits in the array to take.
    */
  def chooseRandomBits(bitArray: Array[Int], bitCount: Int): Unit = {
    clear()
    var index = offset() - 1
    while (index < bitCount) {
      this += bitArray(index)
      index += offset()
    }
  }

  /**
    * Applies the mutation to the given bits.
    * @param bits the bits to mutate.
    */
  def mutate(bits: Array[Boolean]): Unit = {
    for (i <- this) {
      bits(i) ^= true
    }
  }

  /**
    * Undoes the mutation to the given bits.
    * @param bits the bits to undo the mutation on.
    */
  def undo(bits: Array[Boolean]): Unit = mutate(bits)
}
