import java.util.Locale
import scala.language.postfixOps

object Parser extends App {
  val src = scala.io.Source.fromFile(args(0))
  val lines = src.getLines.toIndexedSeq
  src.close()

  def getDoubles(line: String): IndexedSeq[String] = {
    line.split(" ").filter(_.contains(".")).map{ num =>
      if (num(0) == '(') num.substring(1, num.length - 1) else num
    }
  }

  def texed(width: Int)(v: String): String = {
    val ie = v.indexOf('e')
    val num = v.substring(0, ie)
    val mant = ("%.0" + width + "f").formatLocal(Locale.US, num.toDouble)
    val expn = "\\cdot 10^{" + v.substring(ie + 1).toInt + "}"
    "$" + mant + " " + expn + "$"
  }

  def colorMaxTex(seq: IndexedSeq[String], width: Int = 2): IndexedSeq[String] = {
    val seqd = seq.map(_.toDouble)
    val max = seqd.grouped(2).map(_(0)).max
    val qd = seqd.indices.map(t => if (seqd(t & ~1) >= max - 1e-7) "\\qg " else "")
    (qd zip seq.map(texed(width))) map (t => t._1 + t._2)
  }

  def colorMinTex(seq: IndexedSeq[String], width: Int = 2): IndexedSeq[String] = {
    val seqd = seq.map(_.toDouble)
    val min = seqd.grouped(2).map(_(0)).min
    val qd = seqd.indices.map(t => if (seqd(t & ~1) <= min + 1e-7) "\\qg " else "")
    (qd zip seq.map(texed(width))) map (t => t._1 + t._2)
  }

  def startTableNSGA() {
    println("\\begin{table*}[!t]")
    println("\\caption{}\\label{}\\scriptsize")
    println("\\setlength{\\tabcolsep}{0.13em}")
    println("\\begin{tabular}{c||c|c|c|c|c|c||c|c|c|c|c|c}\\hline")
    println("Type & \\multicolumn{2}{c|}{INDS(gen)} & \\multicolumn{2}{c|}{ENLU(gen)} & " +
                    "\\multicolumn{2}{c||}{debNDS(gen)} & \\multicolumn{2}{c|}{INDS(ss)} & " +
                    "\\multicolumn{2}{c|}{ENLU(ss)} & \\multicolumn{2}{c}{debNDS(ss)} \\\\")
    println("\\cline{2-3}\\cline{4-5}\\cline{6-7}\\cline{8-9}\\cline{10-11}\\cline{12-13}")
    println(" & med & IQR & med & IQR & med & IQR & med & IQR & med & IQR & med & IQR \\\\")
  }

  def startTableSteadiness() {
    println("\\begin{table*}[!t]")
    println("\\caption{}\\label{}\\small")
    println("\\setlength{\\tabcolsep}{0.5em}")
    println("\\begin{tabular}{c|cccc|c|cccc}\\hline")
    println("Problem & PSS & SISR & BISR & BIBR & Problem & PSS & SISR & BISR & BIBR \\\\")
  }

  def endTable() {
    println("\\hline\\end{tabular}")
    println("\\end{table*}")
  }

  case class Header(val name: String, val budget: Int, val generationSize: Int)
  object Header {
    def apply(line: String): Header = {
      val data = line.split(" ").filter(_.nonEmpty)
      //| ZDT1 | Budget 25000     | Generation size 100    |
      Header(data(1), data(4).toInt, data(8).toInt)
    }
  }

