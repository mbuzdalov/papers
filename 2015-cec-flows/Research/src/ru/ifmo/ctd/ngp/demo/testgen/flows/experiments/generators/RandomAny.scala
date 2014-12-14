package ru.ifmo.ctd.ngp.demo.testgen.flows.experiments.generators

import ru.ifmo.ctd.ngp.demo.testgen.flows.EdgeRec
import ru.ifmo.ctd.ngp.demo.testgen.flows.experiments.generators.TestGenerator.Result
import ru.ifmo.ctd.ngp.util.FastRandom

/**
 * A fully random generator.
 */
object RandomAny extends TestGenerator("random-any") {
  override def generate(vertices: Int, edges: Int, maxCapacity: Int): Result = {
    val rng = FastRandom.threadLocal()
    def newEdge(): EdgeRec = {
      val src, trg = rng.nextInt(vertices)
      if (src != trg) new EdgeRec(src, trg, rng.nextInt(maxCapacity) + 1) else newEdge()
    }
    Result(IndexedSeq.fill(edges)(newEdge()), 0, vertices - 1)
  }
}
