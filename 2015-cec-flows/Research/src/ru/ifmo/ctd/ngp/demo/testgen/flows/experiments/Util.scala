package ru.ifmo.ctd.ngp.demo.testgen.flows.experiments

import java.io.{PrintWriter, File}

import ru.ifmo.ctd.ngp.demo.testgen.flows.EdgeRec

object Util {
  /**
   * Prints the given test in the DIMACS format.
   * @param test the test to be printed.
   * @param src the source vertex.
   * @param trg the sink (target) vertex.
   * @param head the head the file should start with.
   * @param file the file where the test should be printed.
   */
  def printDimacs(test: Iterable[EdgeRec], src: Int, trg: Int, head: String, file: File) {
    val dimacsWriter = new PrintWriter(file)
    val nodes = test.map(e => math.max(e.source, e.target)).max + 1
    dimacsWriter.println(
      s"""$head
         |p max $nodes ${test.size}
         |n $src s
         |n $trg t
      """.stripMargin
    )
    for (rec <- test) {
      val s = rec.source
      val t = rec.target
      val c = rec.capacity
      dimacsWriter.println(s"a ${s + 1} ${t + 1} $c")
    }
    dimacsWriter.close()
  }
}
