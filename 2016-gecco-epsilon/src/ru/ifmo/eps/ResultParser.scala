package ru.ifmo.eps

import java.io._
import java.util.Locale

import scala.io.Source

object ResultParser {
  case class Result(generator: String, solver: String, n: Int, d: Int, result: Double)
  case class Context(generator: String, n: Int, d: Int)
  case class SolverRun(solver: String, result: Double)

  object SolverRun {
    def unapply(in: String): Option[SolverRun] = {
      val colon = in.indexOf(':')
      val sec = in.indexOf("sec", colon + 1)
      if (colon >= 0 && sec > colon) {
        try {
          Some(SolverRun(in.substring(0, colon).trim, in.substring(colon + 1, sec).trim.toDouble))
        } catch {
          case th: Throwable => None
        }
      } else None
    }
  }

  object Context {
    def unapply(in: String): Option[Context] = {
      val openB = in.indexOf('[')
      val closeB = in.indexOf(']', openB + 1)
      val nEq = in.indexOf("n = ", closeB)
      val dEq = in.indexOf("d = ", nEq)
      if (openB >= 0 && closeB > openB && nEq > closeB && dEq > closeB) {
        val nEqEnd = in.indexOf(' ', nEq + 4)
        val dEqEnd = in.indexOf(' ', dEq + 4)
        try {
          Some(Context(
            in.substring(openB + 1, closeB),
            (if (nEqEnd == -1) in.substring(nEq + 4) else in.substring(nEq + 4, nEqEnd)).toInt,
            (if (dEqEnd == -1) in.substring(dEq + 4) else in.substring(dEq + 4, dEqEnd)).toInt
          ))
        } catch {
          case th: Throwable =>
            th.printStackTrace()
            None
        }
      } else None
    }
  }

  // Don't try reusing this in your code, this is tailored for the data
  def double2tex(value: Double): String = {
    val str = "%.02e".format(value)
    "$" + str.replace("e+0", " \\cdot 10^{").replace("e-0", " \\cdot 10^{-") + "}$"
  }

  def parse(file: String): Seq[Result] = {
    val builder = IndexedSeq.newBuilder[Result]

    def parse(context: Option[Context], lines: Iterator[String]): Unit = {
      if (lines.hasNext) {
        lines.next() match {
          case Context(c) =>
            parse(Some(c), lines)
          case SolverRun(r) =>
            val c = context.get
            builder += Result(c.generator, r.solver, c.n, c.d, r.result)
            parse(context, lines)
          case s: String =>
            println(s"Warning: a string of unknown format: '$s'")
            parse(context, lines)
        }
      }
    }

    val in = Source.fromFile(file)
    parse(None, in.getLines)
    in.close()

    builder.result()
  }

  def main(args: Array[String]): Unit = {
    if (args.length < 3) {
      println("Usage: scala ResultParser <file-with-results> <file-for-ratios> <target-directory>")
      sys.exit(1)
    } else {
      val generatorFullNames = Map("flatPoints" -> "coplanar points", "randomPoints" -> "random points")
      val solverFullNames = Map("NaiveBinaryEpsilon" -> "naive", "ORQBinaryEpsilon(TreeORQ)" -> "tree", "ORQ2BinaryEpsilon" -> "div-conq")

      Locale.setDefault(Locale.US)
      val results = parse(args(0))
      val dashingSeq = IndexedSeq("solid", "dashed", "dashdotted")
      for ((g, resultsG) <- results.groupBy(_.generator)) {
        val outFigures = new PrintWriter(s"${args(2)}/figures-$g.tex")
        val outTables = new PrintWriter(s"${args(2)}/tables-$g.tex")
        for ((d, resultsD) <- resultsG.groupBy(_.d).toIndexedSeq.sortBy(_._1)) {
          // Printing figures
          outFigures.println("""
            |\begin{figure}[!t]
            |\centering
            |\resizebox{\columnwidth} {!} {
            |\begin{tikzpicture}[scale=1]
            |\begin{loglogaxis}[xlabel=Number of points in the arguments, ylabel=Running time, width=3.7in, height=1.7in, legend pos=north west, minor tick length = 0pt]
          """.stripMargin)
          for (((s, resultsS), dash) <- resultsD.groupBy(_.solver).toIndexedSeq.sortBy(_._1).zip(dashingSeq)) {
            val coordinates = resultsS.sortBy(_.n).map(r => f"(${r.n}%d, ${r.result}%f)").mkString("{", " ", "}")
            outFigures.println(s"  \\addplot coordinates $coordinates;")
            outFigures.println(s"  \\addlegendentry{${solverFullNames(s)}};")
          }
          outFigures.println(s"""
            |\\end{loglogaxis}
            |\\end{tikzpicture}
            |}
            |\\caption{Plots for ${generatorFullNames(g)}, $$k = $d$$}
            |\\label{plot:$g:$d}
            |\\end{figure}
          """.stripMargin)
          // Printing tables
          outTables.println(s"""
            |\\begin{table}[!t]
            |\\centering
            |\\resizebox{\\columnwidth} {!} {
            |\\setlength{\\tabcolsep}{0.4em}
            |\\begin{tabular}{|r|${solverFullNames.map(_ => "l|").mkString("", "", "}\\hline")}
          """.stripMargin)
          outTables.println(solverFullNames.values.map(t => s"\\multicolumn{1}{c|}{$t}").mkString("$n$ & ", " & ", " \\\\\\hline"));
          for ((n, resultsN) <- resultsD.groupBy(_.n).toIndexedSeq.sortBy(_._1)) {
            outTables.print(s"$n")
            for ((k, v) <- solverFullNames) {
              outTables.print(" & ")
              outTables.print(double2tex(resultsN.find(_.solver == k).get.result))
            }
            outTables.println("\\\\\\hline")
          }
          outTables.println(s"""
            |\\end{tabular}
            |}
            |\\caption{Data for ${generatorFullNames(g)}, $$k = $d$$}
            |\\label{table:$g:$d}
            |\\end{table}
          """.stripMargin)
        }
        outTables.close()
        outFigures.close()
      }

      val ratioFile = new PrintWriter(args(1))
      val maxRatioSeq = for (((solver, n, d), resultsForG) <- results.groupBy(r => (r.solver, r.n, r.d))) yield {
        val times = resultsForG.map(_.result).toIndexedSeq.sorted
        val ratio = times.last / times.head
        ratioFile.println(s"($solver, $n, $d) => ${times.mkString("[", ", ", "]")}, $ratio")
        ratio
      }
      val maxRatioSeqSorted = maxRatioSeq.toIndexedSeq.sorted
      ratioFile.println(s"Maximum 5 ratios: ${maxRatioSeqSorted.takeRight(5).mkString("[", ",", "]")}")
      ratioFile.close()
    }
  }
}
