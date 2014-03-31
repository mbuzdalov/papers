import java.util.Locale

/**
 * THIS IS A PART OF SUPPLEMENTARY MATERIAL FOR THE PAPER:
 * -----------------------------------
 * Initial Results on Runtime Analysis
 *   of (1+1) Evolutionary Algorithm
 *     Controlled with Q-learning
 *  using Greedy Exploration Strategy
 *     on OneMax+ZeroMax Problem
 * -----------------------------------
 * This class evaluates the minimum difference 
 * between the expectations of number of fitness evaluations necessary to get to an optimum
 * from adjacent states in the Markov chain
 * for the OneMax+ZeroMax problem solved by (1+1)-EA controlled by greedy Q-learning.
 *
 * This is a simple Scala class that you may compile and run without any dependencies.
 *
 * @author Maxim Buzdalov
 */
object NonIncreasingAssumption extends App {
  val choose = {
    val maxN = 26
    val arr = Array.ofDim[Array[Int]](maxN + 1)
    arr(0) = Array(1)
    for (i <- 1 to maxN) {
        arr(i) = Array.tabulate(i + 1)(j => if (j == 0 || j == i) 1 else arr(i - 1)(j - 1) + arr(i - 1)(j))
    }
    arr
  }

  def solveES(n: Int) = {
    val dp = Array.ofDim[Double](1 << n, n)
    val prob = 1.0 / n
    for (mask <- (dp.length - 1) to 0 by -1) {
      for (point <- (n - 1) to 0 by -1) {
        val ones = point
        val zeros = n - ones
        if ((mask & (1 << point)) == 0) {
          var sumP = 0.0
          var sumE = 0.0
          for (t0 <- 0 to zeros; t1 <- 0 to ones) {
            val p = 0.5 * math.pow(prob, t0 + t1) * math.pow(1 - prob, n - t0 - t1) * choose(ones)(t1) * choose(zeros)(t0)
            if (t0 != t1) {
              sumP += p
              sumE += p * (if (point + t0 - t1 == n) 0 else dp(mask | (1 << point))(point + t0 - t1))
            }
          }
          dp(mask)(point) = (1 + sumE) / sumP
        } else {
          var sumP = 0.0
          var sumE = 0.0
          for (t1 <- 0 to ones; t0 <- t1 + 1 to zeros) {
            val p = math.pow(prob, t0 + t1) * math.pow(1 - prob, n - t0 - t1) * choose(ones)(t1) * choose(zeros)(t0)
            sumP += p
            sumE += p * (if (point + t0 - t1 == n) 0 else dp(mask)(point + t0 - t1))
          }
          dp(mask)(point) = (1 + sumE) / sumP
        }
      }
    }
    //Yet another assumption that also holds and is also not proven.
    for (mask <- 0 until dp.length) {
      for (point <- (n - 1) to 0 by -1) {
        if ((mask & (1 << point)) != 0) {
          val prevMask = mask ^ (1 << point)
          for (z <- 0 until n) {
            assert(dp(prevMask)(z) >= dp(mask)(z))
          }
        }
      }
    }
    var min = 1e6
    for (mask <- 0 until dp.length) {
      for (point <- (n - 2) to 0 by -1) {
        min = math.min(min, dp(mask)(point) - dp(mask)(point + 1))
      }
    }
    (dp(0)(0), min)
  }

  Locale.setDefault(Locale.US)
  for (t <- 3 to 22) {
    val (maxValue, minDiff) = solveES(t)
    println(f"$t: $maxValue%.2f, $minDiff%.2f")
  }
}
