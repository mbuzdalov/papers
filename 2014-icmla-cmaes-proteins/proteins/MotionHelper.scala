package proteins

import scala.annotation.tailrec

import ru.ifmo.ctd.ngp.util._

/**
 * This object encapsulates two structures and useful utilities to
 * calculate the optimal transformation between them.
 *
 * @author Maxim Buzdalov
 */
class MotionHelper(val src: Structure, val trg: Structure, val intermediate: Int, reverseLinear: Set[Int]) {
  needs: MatrixEncoding with Intersector =>

  //Checking whether the structures encode the same protein
  require(src.atoms.size == trg.atoms.size, "Structures have unequal sizes")
  require(src.atoms.map(_.aminoAcid) == trg.atoms.map(_.aminoAcid), "Structures have different amino acids")

  private val sumWeights = src.atoms.toIterator.map(_.weight).sum
  private val srcSeq = src.atoms.map(_.location)
  private val trgSeq = trg.atoms.map(_.location)

  private val planars = IndexedSeq.tabulate(intermediate, src.planar.size)(linearApproximationP)

  def restoreCoords(torsion: Int => SinCos, planar: Int => SinCos): IndexedSeq[Point3] = {
    val atoms = src.atoms
    val lengths = src.lengths
    val coords = Array.ofDim[Point3](atoms.length)
    coords(0) = Point3(0, 0, 0)
    coords(1) = Point3(lengths(0), 0, 0)
    coords(2) = Point3(lengths(0) + lengths(1) * planar(0).cos, lengths(1) * planar(0).sin, 0)
    for (i <- 3 until atoms.length) {
      val r = lengths(i - 1)
      val p = planar(i - 2)
      val t = torsion(i - 3)
      coords(i) = Point3(r * p.cos, r * p.sin * t.cos, r * p.sin * t.sin)
    }
    for (i <- 3 until atoms.length) {
      val bc = (coords(i - 1) - coords(i - 2)).normalized
      val n = ((coords(i - 2) - coords(i - 3)) ^ bc).normalized
      val nbc = n ^ bc
      val ci = coords(i)
      val add = Point3(ci.x * bc.x + ci.y * nbc.x + ci.z * n.x,
                       ci.x * bc.y + ci.y * nbc.y + ci.z * n.y,
                       ci.x * bc.z + ci.y * nbc.z + ci.z * n.z)
      coords(i) = coords(i - 1) + add
    }
    coords
  }

  def linearApproximation(model: Int, atom: Int): SinCos = {
    val l = src.torsion(atom)
    val r = trg.torsion(atom)
    val angle0 = (r - l).angle
    val angle = if (reverseLinear contains atom) {
      if (angle0 > 0) angle0 - math.Pi * 2 else angle0 + math.Pi * 2
    } else angle0
    l + SinCos.fromAngle(angle * (1 + model) / (intermediate + 1))
  }

  def linearApproximationP(model: Int, atom: Int): SinCos = {
    val l = src.planar(atom)
    val r = trg.planar(atom)
    val angle = (r - l).angle
    l + SinCos.fromAngle(angle * (1 + model) / (intermediate + 1))
  }