  args(0) match {
    case "paper-nsga.log" =>
      startTableNSGA()
      lines grouped 13 foreach { grp =>
        val name = Header(grp(1)).name
        val genH = getDoubles(grp(5))
        val genT = getDoubles(grp(6))
        val genC = getDoubles(grp(7))
        val ssH = getDoubles(grp(9))
        val ssT = getDoubles(grp(10))
        val ssC = getDoubles(grp(11))

        println("\\hline\\multicolumn{13}{c}{" + name + "}\\\\\\hline")
        //println(colorMaxTex(genH ++ ssH).mkString("HV & ", " & ", " \\\\"))
        println((colorMinTex(genT) ++ colorMinTex(ssT)).mkString("time & ", " & ", " \\\\"))
        println((colorMinTex(genC) ++ colorMinTex(ssC)).mkString("cmp & ", " & ", " \\\\"))
      }
      endTable()

    case "paper-steadiness.log" =>
      val data = lines grouped 21 map { grp =>
        val name = Header(grp(1)).name
        val vPSS = getDoubles(grp(5)).head
        val vSISR = getDoubles(grp(9)).head
        val vBISR = getDoubles(grp(13)).head
        val vBIBR = getDoubles(grp(17)).head
        (name, IndexedSeq(vPSS, vSISR, vBISR, vBIBR))
      }

      startTableSteadiness()
      data.toIndexedSeq.groupBy(_._1(0)).foreach { grp =>
        println("\\hline")
        grp._2.sortBy(_._1).grouped(2) foreach { sq =>
          val strings0 = sq flatMap { case (name, values) => name +: values.map(texed(3)) }
          val strings = if (sq.size == 2) strings0 else strings0 ++ Seq("", "", "", "", "")
          println(strings.mkString("", " & ", "\\\\"))
        }
      }
      endTable()

    case "paper-convex-hull.log" =>
      case class Entry(problem: String, budget: Int, generationSize: Int,
                       hypervolume: String, runningTimes: IndexedSeq[String])
      val data = lines grouped 9 map { grp =>
        val Header(name, budget, genSize) = Header(grp(1))
        val hv = getDoubles(grp(5)).head
        val times = getDoubles(grp(6))
        Entry(name, budget, genSize, hv, times)
      } toIndexedSeq

      val problems = data.map(_.problem).distinct.sorted
      val columns = data.map(e => (e.budget, e.generationSize)).distinct.sorted

      def generateHeaderRow(seq: IndexedSeq[Int], sb: StringBuilder = new StringBuilder()): String = {
        if (seq.isEmpty) {
          sb.toString
        } else {
          val newSeq = seq.dropWhile(_ == seq(0))
          val count = seq.size - newSeq.size
          if (newSeq.isEmpty) {
            sb.append(s" & \\multicolumn{${2 * count}}{c}{${seq(0)}}")
          } else {
            sb.append(s" & \\multicolumn{${2 * count}}{c|}{${seq(0)}}")
          }
          generateHeaderRow(newSeq, sb)
        }
      }

      println("\\scriptsize")
      println("\\setlength{\\tabcolsep}{0.1em}")
      println("\\centering")
      println("\\newcommand\\qeq{\\cellcolor{gray!50}}")
      println("\\newcommand\\qgt{\\cellcolor{gray}}")
      println(s"\\begin{tabular}{r*{${columns.size}}{|l|l}}\\hline")
      val cline = s"\\cline{2-${2 * columns.size + 1}}"
      println("Problem " + generateHeaderRow(columns.map(_._1)) + s" \\\\$cline")
      println(generateHeaderRow(columns.map(_._2)) + "\\\\")
      println(Seq.fill(columns.size - 1)(" & \\multicolumn{1}{c|}{INDS/HV} & \\multicolumn{1}{c|}{Hull/Ratio}")
                    mkString (cline, "", " & \\multicolumn{1}{c|}{INDS/HV} & \\multicolumn{1}{c}{Hull/Ratio} \\\\"))
      for (problem <- problems) {
        println(" \\hline")
        print(problem)
        val entries = for ((budget, generationSize) <- columns) yield {
          val currEntries = data.filter(e => e.problem == problem && e.budget == budget && e.generationSize == generationSize)
          if (currEntries.size != 1) {
            throw new AssertionError(
              s"Cannot understand problem = $problem, budget = $budget, generationSize = $generationSize: The entry count = ${currEntries.size}"
            )
          }
          currEntries.head
        }
        for (entry <- entries) {
          val Seq(inds, indspm, hull, hullpm) = entry.runningTimes.map(_.toDouble)
          val colorPrefix = if (inds + indspm < hull - hullpm) {
            ""
          } else if (hull + hullpm < inds - indspm) {
            "\\qgt"
          } else {
            "\\qeq"
          }
          println(s" & $colorPrefix ${texed(2)(entry.runningTimes(0))} & $colorPrefix ${texed(2)(entry.runningTimes(2))}")
        }
        println(s" \\\\$cline")
        for (entry <- entries) {
          print("& " + texed(2)(entry.hypervolume) + " & " + texed(2)("%.02e".formatLocal(Locale.US, entry.runningTimes(0).toDouble / entry.runningTimes(2).toDouble)))
        }
        println(" \\\\")
      }
      println("\\hline\\end{tabular}")

    case _ =>
      println("Error: don't know how to process file " + args(0))
  }
}
