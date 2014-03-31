package knapsack

import java.io.{FileWriter, PrintWriter, File}

import knapsack.solvers._
import knapsack.Config._

/**
 * Runner for the genetic algorithms against knapsack problem solvers
 *
 * @author Maxim Buzdalov
 */
object Runner extends App {
  case class Optimizer(name: String,
                       private val fun: (KnapsackRunLimits, KnapsackSolver) => Result) {
    def apply(limits: KnapsackRunLimits, solver: KnapsackSolver): Result = {
      val t0 = System.currentTimeMillis()
      val res = fun(limits, solver)
      val time = System.currentTimeMillis() - t0
      println(s"[$name on ${solver.getName}]: ${res.fitness}, total time $time")
      res
    }
  }

  val D = new File("runner-results")
  val limits = Seq(
    (100, new KnapsackRunLimits(20, 10000, 10000, 500000))
  )
  val solvers = Seq(
    SimpleBranch.getInstance(),
    ExpKnapPart.getInstance(),
    ExpKnap.getInstance(),
    HardKnapPart.getInstance(),
    HardKnap.getInstance()
  )

  val optimizers = Seq(
    Optimizer("TwoEqual Random", new TwoEqual(50, _, _).runRandom()),
    Optimizer("AnyData NGP", new AnyData(50, _, _).runEvolution()),
    Optimizer("SubsetSum NGP", new SubsetSum(50, _, _).runEvolution()),
    Optimizer("StronglyCorrelated NGP", new StronglyCorrelated(50, _, _).runEvolution()),
    Optimizer("AnyData Random", new AnyData(50, _, _).runRandom()),
    Optimizer("SubsetSum Random", new SubsetSum(50, _, _).runRandom()),
    Optimizer("StronglyCorrelated Random", new StronglyCorrelated(50, _, _).runRandom())
  )

  val runs = for ((avg, limit) <- limits; solver <- solvers; optimizer <- optimizers) yield {
    import limit._
    val resFile = new File(D,
      s"N=$maxN-W=$maxWeight-V=$maxValue-IT=$evaluationLimit/${solver.getName} in ${optimizer.name}"
    )
    resFile.getParentFile.mkdirs()
    IndexedSeq.fill(avg)((limit, solver, optimizer, resFile))
  }
  val results = scala.util.Random.shuffle(runs.flatten).par.map(run => (run._3(run._1, run._2), run._4)).seq
  results.foreach(res => dumpResult(res._1, res._2))

  private def dumpResult(result: Result, file: File) {
    val out = new PrintWriter(new FileWriter(file, true))
    val fitness = result.fitness
    val items = result.genotype

    out.println("===================================")
    out.println("Result: ")
    out.println("   " + fitness + " ops")
    out.println("Test data:")
    out.println("N = " + items.size + " W = " + items.map(_.weight).sum / 2)
    for (item <- items) {
      out.println("w = " + item.weight + " v = " + item.value)
    }
    out.close()
  }
}