  def align(reference: IndexedSeq[Point3], atoms: IndexedSeq[Point3]): AlignResult = {
    val aw = src.atoms
    def weighedAverage(points: IndexedSeq[Point3]) = {
      var sumX, sumY, sumZ = 0.0
      for (i <- 0 until points.size) {
        val w = aw(i).weight
        sumX += points(i).x * w
        sumY += points(i).y * w
        sumZ += points(i).z * w
      }
      Point3(sumX / sumWeights, sumY / sumWeights, sumZ / sumWeights)
    }
    val refAvg = weighedAverage(reference)
    val atmAvg = weighedAverage(atoms)
    val cRef = reference.map(_ - refAvg)
    val cAtm = atoms.map(_ - atmAvg)

    val e0 = (0 until cRef.size) mapSum { index =>
      aw(index).weight * (cRef(index).length2 + cAtm(index).length2) / 2
    }
    val (sxx, sxy, sxz, syx, syy, syz, szx, szy, szz) = {
      var axx, axy, axz, ayx, ayy, ayz, azx, azy, azz = 0.0
      for (index <- 0 until cRef.size) {
        val w = aw(index).weight
        val r = cRef(index)
        val a = cAtm(index)
        axx += w * r.x * a.x
        axy += w * r.x * a.y
        axz += w * r.x * a.z
        ayx += w * r.y * a.x
        ayy += w * r.y * a.y
        ayz += w * r.y * a.z
        azx += w * r.z * a.x
        azy += w * r.z * a.y
        azz += w * r.z * a.z
      }
      (axx, axy, axz, ayx, ayy, ayz, azx, azy, azz)
    }

    //Sorry for that below. It was Matlab before its birth.
    //Let us hope that it is quite optimized.
    val evecprec = 1e-6
    val evalprec = 1e-11
    val sxx2 = sxx * sxx
    val syy2 = syy * syy
    val szz2 = szz * szz

    val sxy2 = sxy * sxy
    val syz2 = syz * syz
    val sxz2 = sxz * sxz

    val syx2 = syx * syx
    val szy2 = szy * szy
    val szx2 = szx * szx

    val syzszymsyyszz2 = 2.0 * (syz * szy - syy * szz)
    val sxx2syy2szz2syz2szy2 = syy2 + szz2 - sxx2 + syz2 + szy2

    val c3 = -2.0 * (sxx2 + syy2 + szz2 + sxy2 + syx2 + sxz2 + szx2 + syz2 + szy2)
    val c2 = 8.0 * (sxx * syz * szy + syy * szx * sxz + szz * sxy * syx -
      sxx * syy * szz - syz * szx * sxy - szy * syx * sxz)

    val sxzpszx = sxz + szx
    val syzpszy = syz + szy
    val sxypsyx = sxy + syx
    val syzmszy = syz - szy
    val sxzmszx = sxz - szx
    val sxymsyx = sxy - syx
    val sxxpsyy = sxx + syy
    val sxxmsyy = sxx - syy
    val sxy2sxz2syx2szx2 = sxy2 + sxz2 - syx2 - szx2

    val c1 = sxy2sxz2syx2szx2 * sxy2sxz2syx2szx2 +
      (sxx2syy2szz2syz2szy2 + syzszymsyyszz2) * (sxx2syy2szz2syz2szy2 - syzszymsyyszz2) +
      (-sxzpszx * syzmszy + sxymsyx * (sxxmsyy - szz)) * (-sxzmszx * syzpszy + sxymsyx * (sxxmsyy + szz)) +
      (-sxzpszx * syzpszy - sxypsyx * (sxxpsyy - szz)) * (-sxzmszx * syzmszy - sxypsyx * (sxxpsyy + szz)) +
      (+sxypsyx * syzpszy + sxzpszx * (sxxmsyy + szz)) * (-sxymsyx * syzmszy + sxzpszx * (sxxpsyy + szz)) +
      (+sxypsyx * syzmszy + sxzmszx * (sxxmsyy - szz)) * (-sxymsyx * syzpszy + sxzmszx * (sxxpsyy - szz))

    @tailrec
    def eigenIteration(iterRemains: Int, mxEigenV: Double): (Double, Int) = {
      if (iterRemains == 0) {
        (mxEigenV, 0)
      } else {
        val oldg = mxEigenV
        val x2 = mxEigenV * mxEigenV
        val b = (x2 + c3) * mxEigenV
        val a = b + c2
        val delta = (a * mxEigenV + c1) / (2.0 * x2 * mxEigenV + b + a)
        val newMxEigenV = mxEigenV - delta
        if (math.abs(newMxEigenV - oldg) < math.abs(evalprec * newMxEigenV)) {
          (newMxEigenV, iterRemains - 1)
        } else {
          eigenIteration(iterRemains - 1, newMxEigenV)
        }
      }
    }

    val (mxEigenV, iterationsRemained) = eigenIteration(50, e0)
    if (iterationsRemained == 0) {
      throw new AssertionError("50 iterations is too small")
    }
    val rmsd = math.sqrt(math.abs(2.0 * (e0 - mxEigenV) / reference.size))
    //The two below are needed for the unimplemented code below.
    //val a11 = SxxpSyy + Szz - mxEigenV
    //val a12 = SyzmSzy
    val a13 = -sxzmszx
    val a14 = sxymsyx
    val a21 = syzmszy
    val a22 = sxxmsyy - szz - mxEigenV
    val a23 = sxypsyx
    val a24 = sxzpszx
    val a31 = a13
    val a32 = a23
    val a33 = syy - sxx - szz - mxEigenV
    val a34 = syzpszy
    val a41 = a14
    val a42 = a24
    val a43 = a34
    val a44 = szz - sxxpsyy - mxEigenV
    val a3344_4334 = a33 * a44 - a43 * a34
    val a3244_4234 = a32 * a44 - a42 * a34
    val a3243_4233 = a32 * a43 - a42 * a33
    val a3143_4133 = a31 * a43 - a41 * a33
    val a3144_4134 = a31 * a44 - a41 * a34
    val a3142_4132 = a31 * a42 - a41 * a32
    val q1 =  a22 * a3344_4334 - a23 * a3244_4234 + a24 * a3243_4233
    val q2 = -a21 * a3344_4334 + a23 * a3144_4134 - a24 * a3143_4133
    val q3 =  a21 * a3244_4234 - a22 * a3144_4134 + a24 * a3142_4132
    val q4 = -a21 * a3243_4233 + a22 * a3143_4133 - a23 * a3142_4132

    val qsqr = q1 * q1 + q2 * q2 + q3 * q3 + q4 * q4
    if (qsqr < evecprec) {
      throw new AssertionError("Implement more of this...")
    } else {
      val normq = math.sqrt(qsqr)
      val qq1 = q1 / normq
      val qq2 = q2 / normq
      val qq3 = q3 / normq
      val qq4 = q4 / normq

      val a2 = qq1 * qq1
      val x2 = qq2 * qq2
      val y2 = qq3 * qq3
      val z2 = qq4 * qq4

      val xy = qq2 * qq3
      val az = qq1 * qq4
      val zx = qq4 * qq2
      val ay = qq1 * qq3
      val yz = qq3 * qq4
      val ax = qq1 * qq2

      val rot00 = a2 + x2 - y2 - z2
      val rot01 = 2 * (xy + az)
      val rot02 = 2 * (zx - ay)
      val rot10 = 2 * (xy - az)
      val rot11 = a2 - x2 + y2 - z2
      val rot12 = 2 * (yz + ax)
      val rot20 = 2 * (zx + ay)
      val rot21 = 2 * (yz - ax)
      val rot22 = a2 - x2 - y2 + z2

      new AlignResult {
        val firstChain = reference
        lazy val secondChain = cAtm.map { p =>
          val Point3(x, y, z) = p
          refAvg + Point3(
            rot00 * x + rot01 * y + rot02 * z,
            rot10 * x + rot11 * y + rot12 * z,
            rot20 * x + rot21 * y + rot22 * z
          )
        }
        val weighedRMSD = rmsd
      }
    }
  }

