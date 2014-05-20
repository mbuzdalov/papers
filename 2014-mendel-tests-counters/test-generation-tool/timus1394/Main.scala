package timus1394

import java.io._

import ru.ifmo.ctd.ngp.learning.reinforce.q.DelayedAgent
import ru.ifmo.ctd.ngp.learning.reinforce.{WaitingAgent, RAgent, RandomAgent}

import Config.Result

/**
 * A main class for Timus 1394 test generation.
 *
 * @author Maxim Buzdalov
 */
object Main extends App {
  object Agents {
    def sampleDelayed() = new DelayedAgent[Int, Int](50, 0.1, 5, 0.001)
    def random()  = new RandomAgent[Int, Int]
    def sampleR() = new RAgent[Int, Int](0.2, 0.3, 0.01)
    def waiting() = new WaitingAgent[Int, Int]
  }

  object Adapters {
    def J2208365P = new ClassAdapterForKravtsov("solutions/J2208365P.jar", "J2208365P")
    def J3589819P = new ClassAdapterForKravtsov("solutions/J3589819P.jar", "J3589819P")
    def J3600147P = new ClassAdapterForKravtsov("solutions/J3600147P.jar", "J3600147P")
  }

  class Statistics(val name: String, val results: List[Config.Result], genLim: Int) {
    object succeeded {
      val values = results.filter(_.successful).map(_.evaluations.toDouble)
      val count = values.size

      protected[Statistics] val sum = values.sum
      protected[Statistics] val sumSq = values.map(t => t * t).sum

      val mean = sum / values.size
      val median = if (count == 0) Double.NaN else {
        if (count % 2 == 1) values(count / 2) else (values((count - 1) / 2) + values(count / 2)) / 2.0
      }
      val deviation = math.sqrt(sumSq / values.size - mean * mean)
    }
    object total {
      import succeeded.{count => scount, sum => ssum, sumSq => ssumSq}
      val count = results.size
      val mean = (ssum + (count - scount) * genLim) / scount
      val deviation = {
        val exp2 = (ssumSq + (count - scount) * (genLim * genLim + 2 * genLim * mean)) / scount
        math.sqrt(exp2 - mean * mean)
      }
    }
    def write(root: File) {
      val dest = new File(root, name + ".txt")
      dest.getAbsoluteFile.getParentFile.mkdirs()
      val out = new PrintWriter(dest)
      out.println(s"Succeeded: ${succeeded.count} out of ${total.count}")
      if (succeeded.count > 0) {
        out.println("Succeeded:")
        out.println(s" Mean: ${succeeded.mean}. Dev: ${succeeded.deviation}. Median: ${succeeded.median}")
        out.println("Total:")
        out.println(s" Mean: ${total.mean}. Dev: ${total.deviation}.")
      }
      out.println("Values")
      succeeded.values.foreach(out.println)
      out.close()
    }
  }

  for (adapter <- Seq(
    () => Adapters.J2208365P,
    () => Adapters.J3600147P,
    () => Adapters.J3589819P
  )) {
    def config() = new Config(adapter(), 50, 5000, true)
    val configSample = config()
    def task(name: String, index: Int, fun: PrintWriter => Result) = {
      new File(name).mkdirs()
      val fn = new PrintWriter(new FileWriter(f"$name%s/$index%02d"), true)
      val res = fun(fn)
      fn.close()
      (name, res)
    }
    val (gs, gl, el, sl) = (200, 50000, 0.025, 1000)

    val aName = adapter().name()
    val taskSource = Seq(
      task(s"timus/$aName/random",        _: Int, config().runSingleEARL(_, gs, gl, el, Agents.random())),
      task(s"timus/$aName/delayed",       _: Int, config().runSingleEARL(_, gs, gl, el, Agents.sampleDelayed())),
      task(s"timus/$aName/waiting",       _: Int, config().runSingleEARL(_, gs, gl, el, Agents.waiting())),
      task(s"timus/$aName/alg1",          _: Int, config().runSingleAlg1(_, gs, gl, el)),
      task(s"timus/$aName/alg2",          _: Int, config().runSingleAlg2(_, gs, gl, gl + 1, el))
    )

    val tasks = (0 until 100).flatMap(i => taskSource.map(f => () => f(i)))
    val results = tasks.par.map(_()).seq.groupBy(_._1)
    for ((name, res) <- results) {
      val fn = new PrintWriter(f"$name%s/all")
      for (z <- res.sortBy(_._2.evaluations)) {
        fn.println(z._2.evaluations)
      }
      fn.close()
    }
  }
}
