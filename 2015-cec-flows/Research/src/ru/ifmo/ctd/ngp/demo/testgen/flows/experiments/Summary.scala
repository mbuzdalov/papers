package ru.ifmo.ctd.ngp.demo.testgen.flows.experiments

import java.io._
import java.util.Locale

import scala.collection.JavaConverters._
import scala.collection.mutable.{HashSet => MHashSet}

import ru.ifmo.ctd.ngp.util.{StatUtil => SUtil}

import ru.ifmo.ctd.ngp.demo.testgen.flows.MaxFlowSolver
import ru.ifmo.ctd.ngp.demo.testgen.flows.solvers._

/**
 * Printing out the summary for all algorithms and all tests.
 */
object Summary extends App {
  object RedirectOut {
    def apply(name: String)(code: => Any) {
      val oldOut = scala.Console.out
      val newStream = new PrintStream(name)
      scala.Console.withOut(newStream)(code)
      newStream.close()
    }
  }

  def collect(f: File, res: File => Unit): Unit = {
    if (f.isFile && f.getName.endsWith(".dimacs")) {
      res(f)
    } else if (f.isDirectory) {
      f.listFiles().foreach(t => collect(t, res))
    }
  }
  val dimacsFiles = {
    val seq = IndexedSeq.newBuilder[File]
    collect(new File("flows/tests"), seq += _)
    seq.result().sorted
  }

  case class RunResult(
    algorithm: String,
    test: String,
    testClass: String,
    cpuTime: Long,
    wallClockTime: Long,
    traversalCount: Long,
    vertexCount: Long,
    edgeCount: Long,
    resultMap: Map[String, Long]
  )

  val cacheFile = new File(s"flows/summary/cache-${dimacsFiles.length}")
  cacheFile.getParentFile.mkdirs()

  val results = if (cacheFile.exists()) {
    val ser = new ObjectInputStream(new FileInputStream(cacheFile))
    val rv = ser.readObject().asInstanceOf[Seq[RunResult]]
    ser.close()
    rv
  } else {
    val algorithms = IndexedSeq(
      new FordFulkersonScaling,
      new EdmondsCarp,
      new EdmondsCarpScaling,
      new Dinic,
      new DinicSlow,
      new ImprovedShortestPath
    )

    //Warm-up phase
    for (f <- dimacsFiles) {
      val set = new MHashSet[Long]()
      for (a <- algorithms) {
        set += a.solveDimacs(f, 5000).get(MaxFlowSolver.ANSWER_KEY)
      }
      assert(set.size == 1, s"Some of the algorithms give different answers for test $f: ${set.mkString(", ")}")
    }
    val computed = for (a <- algorithms; f <- dimacsFiles) yield {
      val map = a.solveDimacs(f, 5000).asScala.mapValues(_.longValue()).toMap
      val fileName = f.getName
      val test = fileName.substring(0, fileName.lastIndexOf('.'))
      val testClass = if (test.takeRight(5).take(3) == "run") test.dropRight(6) else test
      RunResult(
        algorithm = a.getName,
        test = test,
        testClass = testClass,
        cpuTime = map(MaxFlowSolver.TIME_KEY),
        wallClockTime = map(MaxFlowSolver.WC_TIME_KEY),
        traversalCount = map.getOrElse("dfsCount", map.getOrElse("bfsCount", map("retreatCount"))),
        vertexCount = map("vertexCount"),
        edgeCount = map("edgeCount"),
        resultMap = map
      )
    }
    val ser = new ObjectOutputStream(new FileOutputStream(cacheFile))
    ser.writeObject(computed)
    ser.close()
    computed
  }

  def totalTable(): Unit = {
    val (header, extSource) = IndexedSeq[(String, RunResult => String)](
      ("Test",            _.test),
      ("Algorithm",       _.algorithm),
      ("User CPU Time",   _.cpuTime.toString),
      ("Wall Clock Time", _.wallClockTime.toString),
      ("Traversal",       _.traversalCount.toString),
      ("Vertices",        _.vertexCount.toString),
      ("Edges",           _.edgeCount.toString)
    ).unzip
    val extractor: RunResult => IndexedSeq[String] = r => extSource.map(_(r))

    val resultsSorted = results.sortBy(-_.edgeCount).sortBy(_.algorithm).map(extractor)
    val lengths = (header +: resultsSorted).transpose.map(_.map(_.length).max)
    val formatString = lengths.map(t => s"%${t}s").mkString("", " & ", "\\\\\n")

    printf(formatString, header :_*)
    resultsSorted.foreach(t => printf(formatString, t :_*))
  }

