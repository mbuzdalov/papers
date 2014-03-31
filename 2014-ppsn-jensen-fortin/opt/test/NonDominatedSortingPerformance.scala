package opt.test

import scala.annotation.tailrec

import opt.test.NonDominatedSorter._

/**
 * A performance & torture testing for non-dominated sorters.
 *
 * @author Maxim Buzdalov
 */
object NonDominatedSortingPerformance extends App {
  def test(test: IS[IS[Int]], name: String) {
    val dim = test(0).size
    val deb = createDeb(dim)
    val jen = createJensenFortin(dim)
    val buz = createJensenFortinBuzdalov(dim)

    println(s"Test: $name")
    val jenAnswer = sortNDSResults(jen.doSorting(test))
    val buzAnswer = sortNDSResults(buz.doSorting(test))
    val debAnswer = sortNDSResults(deb.doSorting(test))
    if (debAnswer != jenAnswer || debAnswer != buzAnswer) {
      println(s"  Failed!")
      println(s"    Test: ${test.toString().replace("Vector", "IS")}")
      println(s"    Deb: ${debAnswer.toString().replace("Vector", "IS")}")
      println(s"    Jen: ${jenAnswer.toString().replace("Vector", "IS")}")
      println(s"    Buz: ${buzAnswer.toString().replace("Vector", "IS")}")
    } else {
      for (i <- 0 until 5) {
        System.gc()
        System.gc()
        val s0 = System.currentTimeMillis()
        deb.doSorting(test)
        val t0 = System.currentTimeMillis() - s0
        System.gc()
        System.gc()
        val s1 = System.currentTimeMillis()
        jen.doSorting(test)
        val t1 = System.currentTimeMillis() - s1
        System.gc()
        System.gc()
        val s2 = System.currentTimeMillis()
        buz.doSorting(test)
        val t2 = System.currentTimeMillis() - s2
        println(s"    Deb: $t0 Jen: $t1 Buz: $t2")
      }
    }
  }

  def nDim(dims: IS[Int]) = {
    val answer = IS.newBuilder[IS[Int]]
    val tmp = Array.ofDim[Int](dims.size)
    @tailrec
    def next(i: Int): Boolean = {
      if (i == dims.size) false else {
        tmp(i) += 1
        if (tmp(i) == dims(i)) {
          tmp(i) = 0
          next(i + 1)
        } else true
      }
    }
    do {
      answer += tmp
    } while (next(0))
    answer.result()
  }

  val rng = new java.util.Random(2345346)
  def random(dims: IS[Int], cnt: Int) = {
    IS.fill(cnt)(IS.tabulate(dims.size)(i => rng.nextInt(dims(i))))
  }

  def bad(size: Int) = {
    IS.tabulate(size)(i => IS.tabulate(size)(j => if (j > i) 1 else 0))
  }

  for (t <- 0 until 10) {
    test(bad(200), "Bad 200")
    test(bad(400), "Bad 400")
    test(bad(600), "Bad 600")
    test(random(IS.fill(12)(3),   3000), "Random 3000 from 3^12")
    test(random(IS.fill(6)(10),   3000), "Random 3000 from 10^6")
    test(random(IS.fill(3)(1000), 3000), "Random 3000 from 1000^3")
    test(random(IS.fill(3)(10),   3000), "Random 3000 from 10^3")
    test(random(IS.fill(3)(3),    3000), "Random 3000 from 3^3")
  }
}
