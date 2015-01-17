import java.io.PrintWriter
import java.util.StringTokenizer

import scala.io.Source

/**
 * This class collects averages from the CSV data from experiments
 * and saves it to a bunch of data files readable by Metapost.
 * @author Maxim Buzdalov
 */
object Parser extends App {
    case class Run(attempt: Int, generator: String, size: Int, sorter: String, time: Double, comparisons: Long)
    case class RunAverage(generator: String, size: Int, sorter: String, time: Double, comparisons: Double)

    val inputSource = Source.fromFile("lastRun.csv")
    val lines = inputSource.getLines.toIndexedSeq.drop(1)
    inputSource.close()

    val runs = lines map { line =>
        val tok = new StringTokenizer(line, "\";")
        Run(
            attempt = tok.nextToken.toInt,
            generator = tok.nextToken,
            size = tok.nextToken.toInt,
            sorter = tok.nextToken,
            time = tok.nextToken.replace(',', '.').toDouble,
            comparisons = tok.nextToken.toLong
        )
    }

    val averaged = runs groupBy {
        run => (run.generator, run.size, run.sorter)
    } map {
        case ((generator, size, sorter), list) => RunAverage(
            generator = generator,
            size = size,
            sorter = sorter,
            time = list.map(_.time).sum / list.size,
            comparisons = list.map(_.comparisons).sum / list.size.toDouble
        )
    }

    val remapGeneratorNames = Map(
        "parper"            -> "parper",
        "parallel fronts"   -> "parallel",
        "stripe"            -> "stripe",
        "diag"              -> "diag1",
        "diag2"             -> "diag2",
        "diagRand"          -> "diagR",
        "square"            -> "square",
        "circle fronts"     -> "circle"
    )

    val remapSorterNames = Map(
        "my"    -> "proposed",
        "NSGA2" -> "fastNDS",
        "ENLU"  -> "ENLU"
    )

    averaged groupBy {
        run => (run.generator, run.sorter)
    } foreach {
        case ((generator, sorter), list) =>
            val rgn = remapGeneratorNames(generator)
            val rsn = remapSorterNames(sorter)
            for ((name, mapper) <- Seq[(String, RunAverage => Double)](("time", _.time), ("comp", _.comparisons))) {
                val out = new PrintWriter(s"$rgn-$rsn-$name.dat")
                list.toIndexedSeq sortBy(_.size) foreach {
                    l => out.println(f"${l.size}%d ${mapper(l)}%.02e".replace(',', '.'))
                }
                out.close()
            }
    }
}