  def med(a: Seq[Long]) = {
    require(a.nonEmpty, "no median of empty collection")
    val s = a.sorted
    val hs = s.size / 2
    if (s.size % 2 == 1) s(hs).toDouble else (s(hs - 1) + s(hs)) / 2.0
  }

  def classTable(): Unit = {
    def fmt(v: Double) = "%.1f".formatLocal(Locale.US, v)
    def medOf(single: RunResult => Long): Seq[RunResult] => String = t => fmt(med(t.map(single)))
    def minOf(single: RunResult => Long): Seq[RunResult] => String = t => t.map(single).min.toString
    def maxOf(single: RunResult => Long): Seq[RunResult] => String = t => t.map(single).max.toString
    val (header, extSource) = IndexedSeq[(String, Seq[RunResult] => String)](
      ("Test Class", _(0).testClass),
      ("Algorithm",  _(0).algorithm),
      ("Med CPU",    medOf(_.cpuTime)),
      ("Min WC",     minOf(_.wallClockTime)),
      ("Med WC",     medOf(_.wallClockTime)),
      ("Max WC",     maxOf(_.wallClockTime)),
      ("Traversal",  medOf(_.traversalCount)),
      ("Vertices",   medOf(_.vertexCount)),
      ("Min Edges",  minOf(_.edgeCount)),
      ("Med Edges",  medOf(_.edgeCount)),
      ("Max Edges",  maxOf(_.edgeCount))
    ).unzip

    val sortIdx = header.indexOf("Med Edges")
    val stringsUngrouped = for ((algo, res) <- results.groupBy(_.algorithm)) yield {
      val extractor: Seq[RunResult] => IndexedSeq[String] = r => extSource.map(_(r))
      res.groupBy(_.testClass).map(t => extractor(t._2)).toIndexedSeq.sortBy(-_(sortIdx).toDouble)
    }
    val resultsSorted = stringsUngrouped.flatten.toIndexedSeq
    val lengths = (header +: resultsSorted).transpose.map(_.map(_.length).max)
    val formatString = lengths.map(t => s"%${t}s").mkString("", " & ", "\\\\\n")

    printf(formatString, header :_*)
    resultsSorted.foreach(t => printf(formatString, t :_*))
  }

  def correlationCoefficients(): Unit = {
    def meanDev(seq: Seq[Double]): (Double, Double) = {
      val mean = seq.sum / seq.size
      val dev = math.sqrt(seq.map(t => (t - mean) * (t - mean)).sum / (seq.size - 1))
      (mean, dev)
    }

    val keys = IndexedSeq("time", "edgeCount", "vertexCount", "dfsCount", "bfsCount", "retreatCount", "phaseCount")

    val table = ("" +: keys) +: (for ((algo, data) <- results.groupBy(_.algorithm).toIndexedSeq.sortBy(_._1)) yield {
      val (wcMean, wcDev) = meanDev(data.map(_.wallClockTime.toDouble))
      algo.filter(_.isUpper) +: (for (key <- keys) yield {
        if (data(0).resultMap.contains(key)) {
          val (kMean, kDev) = meanDev(data.map(_.resultMap(key).toDouble))
          val r = data.map(t => (t.wallClockTime - wcMean) / wcDev * (t.resultMap(key) - kMean) / kDev).sum / (data.size - 1)
          f"$r%.5f"
        } else {
          "---"
        }
      }).toIndexedSeq
    }).toIndexedSeq

    val tt = table.transpose

    println(s"\\begin{tabular}{|*{${tt(0).size}}{c|}}\\hline")
    for (row <- tt) println(row.mkString("", " & ", "\\\\\\hline"))
    println("\\end{tabular}")
  }

  def classAverageRank(): Unit = {
    val ranksPerAlgo = for ((_, data) <- results.groupBy(_.algorithm)) yield {
      data zip SUtil.ranks(data.map(_.edgeCount)).map(data.size - _ + 1)
    }
    val byTest = ranksPerAlgo.flatten.groupBy(_._1.test)
    val triples = byTest map {
      case (test, itr) => (test, itr.head._1.testClass, itr.map(_._2).sum / itr.size)
    }
    val byClassDec = triples.groupBy(_._2).mapValues(t => t.map(_._3).sum / t.size).toIndexedSeq.sortBy(_._2)
    for ((cls, v) <- byClassDec) {
      println(s"$cls -> $v")
    }
  }

  RedirectOut("flows/summary/classTable.tex")(classTable())
  RedirectOut("flows/summary/totalTable.tex")(totalTable())
  RedirectOut("flows/summary/correlationCoefficients.tex")(correlationCoefficients())
  RedirectOut("flows/summary/classAverageRank.txt")(classAverageRank())
}
