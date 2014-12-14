package ru.ifmo.ctd.ngp.demo.testgen.flows.experiments.generators

import ru.ifmo.ctd.ngp.demo.testgen.flows.EdgeRec
import ru.ifmo.ctd.ngp.demo.testgen.flows.experiments.generators.TestGenerator.Result
import ru.ifmo.ctd.ngp.util.FastRandom

/**
 * A random generator that does not allow duplicate edges.
 */
object RandomNoDup extends TestGenerator("random-no-dup") {
  override def generate(vertices: Int, edges: Int, maxCapacity: Int): Result = {
    val edges0 = math.min(edges, vertices * (vertices - 1))
    val rng = FastRandom.threadLocal()
    val hash = scala.collection.mutable.HashSet[(Int, Int)]()
    def newEdge(): EdgeRec = {
      val src, trg = rng.nextInt(vertices)
      if (src != trg && !hash.contains(src -> trg)) {
        hash += src -> trg
        new EdgeRec(src, trg, rng.nextInt(maxCapacity) + 1)
      } else newEdge()
    }
    Result(IndexedSeq.fill(edges0)(newEdge()), 0, vertices - 1)
  }
}
