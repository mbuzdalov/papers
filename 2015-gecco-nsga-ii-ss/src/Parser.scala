import java.util.Locale

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
    println("\\caption{}\\label{}\\small")
    println("\\setlength{\\tabcolsep}{0.15em}")
    println("\\begin{tabular}{c||c|c|c|c|c|c||c|c|c|c|c|c}\\hline")
    println("Type & \\multicolumn{2}{c|}{INDS(gen)} & \\multicolumn{2}{c|}{ENLU(gen)} & " +
                    "\\multicolumn{2}{c||}{debNDS(gen)} & \\multicolumn{2}{c|}{INDS(ss)} & " +
                    "\\multicolumn{2}{c|}{ENLU(ss)} & \\multicolumn{2}{c}{debNDS(ss)} \\\\")
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

  args(0) match {
    case "paper-nsga.log" =>
      startTableNSGA()
      lines grouped 13 foreach { grp =>
        val name = grp(1).drop(1).dropRight(1).trim
        val ssH = getDoubles(grp(5))
        val ssT = getDoubles(grp(6))
        val ssC = getDoubles(grp(7))
        val genH = getDoubles(grp(9))
        val genT = getDoubles(grp(10))
        val genC = getDoubles(grp(11))

        if (name == "WFG1") {
          endTable()
          startTableNSGA()
        }

        println("\\hline\\multicolumn{13}{c}{" + name + "}\\\\\\hline")
        println(colorMaxTex(genH ++ ssH).mkString("HV & ", " & ", " \\\\"))
        println((colorMinTex(genT) ++ colorMinTex(ssT)).mkString("time & ", " & ", " \\\\"))
        println((colorMinTex(genC) ++ colorMinTex(ssC)).mkString("cmp & ", " & ", " \\\\"))
      }
      endTable()

    case "paper-steadiness.log" =>
      val data = lines grouped 21 map { grp =>
        val name = grp(1).drop(1).dropRight(1).trim
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

    case _ =>
      println("Error: don't know how to process file " + args(0))
  }
}
