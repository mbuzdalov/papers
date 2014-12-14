package ru.ifmo.ctd.ngp.demo.testgen.flows.experiments.generators

import ru.ifmo.ctd.ngp.demo.testgen.flows.EdgeRec
import ru.ifmo.ctd.ngp.demo.testgen.flows.experiments.generators.TestGenerator.Result
import ru.ifmo.ctd.ngp.util.FastRandom

/**
 * A random generator that generates dense acyclic graphs without edge duplicates.
 */
object RandomAcyclic extends TestGenerator("ac") {
  override def generate(vertices: Int, edges: Int, maxCapacity: Int): Result = {
    val rng = FastRandom.threadLocal()
    val edges = for (src <- 0 until vertices; trg <- src + 1 until vertices) yield {
      new EdgeRec(src, trg, rng.nextInt(maxCapacity) + 1)
    }
    Result(edges, 0, vertices - 1)
  }
}