  def alignAll(torsions: (Int, Int) => SinCos): IndexedSeq[AlignResult] = {
    val restored = (-1 to intermediate) map { i =>
      if (i == -1) srcSeq else if (i == intermediate) trgSeq else {
        restoreCoords(torsions(i, _), planars(i))
      }
    }
    restored.sliding(2).map(pair => align(pair(0), pair(1))).toIndexedSeq
  }

  def buildPDB(torsions: (Int, Int) => SinCos): String = {
    val restored = (-1 to intermediate) map { i =>
      if (i == -1) srcSeq else if (i == intermediate) trgSeq else {
        restoreCoords(torsions(i, _), planars(i))
      }
    }
    val aligned = restored.tail.foldLeft(List(restored.head))((d, c) => align(d.head, c).secondChain :: d).reverse
    val structures = aligned.map(ch => new Structure(ch.zipWithIndex map {case(p, i) => src.atoms(i).copy(location = p)}))
    structures.zipWithIndex.map {
      case (str, idx) => s"MODEL $idx\n${str.toPDBString}"
    } mkString("TITLE optimized by MaxBuzz\n", "", "END")
  }

  val distanceThreshold = math.min(src.lengths.min, trg.lengths.min)

  def evaluateInitialApproximation(useIntersector: Boolean) = evaluate(torsion2indexedSeq(linearApproximation), useIntersector)
  
  def evaluate(g: IndexedSeq[Double], useIntersector: Boolean) = {
    val alignments = alignAll(indexedSeq2torsion(g))
    val transitions = alignments.map { alignment =>
      val rmsd = alignment.weighedRMSD
      rmsd * rmsd * src.atoms.size
    }
    val weight = transitions.sum
    val differences = transitions.sliding(2).map(p => math.abs(p(0) - p(1))).sum
    val intersections = alignments.map { alignment =>
      if (useIntersector) {
        computeIntersectionPenalty(alignment.firstChain, alignment.secondChain, distanceThreshold)
      } else 0
    }
    (weight, differences, intersections.sum)
  }
}
