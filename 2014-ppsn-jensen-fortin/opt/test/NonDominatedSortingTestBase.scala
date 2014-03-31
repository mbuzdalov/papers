package opt.test

import opt.test.NonDominatedSorter._

/**
 * Tests the non-dominated sorting algorithms.
 *
 * @author Maxim Buzdalov
 */
abstract class NonDominatedSortingTestBase {
  def getSorter(criteria: Int): NonDominatedSorter

  final def runTests() {
    testOnePoint()
    testOnePointManyCriteria()
    testOneCriterionManyPoints()
    testTwoDominatingPoints1()
    testTwoDominatingPoints2()
    testTwoDominatingPoints3()
    testTwoDominatingPoints4()
    testTwoNonDominatingPoints()
    testManyEqualPoints()
    testTwoGroupsOfManyEqualPoints()
    testSquare()
    testSquareDup()
    testCube()
    testCubeDup()
    testHypercube()
    testHypercubeDup()
    trickyRandomTest()
    trickyRandomTest2()
  }    

  def assertEquals(a: Any, b: Any) {
    if (a != b) throw new AssertionError(a + " != " + b)
  }

  def testOnePoint() {
    val srt = getSorter(1)
    val seq = IS(IS(0))
    assertEquals(IS(seq), srt.doSorting(seq))
  }

  def testOnePointManyCriteria() {
    val srt = getSorter(100)
    val seq = IS(IS.fill(100)(239))
    assertEquals(IS(seq), srt.doSorting(seq))
  }

  def testOneCriterionManyPoints() {
    val srt = getSorter(1)
    val seq = IS.tabulate(100)(v => IS(v))
    assertEquals(seq.map(e => IS(e)).reverse, srt.doSorting(seq))
  }

  def testTwoDominatingPoints1() {
    val srt = getSorter(2)
    val seq = IS(IS(1, 0), IS(2, 0))
    assertEquals(seq.map(e => IS(e)).reverse, srt.doSorting(seq))
  }

  def testTwoDominatingPoints2() {
    val srt = getSorter(2)
    val seq = IS(IS(1, 0), IS(2, 1))
    assertEquals(seq.map(e => IS(e)).reverse, srt.doSorting(seq))
  }

  def testTwoDominatingPoints3() {
    val srt = getSorter(2)
    val seq = IS(IS(2, 0), IS(1, 0))
    assertEquals(seq.map(e => IS(e)), srt.doSorting(seq))
  }

  def testTwoDominatingPoints4() {
    val srt = getSorter(2)
    val seq = IS(IS(2, 1), IS(1, 0))
    assertEquals(seq.map(e => IS(e)), srt.doSorting(seq))
  }

  def testTwoNonDominatingPoints() {
    val srt = getSorter(2)
    val seq = IS(IS(2, 1), IS(1, 2))
    assertEquals(IS(seq), srt.doSorting(seq))
  }

  def testManyEqualPoints() {
    val srt = getSorter(10)
    val seq = IS.fill(10, 10)(11111)
    assertEquals(IS(seq), srt.doSorting(seq))
  }

  def testTwoGroupsOfManyEqualPoints() {
    val srt = getSorter(10)
    val seq1 = IS.fill(10, 10)(11110)
    val seq2 = IS.fill(10, 10)(11111)
    assertEquals(IS(seq2, seq1), srt.doSorting(seq1 ++ seq2))
  }

  def genSimplex(dim: Int, size: Int): IndexedSeq[IndexedSeq[Int]] = {
    if (dim == 1) {
      IS.tabulate(size)(v => IS(v))
    } else {
      genSimplex(dim - 1, size).flatMap(seq => (0 until size) map (e => seq :+ e))
    }
  }

  def testSquare() {
    val srt = getSorter(2)
    val seq = genSimplex(2, 10)
    val expected = seq.groupBy(_.sum).toIndexedSeq.sortBy(-_._1).map(_._2)
    assertEquals(sortNDSResults(expected), sortNDSResults(srt.doSorting(seq)))
  }

