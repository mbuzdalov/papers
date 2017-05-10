package onell.util

import java.io.{IOException, PrintWriter}

import scala.collection.mutable.{HashMap => MuHashMap, TreeMap => MuTreeMap}
import onell.util.RunHelpers.Statistics
import onell.{Algorithm, MutationAwarePseudoBooleanProblem}

/**
  * A facade for the plotting capabilities.
  */
class Plotter(classifier: MutationAwarePseudoBooleanProblem[_] => (String, Double)) {
  private[this] final val map = new MuHashMap[String, MuTreeMap[String, MuTreeMap[Double, Statistics]]]()

  def append[F](algorithm: Algorithm[F], problem: MutationAwarePseudoBooleanProblem[F], stats: Statistics): Unit = {
    val (token, x) = classifier(problem)
    map.getOrElseUpdate(token, new MuTreeMap()(RunHelpers.numberTokenSorting)).getOrElseUpdate(algorithm.pgfPlotLegend, new MuTreeMap()) += x -> stats
  }

  private def intToString(value: Int): String = {
    if (value < 26) {
      ('A' + value).toChar.toString
    } else {
      val ch = ('A' + value % 26).toChar.toString
      intToString(value / 26) + ch
    }
  }
  def writeAllTikZPlots(filename: String, iqr: Boolean, filter: String => Boolean): Unit = {
    try {
      val pw = new PrintWriter(filename)
      val delta = 0.25
      for ((clazz, plots) <- map if filter(clazz)) {
        pw.println(s"\\newcommand{\\iqrPlot$clazz}[2]{")
        pw.println("  \\begin{tikzpicture}")
        pw.println("    \\begin{axis}[enlargelimits=false, xmode=log, log basis x = 2, cycle list name = my custom, " +
          "xlabel=Problem size, ylabel=Evaluations / problem size, " +
          "width=#1, height=#2, legend pos=outer north east]")
        for (((algo, plot), index) <- plots.toIndexedSeq.zipWithIndex) {
          val tag = intToString(index)
          if (iqr) {
            pw.print(s"      \\addplot[name path=s$tag, forget plot] coordinates {")
            for ((x, stat) <- plot) {
              pw.print(s"($x, ${stat.percentile(0.5 - delta) / x})")
            }
            pw.println("};")
            pw.print(s"      \\addplot[name path=e$tag, forget plot] coordinates {")
            for ((x, stat) <- plot) {
              pw.print(s"($x, ${stat.percentile(0.5 + delta) / x})")
            }
            pw.println("};")
            pw.println(s"      \\addplot fill between[of = s$tag and e$tag];")
          } else {
            pw.print(s"      \\addplot+[mark size=1.2] coordinates {")
            for ((x, stat) <- plot) {
              pw.print(s"($x, ${stat.median / x})")
            }
            pw.println("};")
          }
          pw.println(s"      \\addlegendentry{$algo};")
        }
        pw.println("    \\end{axis}")
        pw.println("  \\end{tikzpicture}")
        pw.println("}")
      }
      pw.close()
    } catch {
      case e: IOException =>
        e.printStackTrace()
    }
  }
}
