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

  def texed(v: String): String = {
    val ie = v.indexOf('e')
    val num = v.substring(0, ie)
    val mant = ("%.0" + (num.length - 3) + "f").formatLocal(Locale.US, num.toDouble)
    val expn = "\\cdot 10^{" + v.substring(ie + 1).toInt + "}"
    "$" + mant + " " + expn + "$"
  }

  def startTable() {
    println("\\begin{table*}[!t]")
    println("\\caption{}\\label{}\\small")
    println("\\setlength{\\tabcolsep}{0.15em}")
    println("\\begin{tabular}{c||c|c|c|c|c|c||c|c|c|c|c|c}\\hline")
    println("Type & \\multicolumn{2}{c|}{INDS(gen)} & \\multicolumn{2}{c|}{ENLU(gen)} & " +
                   "\\multicolumn{2}{c||}{debNDS(gen)} & \\multicolumn{2}{c|}{INDS(ss)} & " +
                   "\\multicolumn{2}{c|}{ENLU(ss)} & \\multicolumn{2}{c}{debNDS(ss)} \\\\")
    println(" & med & IQR & med & IQR & med & IQR & med & IQR & med & IQR & med & IQR \\\\")
  }

  def endTable() {
    println("\\hline\\end{tabular}")
    println("\\end{table*}")
  }

  def colorMaxTex(seq: IndexedSeq[String]): IndexedSeq[String] = {
    val seqd = seq.map(_.toDouble)
    val max = seqd.grouped(2).map(_(0)).max
    val qd = seqd.indices.map(t => if (seqd(t & ~1) >= max - 1e-7) "\\qg " else "")
    (qd zip seq.map(texed)) map (t => t._1 + t._2)
  }

  def colorMinTex(seq: IndexedSeq[String]): IndexedSeq[String] = {
    val seqd = seq.map(_.toDouble)
    val min = seqd.grouped(2).map(_(0)).min
    val qd = seqd.indices.map(t => if (seqd(t & ~1) <= min + 1e-7) "\\qg " else "")
    (qd zip seq.map(texed)) map (t => t._1 + t._2)
  }

  startTable()
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
      startTable()
    }

    println("\\hline\\multicolumn{13}{c}{" + name + "}\\\\\\hline")
    println(colorMaxTex(genH ++ ssH).mkString("HV & ", " & ", " \\\\"))
    println((colorMinTex(genT) ++ colorMinTex(ssT)).mkString("time & ", " & ", " \\\\"))
    println((colorMinTex(genC) ++ colorMinTex(ssC)).mkString("cmp & ", " & ", " \\\\"))
  }
  endTable()
}
