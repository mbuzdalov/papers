package ru.ifmo.ctd.ngp.demo.testgen.flows.experiments.generators

import scala.util.Random._

import ru.ifmo.ctd.ngp.demo.testgen.flows.EdgeRec
import ru.ifmo.ctd.ngp.demo.testgen.flows.experiments.generators.TestGenerator.Result
import ru.ifmo.ctd.ngp.util.FastRandom

/**
 * An implementation of the random frame generator (genrmf).
 */
object RandomFrames extends TestGenerator("genrmf") {
  override def generate(vertices: Int, edges: Int, maxCapacity: Int): Result = {
    val rng = FastRandom.threadLocal()

    def edgesFor(a: Int, b: Int) = 3 * a * a * b - a * (a + 2 * b)

    def genABC(): (Int, Int, Int) = {
      val b = rng.nextInt(math.min(vertices, edges)) + 1
      val aP = math.sqrt(vertices / b).toInt
      val a = if (edgesFor(aP, b) <= edges) aP else {
        def binary(l: Int, r: Int): Int = {
          if (l + 1 == r) l else {
            val m = (l + r) >>> 1
            if (edgesFor(m, b) <= vertices) binary(m, r) else binary(l, m)
          }
        }
        binary(0, aP)
      }
      if (a < 1) genABC() else {
        val c2 = maxCapacity / (a * a)
        if (c2 < 1) genABC() else (a, b, c2)
      }
    }

    val (a, b, c2) = genABC()
    val inLayerCap = a * a * c2

    val layerEdges = for {
      off <- 0 until (a * a * b) by (a * a)
      fc <- 0 until a
      sc <- 0 until (a - 1)
      way <- 0 to 3
    } yield way match {
      case 0 => new EdgeRec(off + fc + a * sc,        off + fc + a * (sc + 1), inLayerCap)
      case 1 => new EdgeRec(off + fc + a * (sc + 1),  off + fc + a * sc,       inLayerCap)
      case 2 => new EdgeRec(off + a * fc + sc,        off + a * fc + sc + 1,   inLayerCap)
      case 3 => new EdgeRec(off + a * fc + sc + 1,    off + a * fc + sc,       inLayerCap)
    }

    val crossEdges = for {
      layer <- 1 until b
      perm = rng.shuffle((0 until a * a).toIndexedSeq)
      index <- 0 until a * a
    } yield new EdgeRec((layer - 1) * a * a + index, layer * a * a + perm(index), rng.nextInt(c2) + 1)

    Result(layerEdges ++ crossEdges, 0, a * a * b - 1)
  }
}
