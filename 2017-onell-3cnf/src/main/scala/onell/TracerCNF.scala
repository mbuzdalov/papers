package onell

import java.io.PrintWriter
import java.util.Locale

import onell.algorithms.OnePlusLambdaLambdaGA
import onell.problems.Random3CNF

/**
  * The main class for additional experiments for lambda traces occurring in unconstrained faulty adaptation.
  */
object TracerCNF {
  class OptimizingAppender(n: Int, eps: Double) {
    private val builder = IndexedSeq.newBuilder[(Double, Double)]
    private var prevX: Option[Double] = None
    private var maxY: Double = 0
    private var minY: Double = 0
    private var firstY: Double = 0
    private var lastY: Double = 0

    def append(x: Int, y: Double): Unit = {
      val xx = math.sqrt(n.toDouble / x)
      prevX match {
        case None =>
          builder += xx -> y
          prevX = Some(xx)
          maxY = y
          minY = y
          firstY = y
          lastY = y
        case Some(px) =>
          if (xx < px + eps) {
            maxY = math.max(maxY, y)
            minY = math.min(minY, y)
            lastY = y
          } else {
            if (maxY != firstY) {
              builder += px -> maxY
            }
            if (minY != maxY) {
              builder += px -> minY
            }
            if (lastY != minY) {
              builder += px -> lastY
            }
            builder += xx -> y
            prevX = Some(xx)
            maxY = y
            minY = y
            firstY = y
            lastY = y
          }
      }
    }

    def result(): IndexedSeq[(Double, Double)] = builder.result().init
  }

  class Tracer(problem: Random3CNF.Instance, appender: OptimizingAppender) extends OnePlusLambdaLambdaGA.Tracer {
    override def trace(individual: Array[Boolean], lambda: Double, evaluations: Long, iterations: Long): Unit = {
      appender.append(problem.distance(individual), lambda)
    }

    override def traceChange(individual: Array[Boolean], lambda: Double, evaluations: Long, iterations: Long,
                             diffFromPrevious: Array[Int], diffSize: Int): Unit = {
      appender.append(problem.distance(individual), lambda)
    }
  }

  def main(args: Array[String]): Unit = {
    Locale.setDefault(Locale.US)

    def getOneLL = new OnePlusLambdaLambdaGA[Int](OnePlusLambdaLambdaGA.adaptiveDefault())
    def getRandom3CNF(n: Int) = new Random3CNF(n, (4 * n * math.log(n)).toInt)

    def getTrace(n: Int): Seq[(Double, Double)] = {
      val appender = new OptimizingAppender(n, 0.05)
      val algo = getOneLL
      val problem = getRandom3CNF(n).newInstance
      val t0 = System.currentTimeMillis()
      algo.solve(problem, Some(new Tracer(problem, appender)))
      val time = System.currentTimeMillis() - t0
      val p = appender.result()
      println(s"getTrace($n) exited with optimized path of length ${p.size} in $time ms")
      p
    }

    def writeTraces(n: Int, runs: Int, title: String, pw: PrintWriter): Unit = {
      pw.println(s"\\newcommand{\\$title}[2]{")
      pw.println("  \\begin{tikzpicture}")
      pw.println("    \\begin{loglogaxis}[[enlargelimits=false, scale only axis, cycle list name=escape-style, " +
        "xlabel={$\\sqrt{n / d}$}, ylabel={$\\lambda$}, log basis x = 2, log basis y = 2, width=#1, height=#2]")
      for (trace <- (0 until runs).par.map(_ => getTrace(n)).seq) {
        pw.print("      \\addplot coordinates {")
        pw.println(trace.grouped(10).map(_.map(p => f"(${p._1}%.3g, ${p._2}%.3g)").mkString(" ")).mkString("\n        ", "\n        ", "\n      };"))
      }
      val sqrtN = math.sqrt(n)
      pw.println(s"      \\addplot coordinates {(1,1) ($sqrtN,$sqrtN)};")
      pw.println("    \\end{loglogaxis}")
      pw.println("  \\end{tikzpicture}")
      pw.println("}")
    }

    val pw = new PrintWriter("/home/maxbuzz/repos/itmo/genome-work/ai-papers/conferences/GECCO/2017/onell-random3cnf/pic/escape.tex")
    for ((n, t) <- Seq(
      (1 << 9, "escapeNine"),
      (1 << 10, "escapeTen"),
      (1 << 11, "escapeEleven"),
      (1 << 12, "escapeTwelve"),
      (1 << 13, "escapeThirteen"),
      (1 << 14, "escapeFourteen"),
      (1 << 15, "escapeFifteen"),
      (1 << 16, "escapeSixteen")
    )) {
      writeTraces(n, 5, t, pw)
    }
    pw.close()
  }
}
