package opt.util.optimized

/**
 * Optimized operations for Int.
 *
 * @author Maxim Buzdalov
 */
object IntOps {
  final val Numeric  = implicitly[Numeric[Int]]
  final val Ordering = implicitly[Ordering[Int]]

  @inline
  final def mapSum(range: Range, fun: Int => Int) = {
    var sum = 0
    for (i <- range) {
      sum += fun(i)
    }
    sum
  }

  @inline
  final def mapProduct(range: Range, fun: Int => Int) = {
    var prod = 1
    for (i <- range) {
      prod *= fun(i)
    }
    prod
  }

  @inline
  final def mapMin(range: Range, fun: Int => Int) = {
    require(!range.isEmpty, "mapMin called on an empty range")
    var min = 0
    var first = true
    for (i <- range) {
      min = if (first) fun(i) else math.min(min, fun(i))
      first = false
    }
    min
  }

  @inline
  final def mapMax(range: Range, fun: Int => Int) = {
    require(!range.isEmpty, "mapMax called on an empty range")
    var max = 0
    var first = true
    for (i <- range) {
      max = if (first) fun(i) else math.max(max, fun(i))
      first = false
    }
    max
  }
}
