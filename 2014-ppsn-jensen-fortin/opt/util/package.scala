package opt

import opt.util.optimized.{DoubleOps, IntOps}

/**
 * Useful implicits for NGP util package.
 *
 * @author Maxim Buzdalov
 */
package object util {
  implicit class ConversionToLeftHandSideArgument[T](val t: T) extends AnyVal {
    def |>[U](fun: T => U): U = fun(t)
    def ifZeroThen(other: => T)(implicit num: Numeric[T]) = if (num.zero == t) other else t
  }

  implicit class RangeWrapper(t: Range) {
    @inline
    final def foldLeft[B](zero: B)(fun: (B, B) => B)(body: Int => B): B = {
      var res = zero
      for (i <- t) {
        res = fun(res, body(i))
      }
      res
    }
    @inline
    private final def foldLeft2[B](zero: B)(fun: (B, B) => B)(body: Int => B): B = {
      var res = zero
      for (i <- t.tail) {
        res = fun(res, body(i))
      }
      res
    }

    final def mapSum[B](body: Int => B)(implicit num: Numeric[B]) = {
      if (num eq IntOps.Numeric) {
        IntOps.mapSum(t, body.asInstanceOf[Int => Int]).asInstanceOf[B]
      } else if (num eq DoubleOps.Numeric) {
        DoubleOps.mapSum(t, body.asInstanceOf[Int => Double]).asInstanceOf[B]
      } else {
        foldLeft(num.zero)(num.plus)(body)
      }
    }

    final def mapProduct[B](body: Int => B)(implicit num: Numeric[B]) = {
      if (num eq IntOps.Numeric) {
        IntOps.mapProduct(t, body.asInstanceOf[Int => Int]).asInstanceOf[B]
      } else if (num eq DoubleOps.Numeric) {
        DoubleOps.mapProduct(t, body.asInstanceOf[Int => Double]).asInstanceOf[B]
      } else {
        foldLeft(num.one)(num.times)(body)
      }
    }

    final def mapMin[B](body: Int => B)(implicit num: Ordering[B]) = {
      require(!t.isEmpty, "mapMin called on an empty range")
      if ((num eq IntOps.Ordering) || (num eq IntOps.Numeric)) {
        IntOps.mapMin(t, body.asInstanceOf[Int => Int]).asInstanceOf[B]
      } else if ((num eq DoubleOps.Ordering) || (num eq DoubleOps.Numeric)) {
        DoubleOps.mapMin(t, body.asInstanceOf[Int => Double]).asInstanceOf[B]
      } else {
        foldLeft2(body(t.start))(num.min)(body)
      }
    }

    final def mapMax[B](body: Int => B)(implicit num: Ordering[B]) = {
      require(!t.isEmpty, "mapMax called on an empty range")
      if ((num eq IntOps.Ordering) || (num eq IntOps.Numeric)) {
        IntOps.mapMax(t, body.asInstanceOf[Int => Int]).asInstanceOf[B]
      } else if ((num eq DoubleOps.Ordering) || (num eq DoubleOps.Numeric)) {
        DoubleOps.mapMax(t, body.asInstanceOf[Int => Double]).asInstanceOf[B]
      } else {
        foldLeft2(body(t.start))(num.max)(body)
      }
    }

    final def mapAnd(body: Int => Boolean) = {
      if (!t.isEmpty) {
        var i = t.start
        val total = t.numRangeElements
        val step = t.step
        var cnt = 0
        var res = true
        while (cnt < total && res) {
          res = body(i)
          i += step
          cnt += 1
        }
        res
      } else true
    }

    final def mapOr(body: Int => Boolean) = {
      if (!t.isEmpty) {
        var i = t.start
        val total = t.numRangeElements
        val step = t.step
        var cnt = 0
        var res = false
        while (cnt < total && !res) {
          res = body(i)
          i += step
          cnt += 1
        }
        res
      } else false
    }
  }
}