  def testSquareDup() {
    val srt = getSorter(2)
    val seq = { val a = genSimplex(2, 10); a ++ a }
    val expected = seq.groupBy(_.sum).toIndexedSeq.sortBy(-_._1).map(_._2)
    assertEquals(sortNDSResults(expected), sortNDSResults(srt.doSorting(seq)))
  }

  def testCube() {
    val srt = getSorter(3)
    val seq = genSimplex(3, 8)
    val expected = seq.groupBy(_.sum).toIndexedSeq.sortBy(-_._1).map(_._2)
    assertEquals(sortNDSResults(expected), sortNDSResults(srt.doSorting(seq)))
  }

  def testCubeDup() {
    val srt = getSorter(3)
    val seq = { val a = genSimplex(3, 8); a ++ a }
    val expected = seq.groupBy(_.sum).toIndexedSeq.sortBy(-_._1).map(_._2)
    assertEquals(sortNDSResults(expected), sortNDSResults(srt.doSorting(seq)))
  }

  def testHypercube() {
    val srt = getSorter(4)
    val seq = genSimplex(4, 5)
    val expected = seq.groupBy(_.sum).toIndexedSeq.sortBy(-_._1).map(_._2)
    assertEquals(sortNDSResults(expected), sortNDSResults(srt.doSorting(seq)))
  }

  def testHypercubeDup() {
    val srt = getSorter(4)
    val seq = { val a = genSimplex(4, 5); a ++ a }
    val expected = seq.groupBy(_.sum).toIndexedSeq.sortBy(-_._1).map(_._2)
    assertEquals(sortNDSResults(expected), sortNDSResults(srt.doSorting(seq)))
  }

  def trickyRandomTest() {
    val input = IS(
      IS(758, 515, 226), IS(786, 98, 268), IS(876, 264, 655), IS(43, 572, 418), IS(158, 517, 647),
      IS(636, 321, 369), IS(19, 547, 935), IS(571, 866, 524), IS(819, 917, 692), IS(555, 487, 980)
    )
    val expected = IS(
      IS(IS(876, 264, 655), IS(19, 547, 935), IS(819, 917, 692), IS(555, 487, 980)),
      IS(IS(758, 515, 226), IS(786, 98, 268), IS(158, 517, 647), IS(636, 321, 369), IS(571, 866, 524)),
      IS(IS(43, 572, 418))
    )
    val srt = getSorter(3)
    assertEquals(sortNDSResults(expected), sortNDSResults(srt.doSorting(input)))
  }

