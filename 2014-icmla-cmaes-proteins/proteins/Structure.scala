package proteins

import java.io.InputStream
import scala.io.Source

class Structure(val atoms: IndexedSeq[Atom]) {
  private val diff = Array.tabulate(atoms.length - 1)(i => atoms(i + 1).location - atoms(i).location)
  val lengths  = diff.map(_.length)
  private val crossDiff = Array.tabulate(diff.length - 1)(i => diff(i) ^ diff(i + 1))
  val planar = IndexedSeq.tabulate(diff.length - 1) { i =>
    val cos = (diff(i) * diff(i + 1)) / (lengths(i) * lengths(i + 1))
    val sin = math.sqrt(1 - cos * cos)
    SinCos(sin, cos)
  }
  val torsion = IndexedSeq.tabulate(crossDiff.length - 1) { i =>
    val xSin = diff(i) * crossDiff(i + 1) * lengths(i + 1)
    val xCos = crossDiff(i) * crossDiff(i + 1)
    SinCos(xSin, xCos)
  }
  def toPDBString = {
    val atomString = atoms.zipWithIndex.map { case (a, i) =>
      val pt = a.location
      val res = f"ATOM $i%6d ${a.kind}%2s ${a.aminoAcid}%5s A ${i / 3 + 1}%3d     " +
                f"${pt.x}%7.3f ${pt.y}%7.3f ${pt.z}%7.3f  1.00  0.00          ${a.kind.charAt(0)}%2c"
      //Scala formatting string uses the default locale. I wonder how to mix String.format(Locale, ...) and this one.
      res.replace(',', '.')
    }
    atomString.mkString("", "\n", "\nTER\nENDMDL\n")
  }
}

object Structure {
  trait Weigher {
    def weight(atomName: String, residueName: String, isFirst: Boolean, isLast: Boolean): Double
  }
  val AtomWeight = new Weigher {
    def weight(atomName: String, residueName: String, isFirst: Boolean, isLast: Boolean) = atomName match {
      case "C"|"CA" => 12.0107
      case "N"      => 14.00674
      case _        => throw new UnsupportedOperationException("Not implemented yet")
    }
  }
  val ResidueWeight = new Weigher {
    val map = Map(
      "ala" -> 71.0779,  "arg" -> 156.1857, "asn" -> 114.1026, "asp" -> 115.0874, "cys" -> 103.1429,
      "glu" -> 129.114,  "gln" -> 128.1292, "gly" -> 57.0513,  "his" -> 137.1393, "ile" -> 113.1576,
      "leu" -> 113.1576, "lys" -> 128.1723, "met" -> 131.1961, "phe" -> 147.1739, "pro" -> 97.1152,
      "ser" -> 87.0773,  "thr" -> 101.1039, "sec" -> 150.0379, "trp" -> 186.2099, "tyr" -> 163.1733,
      "val" -> 99.1311
    )
    def weight(atomName: String, residueName: String, isFirst: Boolean, isLast: Boolean) = atomName match {
      //see http://spin.niddk.nih.gov/bax/software/TALOSORIG/backbone.gif
      case "C"  => 12.0107 +  15.9994 + (if (isLast) 14.00674 + 1.00794 else 0.0)
      case "N"  => 14.00674 + 1.00794 + (if (isFirst) 3 * 1.00794 else 0.0)
      case "CA" => 2 * 12.0107 + 1.00794 + map(residueName.toLowerCase)
    }
  }

  def read(stream: InputStream, weigher: Weigher): IndexedSeq[Structure] = {
    val lines = Source.fromInputStream(stream).getLines().toIndexedSeq
    val resultBuilder = IndexedSeq.newBuilder[Structure]
    val structureBuilder = IndexedSeq.newBuilder[Atom]

    def flushStructure() {
      val res = structureBuilder.result()
      if (res.size > 0) {
        val patched = res.zipWithIndex.map {
          case (a, idx) =>
            a.copy(weight = weigher.weight(a.kind, a.aminoAcid, idx == 0, idx + 1 == res.size))
        }
        resultBuilder += new Structure(patched)
        structureBuilder.clear()
      }
    }

    for (i <- 0 until lines.size) {
      val str = lines(i)
      if (str.startsWith("MODEL")) {
        structureBuilder.clear()
      } else if (str.startsWith("ENDMDL")) {
        flushStructure()
      } else if (str.startsWith("ATOM")) {
        val components = str.split(" ").filter(!_.isEmpty)
        val atoms = components(2) match {
          case "N"|"CA"|"C" =>
            val x = components(6).toDouble
            val y = components(7).toDouble
            val z = components(8).toDouble
            Some(Atom(
              Point3(x, y, z),
              0.0,             //weights are set later, when we know which atom is the last one
              components(2),
              components(3)
            ))
          case _ => None
        }
        structureBuilder ++= atoms
      }
    }

    flushStructure()
    resultBuilder.result()
  }
}
