package proteins.es

import java.io.File
import java.util.Locale

import proteins.{MotionHelper, Structure}
import proteins.encodings.SinCosEncoding
import proteins.intersection.{TernaryShortestDistance, NaiveIntersector}

/**
 * A main class for all-pairs motion prediction comparison.
 */
object AllPairsComparison extends App {
  type IS[+T] = IndexedSeq[T]
  val IS = IndexedSeq

  def readFromRoot(root: String): IS[IS[IS[Structure]]] = {
    val subFiles = new File(root).listFiles()
    val indexed = subFiles.groupBy{ file =>
      val fromTo = file.getName.split('-').map(_.toInt)
      (fromTo(0), fromTo(1))
    }.mapValues(_(0))
    val indices = indexed.keys.flatMap(t => Seq(t._1, t._2))
    val min = indices.min
    val max = indices.max
    for (l <- 0 to max - min) yield {
      for (f <- 0 until l) yield {
        val pairRoot = indexed((f + min) -> (l + min))
        val pdb = Seq("opt-result.pdb", "trmOptimized.pdb").flatMap{name =>
          val pdbFile = new File(pairRoot, name)
          if (pdbFile.exists()) {
            Some(Common.readAll(pdbFile.getPath))
          } else {
            None
          }
        }
        assert(pdb.size == 1)
        pdb(0)
      }
    }
  }

  def computeWeights(a: IS[Structure]): (Double, Double) = {
    val mh = new MotionHelper(a.head, a.last, 0, Set())
      with SinCosEncoding with NaiveIntersector with TernaryShortestDistance
    val threshold = math.min(mh.src.lengths.min, mh.trg.lengths.min)
    val stats = (1 until a.size) map { i =>
      val align = mh.align(a(i - 1).atoms.map(_.location), a(i).atoms.map(_.location))
      val isect = mh.computeIntersectionPenalty(align.firstChain, align.secondChain, threshold)
      val wrmsd = align.weighedRMSD
      (i, wrmsd, wrmsd * wrmsd * mh.src.atoms.size, isect)
    }
    (stats.map(_._3).sum, stats.map(_._4).sum)
  }

  val root0 = readFromRoot(args(0))
  val root1 = readFromRoot(args(1))

  assert(root0.size == root1.size)
  for (i <- 0 until root0.size) {
    assert(root0(i).size == root1(i).size)
  }

  val weights0 = root0.map(_ map computeWeights)
  val weights1 = root1.map(_ map computeWeights)

  val positives = IndexedSeq.newBuilder[Double]
  val negatives = IndexedSeq.newBuilder[Double]
  var intersections0 = 0.0
  var intersections1 = 0.0
  for (i <- weights0.indices) {
    for (j <- weights0(i).indices) {
      val (v0, x0) = weights0(i)(j)
      val (v1, x1) = weights1(i)(j)
      intersections0 += x0
      intersections1 += x1
      val ratio = math.abs(v0 - v1) / math.min(v0, v1)
      if (v0 + 1e-6 < v1) {
        positives += ratio
      } else if (v0 > v1 + 1e-6) {
        negatives += ratio
      }
      val ch = if (v0 + 1e-6 < v1) "+" else if (v0 > v1 + 1e-6) "-" else "?"
      println(s"$i -> $j: [$ch]: $ratio (0 -> ($v0, $x0), 1 -> ($v1, $x1))")
    }
  }

  def quantiles(b: IndexedSeq[Double], name: String) {
    val med = if (b.size % 2 == 1) b(b.size / 2) else (b(b.size / 2) + b(b.size / 2 + 1)) / 2
    println(s"$name count: ${b.size} min: ${b.head} max: ${b.last} avg: ${b.sum / b.size} med: $med fifth: ${b(b.size - 5)}")
  }

  val pRes = positives.result().sorted
  val nRes = negatives.result().sorted
  quantiles(pRes, "Positive")
  quantiles(nRes, "Negative")
  println(s"Intersections: first set total: $intersections0, second set total: $intersections1")

  val n = weights0.size
  println("\\begin{tabular}{|r|" + Seq.fill(n)("c|").mkString("") + "}\\hline")
  println((1 to n).mkString(" & ", " & ", "\\\\\\hline"))
  for (row <- 0 until n) {
    print(row + 1)
    for (col <- 0 until n) {
      print(" & ")
      if (row != col) {
        val (currW, _)  = if (row < col) weights0(col)(row) else weights1(row)(col)
        val (otherW, _) = if (row < col) weights1(col)(row) else weights0(row)(col)
        if (currW < otherW) {
          print("\\cellcolor{gray!20} ")
        }
        print("%.0f".formatLocal(java.util.Locale.US, currW))
      }
    }
    println("\\\\\\hline")
  }
  println("\\end{tabular}")
  println("Plot source:")
  for (i <- nRes.indices.reverse) {
    println("%d %.4f".formatLocal(Locale.US, -i - 1, -nRes(i)))
  }
  println("0 0")
  for (i <- pRes.indices) {
    println("%d %.4f".formatLocal(Locale.US, i + 1, pRes(i)))
  }
  println("Plot source ends.")
}