  def trickyRandomTest2() {
    val input = IS(
      IS(8, 5, 6, 6, 8, 8), IS(6, 4, 5, 3, 2, 8), IS(8, 7, 7, 6, 1, 9), IS(9, 7, 5, 1, 6, 4), IS(9, 7, 2, 5, 7, 0),
      IS(8, 6, 0, 5, 9, 8), IS(2, 3, 2, 8, 2, 5), IS(3, 9, 0, 6, 2, 8), IS(2, 9, 2, 1, 6, 9), IS(5, 2, 3, 5, 9, 6),
      IS(0, 7, 2, 9, 1, 1), IS(4, 4, 6, 6, 3, 8), IS(9, 2, 5, 8, 0, 7), IS(8, 7, 9, 6, 0, 3), IS(7, 1, 7, 3, 0, 6),
      IS(8, 4, 9, 2, 5, 0), IS(5, 1, 5, 1, 7, 0), IS(2, 2, 4, 2, 6, 7), IS(7, 3, 6, 3, 2, 0), IS(1, 6, 6, 2, 9, 5),
      IS(5, 8, 4, 8, 8, 4), IS(0, 2, 5, 9, 0, 5), IS(3, 4, 6, 4, 2, 0), IS(8, 7, 2, 2, 9, 8), IS(5, 0, 1, 5, 6, 1),
      IS(4, 2, 4, 2, 5, 3), IS(8, 7, 8, 9, 3, 1), IS(1, 1, 6, 1, 5, 0), IS(1, 4, 2, 1, 7, 6), IS(7, 8, 8, 9, 3, 9),
      IS(0, 4, 1, 0, 3, 3), IS(8, 4, 4, 7, 6, 5), IS(1, 5, 0, 4, 5, 2), IS(8, 0, 7, 4, 6, 4), IS(9, 4, 0, 5, 6, 7),
      IS(9, 2, 5, 4, 1, 9), IS(9, 6, 3, 7, 6, 2), IS(2, 6, 8, 6, 2, 6), IS(4, 7, 5, 9, 2, 9), IS(5, 3, 7, 7, 1, 5),
      IS(7, 2, 8, 9, 3, 1), IS(9, 4, 1, 9, 2, 3), IS(4, 9, 5, 8, 2, 0), IS(3, 0, 8, 5, 7, 3), IS(4, 5, 6, 6, 5, 4),
      IS(3, 4, 9, 8, 8, 3), IS(0, 4, 8, 9, 9, 4), IS(1, 7, 5, 6, 2, 4), IS(6, 5, 4, 2, 0, 8), IS(0, 8, 5, 0, 9, 4),
      IS(3, 6, 0, 9, 3, 9), IS(2, 7, 5, 6, 3, 8), IS(1, 5, 5, 5, 6, 0), IS(6, 2, 8, 8, 6, 1), IS(0, 5, 3, 6, 5, 9),
      IS(6, 5, 0, 9, 8, 4), IS(5, 2, 0, 4, 0, 1), IS(7, 7, 7, 1, 9, 0), IS(1, 7, 5, 2, 2, 7), IS(3, 5, 5, 9, 9, 5),
      IS(0, 6, 9, 7, 9, 3), IS(8, 3, 7, 3, 8, 4), IS(0, 1, 6, 6, 0, 6), IS(9, 6, 7, 2, 6, 3), IS(1, 6, 0, 4, 9, 0),
      IS(3, 4, 0, 7, 6, 4), IS(4, 3, 1, 7, 9, 4), IS(2, 2, 8, 4, 5, 6), IS(7, 0, 1, 4, 7, 2), IS(5, 6, 1, 2, 8, 9),
      IS(7, 8, 2, 9, 5, 4), IS(5, 6, 4, 7, 4, 5), IS(5, 8, 3, 3, 4, 8), IS(8, 3, 2, 6, 0, 0), IS(1, 5, 3, 3, 1, 3),
      IS(7, 4, 8, 3, 9, 2), IS(3, 4, 0, 2, 3, 7), IS(9, 7, 9, 1, 9, 8), IS(9, 2, 0, 7, 1, 2), IS(7, 4, 5, 8, 7, 7),
      IS(8, 9, 9, 4, 2, 9), IS(5, 1, 3, 7, 2, 8), IS(5, 3, 7, 4, 6, 4), IS(5, 4, 3, 9, 9, 0), IS(7, 2, 8, 3, 4, 2),
      IS(8, 4, 4, 9, 4, 5), IS(9, 5, 1, 2, 9, 9), IS(6, 9, 2, 8, 8, 2), IS(2, 7, 3, 3, 6, 6), IS(2, 2, 5, 7, 9, 1),
      IS(7, 2, 3, 0, 1, 7), IS(2, 4, 4, 2, 2, 9), IS(5, 5, 0, 8, 4, 9), IS(8, 5, 1, 2, 0, 0), IS(9, 7, 9, 2, 1, 5),
      IS(4, 1, 8, 0, 2, 5), IS(3, 7, 3, 8, 0, 7), IS(7, 4, 2, 1, 7, 8), IS(7, 9, 2, 0, 6, 6), IS(2, 6, 3, 3, 5, 4),
      IS(9, 6, 6, 7, 4, 2), IS(0, 2, 7, 2, 7, 3), IS(9, 6, 1, 0, 5, 0), IS(4, 7, 5, 2, 8, 9), IS(3, 7, 8, 8, 9, 7),
      IS(0, 2, 9, 7, 9, 8), IS(8, 6, 9, 3, 4, 6), IS(9, 0, 3, 6, 3, 1), IS(8, 8, 1, 2, 0, 5), IS(6, 7, 8, 7, 2, 3),
      IS(7, 9, 1, 8, 2, 2), IS(2, 5, 6, 1, 8, 6), IS(8, 7, 4, 4, 7, 0), IS(0, 5, 9, 1, 9, 3), IS(4, 7, 7, 9, 0, 8),
      IS(8, 4, 1, 0, 5, 6), IS(9, 1, 7, 6, 2, 3), IS(8, 5, 1, 9, 0, 6), IS(6, 9, 6, 7, 9, 7), IS(8, 6, 6, 0, 4, 6),
      IS(4, 4, 6, 2, 7, 9), IS(3, 6, 4, 6, 1, 2), IS(3, 0, 2, 6, 8, 1), IS(6, 3, 3, 7, 2, 9), IS(0, 0, 8, 3, 6, 9),
      IS(8, 3, 4, 4, 9, 7), IS(7, 5, 4, 5, 1, 2), IS(4, 8, 8, 7, 8, 0), IS(3, 7, 8, 8, 8, 0), IS(8, 7, 8, 7, 5, 1),
      IS(5, 3, 4, 8, 4, 8), IS(7, 4, 1, 6, 7, 1), IS(7, 5, 4, 7, 7, 4), IS(1, 3, 4, 9, 1, 1), IS(1, 0, 4, 7, 4, 0),
      IS(7, 4, 8, 8, 5, 2), IS(1, 0, 3, 0, 4, 4), IS(3, 0, 6, 1, 5, 6), IS(5, 1, 2, 0, 6, 0), IS(3, 1, 2, 4, 1, 3),
      IS(7, 8, 0, 8, 5, 7), IS(4, 5, 4, 7, 8, 7), IS(4, 9, 2, 1, 0, 7), IS(5, 3, 6, 4, 2, 8), IS(0, 6, 7, 9, 3, 8),
      IS(1, 7, 1, 1, 2, 4), IS(6, 1, 3, 3, 9, 8), IS(6, 6, 8, 4, 5, 7), IS(0, 5, 0, 9, 5, 6), IS(3, 0, 4, 2, 1, 2),
      IS(6, 4, 6, 5, 8, 3), IS(7, 5, 6, 7, 3, 6), IS(2, 7, 2, 4, 5, 9), IS(4, 6, 9, 9, 2, 0), IS(9, 2, 1, 1, 4, 4),
      IS(1, 1, 6, 1, 9, 4), IS(5, 1, 7, 3, 4, 9), IS(8, 9, 2, 1, 7, 0), IS(9, 2, 8, 3, 7, 8), IS(7, 4, 6, 9, 1, 3),
      IS(7, 2, 5, 2, 3, 6), IS(9, 0, 4, 1, 0, 4), IS(4, 3, 3, 2, 5, 9), IS(1, 5, 1, 6, 7, 2), IS(4, 6, 1, 3, 9, 9),
      IS(4, 9, 8, 4, 7, 0), IS(6, 3, 5, 5, 8, 9), IS(6, 2, 1, 7, 8, 1), IS(1, 0, 8, 4, 5, 6), IS(8, 7, 3, 1, 8, 1),
      IS(6, 1, 7, 5, 1, 6), IS(1, 4, 6, 3, 6, 1), IS(7, 3, 3, 8, 4, 0), IS(4, 0, 9, 4, 8, 0), IS(3, 5, 7, 8, 5, 0),
      IS(6, 1, 3, 5, 5, 1), IS(0, 9, 5, 9, 1, 9), IS(1, 6, 1, 2, 7, 7), IS(5, 2, 5, 7, 0, 3), IS(5, 5, 5, 7, 5, 2),
      IS(6, 9, 8, 7, 1, 0), IS(4, 8, 6, 7, 7, 8), IS(9, 0, 9, 9, 7, 4), IS(8, 9, 6, 1, 0, 3), IS(4, 3, 1, 2, 3, 7),
      IS(8, 5, 8, 4, 2, 2), IS(7, 7, 8, 1, 9, 5), IS(5, 0, 9, 7, 4, 8), IS(1, 5, 5, 2, 0, 0), IS(1, 7, 7, 9, 0, 8),
      IS(5, 8, 9, 7, 0, 1), IS(5, 3, 9, 4, 6, 4), IS(1, 3, 9, 1, 2, 6), IS(3, 3, 4, 4, 0, 5), IS(8, 4, 9, 3, 7, 7),
      IS(5, 8, 2, 2, 1, 2), IS(2, 5, 5, 3, 4, 5), IS(3, 2, 3, 7, 7, 2), IS(5, 8, 6, 4, 9, 9), IS(8, 4, 0, 4, 3, 2)
    )
    val expected = IS(
      IS(
        IS(0, 0, 8, 3, 6, 9), IS(0, 2, 9, 7, 9, 8), IS(0, 4, 8, 9, 9, 4), IS(0, 5, 0, 9, 5, 6), IS(0, 5, 3, 6, 5, 9),
        IS(0, 6, 9, 7, 9, 3), IS(0, 9, 5, 9, 1, 9), IS(2, 9, 2, 1, 6, 9), IS(3, 4, 9, 8, 8, 3), IS(3, 5, 5, 9, 9, 5),
        IS(3, 7, 8, 8, 9, 7), IS(3, 9, 0, 6, 2, 8), IS(4, 0, 9, 4, 8, 0), IS(4, 6, 9, 9, 2, 0), IS(4, 8, 6, 7, 7, 8),
        IS(4, 8, 8, 7, 8, 0), IS(4, 9, 5, 8, 2, 0), IS(4, 9, 8, 4, 7, 0), IS(5, 0, 9, 7, 4, 8), IS(5, 1, 7, 3, 4, 9),
        IS(5, 3, 4, 8, 4, 8), IS(5, 3, 9, 4, 6, 4), IS(5, 4, 3, 9, 9, 0), IS(5, 5, 0, 8, 4, 9), IS(5, 8, 4, 8, 8, 4),
        IS(5, 8, 6, 4, 9, 9), IS(5, 8, 9, 7, 0, 1), IS(6, 1, 3, 3, 9, 8), IS(6, 2, 8, 8, 6, 1), IS(6, 3, 5, 5, 8, 9),
        IS(6, 5, 0, 9, 8, 4), IS(6, 6, 8, 4, 5, 7), IS(6, 9, 2, 8, 8, 2), IS(6, 9, 6, 7, 9, 7), IS(6, 9, 8, 7, 1, 0),
        IS(7, 4, 5, 8, 7, 7), IS(7, 4, 8, 3, 9, 2), IS(7, 4, 8, 8, 5, 2), IS(7, 5, 4, 7, 7, 4), IS(7, 8, 0, 8, 5, 7),
        IS(7, 8, 2, 9, 5, 4), IS(7, 8, 8, 9, 3, 9), IS(7, 9, 1, 8, 2, 2), IS(7, 9, 2, 0, 6, 6), IS(8, 3, 4, 4, 9, 7),
        IS(8, 3, 7, 3, 8, 4), IS(8, 4, 4, 7, 6, 5), IS(8, 4, 4, 9, 4, 5), IS(8, 4, 9, 3, 7, 7), IS(8, 5, 1, 9, 0, 6),
        IS(8, 5, 6, 6, 8, 8), IS(8, 6, 0, 5, 9, 8), IS(8, 6, 9, 3, 4, 6), IS(8, 7, 2, 2, 9, 8), IS(8, 7, 4, 4, 7, 0),
        IS(8, 7, 7, 6, 1, 9), IS(8, 7, 8, 7, 5, 1), IS(8, 7, 8, 9, 3, 1), IS(8, 7, 9, 6, 0, 3), IS(8, 9, 2, 1, 7, 0),
        IS(8, 9, 9, 4, 2, 9), IS(9, 0, 9, 9, 7, 4), IS(9, 1, 7, 6, 2, 3), IS(9, 2, 5, 4, 1, 9), IS(9, 2, 5, 8, 0, 7),
        IS(9, 2, 8, 3, 7, 8), IS(9, 4, 0, 5, 6, 7), IS(9, 4, 1, 9, 2, 3), IS(9, 5, 1, 2, 9, 9), IS(9, 6, 3, 7, 6, 2),
        IS(9, 6, 6, 7, 4, 2), IS(9, 6, 7, 2, 6, 3), IS(9, 7, 2, 5, 7, 0), IS(9, 7, 9, 1, 9, 8), IS(9, 7, 9, 2, 1, 5)
      ), IS(
        IS(0, 2, 7, 2, 7, 3), IS(0, 5, 9, 1, 9, 3), IS(0, 6, 7, 9, 3, 8), IS(0, 8, 5, 0, 9, 4), IS(1, 3, 9, 1, 2, 6),
        IS(1, 6, 0, 4, 9, 0), IS(1, 6, 6, 2, 9, 5), IS(2, 2, 5, 7, 9, 1), IS(2, 2, 8, 4, 5, 6), IS(2, 5, 5, 3, 4, 5),
        IS(2, 5, 6, 1, 8, 6), IS(2, 6, 8, 6, 2, 6), IS(2, 7, 2, 4, 5, 9), IS(2, 7, 3, 3, 6, 6), IS(2, 7, 5, 6, 3, 8),
        IS(3, 0, 8, 5, 7, 3), IS(3, 6, 0, 9, 3, 9), IS(3, 7, 8, 8, 8, 0), IS(4, 3, 1, 7, 9, 4), IS(4, 4, 6, 2, 7, 9),
        IS(4, 4, 6, 6, 3, 8), IS(4, 5, 4, 7, 8, 7), IS(4, 5, 6, 6, 5, 4), IS(4, 6, 1, 3, 9, 9), IS(4, 7, 5, 2, 8, 9),
        IS(4, 7, 5, 9, 2, 9), IS(4, 7, 7, 9, 0, 8), IS(4, 9, 2, 1, 0, 7), IS(5, 2, 3, 5, 9, 6), IS(5, 3, 6, 4, 2, 8),
        IS(5, 3, 7, 4, 6, 4), IS(5, 3, 7, 7, 1, 5), IS(5, 5, 5, 7, 5, 2), IS(5, 6, 1, 2, 8, 9), IS(5, 6, 4, 7, 4, 5),
        IS(5, 8, 3, 3, 4, 8), IS(6, 1, 7, 5, 1, 6), IS(6, 2, 1, 7, 8, 1), IS(6, 3, 3, 7, 2, 9), IS(6, 4, 5, 3, 2, 8),
        IS(6, 4, 6, 5, 8, 3), IS(6, 5, 4, 2, 0, 8), IS(6, 7, 8, 7, 2, 3), IS(7, 0, 1, 4, 7, 2), IS(7, 1, 7, 3, 0, 6),
        IS(7, 2, 3, 0, 1, 7), IS(7, 2, 8, 3, 4, 2), IS(7, 2, 8, 9, 3, 1), IS(7, 3, 3, 8, 4, 0), IS(7, 4, 1, 6, 7, 1),
        IS(7, 4, 2, 1, 7, 8), IS(7, 4, 6, 9, 1, 3), IS(7, 5, 6, 7, 3, 6), IS(7, 7, 8, 1, 9, 5), IS(8, 0, 7, 4, 6, 4),
        IS(8, 3, 2, 6, 0, 0), IS(8, 4, 0, 4, 3, 2), IS(8, 4, 1, 0, 5, 6), IS(8, 4, 9, 2, 5, 0), IS(8, 5, 8, 4, 2, 2),
        IS(8, 6, 6, 0, 4, 6), IS(8, 7, 3, 1, 8, 1), IS(8, 8, 1, 2, 0, 5), IS(8, 9, 6, 1, 0, 3), IS(9, 0, 3, 6, 3, 1),
        IS(9, 2, 0, 7, 1, 2), IS(9, 7, 5, 1, 6, 4)
      ), IS(
        IS(0, 7, 2, 9, 1, 1), IS(1, 0, 8, 4, 5, 6), IS(1, 1, 6, 1, 9, 4), IS(1, 3, 4, 9, 1, 1), IS(1, 4, 2, 1, 7, 6),
        IS(1, 4, 6, 3, 6, 1), IS(1, 5, 1, 6, 7, 2), IS(1, 5, 5, 5, 6, 0), IS(1, 6, 1, 2, 7, 7), IS(1, 7, 5, 2, 2, 7),
        IS(1, 7, 5, 6, 2, 4), IS(1, 7, 7, 9, 0, 8), IS(2, 2, 4, 2, 6, 7), IS(2, 3, 2, 8, 2, 5), IS(2, 4, 4, 2, 2, 9),
        IS(2, 6, 3, 3, 5, 4), IS(3, 0, 2, 6, 8, 1), IS(3, 0, 6, 1, 5, 6), IS(3, 2, 3, 7, 7, 2), IS(3, 3, 4, 4, 0, 5),
        IS(3, 4, 0, 2, 3, 7), IS(3, 4, 0, 7, 6, 4), IS(3, 5, 7, 8, 5, 0), IS(3, 6, 4, 6, 1, 2), IS(3, 7, 3, 8, 0, 7),
        IS(4, 1, 8, 0, 2, 5), IS(4, 2, 4, 2, 5, 3), IS(4, 3, 3, 2, 5, 9), IS(5, 0, 1, 5, 6, 1), IS(5, 1, 3, 7, 2, 8),
        IS(5, 2, 5, 7, 0, 3), IS(5, 8, 2, 2, 1, 2), IS(6, 1, 3, 5, 5, 1), IS(7, 2, 5, 2, 3, 6), IS(7, 3, 6, 3, 2, 0),
        IS(7, 5, 4, 5, 1, 2), IS(7, 7, 7, 1, 9, 0), IS(8, 5, 1, 2, 0, 0), IS(9, 0, 4, 1, 0, 4), IS(9, 2, 1, 1, 4, 4),
        IS(9, 6, 1, 0, 5, 0)
      ), IS(
        IS(0, 1, 6, 6, 0, 6), IS(0, 2, 5, 9, 0, 5), IS(0, 4, 1, 0, 3, 3), IS(1, 0, 3, 0, 4, 4), IS(1, 0, 4, 7, 4, 0),
        IS(1, 1, 6, 1, 5, 0), IS(1, 5, 0, 4, 5, 2), IS(1, 5, 3, 3, 1, 3), IS(1, 5, 5, 2, 0, 0), IS(1, 7, 1, 1, 2, 4),
        IS(3, 0, 4, 2, 1, 2), IS(3, 1, 2, 4, 1, 3), IS(3, 4, 6, 4, 2, 0), IS(4, 3, 1, 2, 3, 7), IS(5, 1, 5, 1, 7, 0),
        IS(5, 2, 0, 4, 0, 1)
      ), IS(
        IS(5, 1, 2, 0, 6, 0)
      ))
    val srt = getSorter(6)
    assertEquals(sortNDSResults(expected), sortNDSResults(srt.doSorting(input)))
  }
}
