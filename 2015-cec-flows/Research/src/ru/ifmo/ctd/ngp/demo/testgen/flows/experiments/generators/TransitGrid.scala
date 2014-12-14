package ru.ifmo.ctd.ngp.demo.testgen.flows.experiments.generators

import ru.ifmo.ctd.ngp.demo.testgen.flows.EdgeRec
import ru.ifmo.ctd.ngp.demo.testgen.flows.experiments.generators.TestGenerator.Result
import ru.ifmo.ctd.ngp.util.FastRandom

/**
 * An implementation of the transit grid generator.
 */
object TransitGrid extends TestGenerator("tg") {
  override def generate(vertices: Int, edges: Int, maxCapacity: Int): Result = {
    val rng = FastRandom.threadLocal()
    val internal = vertices - 2
    val a = rng.nextInt(internal) + 1
    val b = internal / a

    def anEdge(from: Int, to: Int) = new EdgeRec(from, to, rng.nextInt(maxCapacity) + 1)

    val source = 0
    val target = a * b + 1

    val srcEdges = for (idx <- source + 1 to source + a; d <- 0 to 1) yield {
      if (d == 0) anEdge(source, idx) else anEdge(idx, source)
    }
    val trgEdges = for (idx <- target - 1 to target - a; d <- 0 to 1) yield {
      if (d == 0) anEdge(target, idx) else anEdge(idx, target)
    }
    val etcEdgesH = for (x <- 0 until a; y <- 1 until b; d <- 0 to 1) yield {
      if (d == 0) anEdge(1 + b * x + y - 1, 1 + b * x + y) else anEdge(1 + b * x + y, 1 + b * x + y - 1)
    }
    val etcEdgesV = for (x <- 1 until a; y <- 0 until b; d <- 0 to 1) yield {
      if (d == 0) anEdge(1 + b * (x - 1) + y, 1 + b * x + y) else anEdge(1 + b * x + y, 1 + b * (x - 1) + y)
    }
    Result(srcEdges ++ trgEdges ++ etcEdgesH ++ etcEdgesV, source, target)
  }
}
