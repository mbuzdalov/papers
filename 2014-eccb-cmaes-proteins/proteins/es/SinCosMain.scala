package proteins.es

import java.io.{File, FileWriter, PrintWriter}

import proteins.encodings.SinCosEncoding
import proteins.intersection.{TernaryShortestDistance, NaiveIntersector}
import proteins.{MotionHelper, Structure}

/**
 * A main for a sin-cos encoding method.
 *
 * @author Maxim Buzdalov
 */
object SinCosMain extends App {
  val epsilonStage1 = 1e-1
  val epsilonStage2 = 1e-3
  val epsilonNoIsect = 2e-3

  def workFor(src: Structure, trg: Structure,
              namePrefix: String, intermediate: Int,
              useIntersector: Boolean, beParallel: Boolean, evaluateAll: Boolean) {
    val file = new File(s"$namePrefix.log").getAbsoluteFile
    file.getParentFile.mkdirs()
    val logger = new PrintWriter(new FileWriter(file), true)

    def impl(mh: MotionHelper with SinCosEncoding with NaiveIntersector with TernaryShortestDistance, namePrefix: String) {
      val log2 = if (namePrefix + ".log" != file.getName) {
        new PrintWriter(new FileWriter(s"$namePrefix.log"), true)
      } else logger
      val theStart = mh.torsion2indexedSeq(mh.linearApproximation)
      val result = if (mh.matrixSize == 0) {
        theStart
      } else if (useIntersector) {
        val config1 = new Config(mh, namePrefix + "-stage1", 0, theStart, epsilonStage1, 1e5, true, beParallel, log2)
        val newStart = config1.run()
        val config2 = new Config(mh, namePrefix + "-stage2", 100000, newStart, epsilonStage2, 0, false, beParallel, log2)
        config2.run()
      } else {
        val config = new Config(mh, namePrefix, 30000, theStart, epsilonNoIsect, 0, false, beParallel, log2)
        config.run()
      }
      log2.println(s"Final result full fitness: ${mh.evaluate(result, useIntersector)}")
      log2.close()
      val resultWriter = new PrintWriter(namePrefix + "-result.pdb")
      resultWriter.println(mh.buildPDB(mh.indexedSeq2torsion(result)))
      resultWriter.close()
    }

    val initial = new MotionHelper(src, trg, intermediate, Set()) with SinCosEncoding with NaiveIntersector with TernaryShortestDistance
    val differing = (0 until src.torsion.size).filter{
      i => (src.torsion(i) - trg.torsion(i)).angle.abs > 1.5
    }.toSet
    logger.println(s"# Angles differing by more than 2.0: $differing")
    if (differing.isEmpty || differing.size > 10) {
      logger.println(s"# Initial approx cost: ${initial.evaluateInitialApproximation(useIntersector)}")
      impl(initial, namePrefix)
    } else {
      logger.println(s"# Attempting to find a best approximation")
      val orientations = differing.subsets.toIndexedSeq map { s =>
        val currH = new MotionHelper(src, trg, intermediate, s) with SinCosEncoding with NaiveIntersector with TernaryShortestDistance
        val cost = currH.evaluateInitialApproximation(useIntersector)
        logger.println(s"# Cost for $s: $cost")
        (currH, s, cost)
      }
      if (evaluateAll) {
        val tasks = for (o <- 0 until orientations.size; e <- 0 until 8) yield (o, e)
        tasks.par.foreach { case (o, r) =>
          impl(orientations(o)._1, namePrefix + o + "-" + r)
        }
      } else {
        val minimum = orientations minBy {
          case (_, _, (weight, diff, isect)) => weight + diff
        }
        logger.println(s"# Selecting ${minimum._2} with cost ${minimum._3}")
        impl(minimum._1, namePrefix)
      }
    }
  }

  def usage(): Nothing = {
    println(
      """
        |Usage: SinCosMain check <PDB-file>
        |   or: SinCosMain pair <PDB-model-1> <PDB-model-2> <name-prefix> <intermediate-confs> <intersections>
        |   or: SinCosMain all  <PDB-file> <name-prefix> <intermediate-confs> <intersections>
        |The meanings for the modes are:
        |       check: the file is treated as a motion file, statistics are collected.
        |       pair:  the transformation between two given models is optimized.
        |       all:   the transformations between all pairs of models from the given file
        |              are optimized.
        |The meanings for the options are:
        |       <PDB-file> is a path a a PDB file, possibly with many models.
        |       <PDB-model-*> is either a path to a PDB file
        |           or a <file/model> where model is the model number in the file
        |           starting with 0. If the file is given and there are multiple
        |           models within it, the first one is picked.
        |       <name-prefix> is a prefix for names of files that will be generated.
        |           In the case of 'all', it is treated as a Java format string
        |           for two integers, the zero-based number of model.
        |       <intermediate-confs> is the number of intermediate conformations.
        |       <intersections> is yes or no,
        |           depending on whether you want to use an intersection detection
        |           algorithm or not.
      """.stripMargin)
    sys.exit(1)
  }

  if (args.length > 0) {
    args(0) match {
      case "check" =>
        if (args.length != 2) {
          usage()
        } else {
          val allPDB = Common.readAll(args(1))
          val mh = new MotionHelper(allPDB.head, allPDB.last, 0, Set())
                   with SinCosEncoding with NaiveIntersector with TernaryShortestDistance
          val threshold = math.min(mh.src.lengths.min, mh.trg.lengths.min)
          println(s"Models in the input file: ${allPDB.size}")
          val stats = (1 until allPDB.size) map { i =>
            val align = mh.align(allPDB(i - 1).atoms.map(_.location), allPDB(i).atoms.map(_.location))
            val isect = mh.computeIntersectionPenalty(align.firstChain, align.secondChain, threshold)
            val wrmsd = align.weighedRMSD
            (i, wrmsd, wrmsd * wrmsd * mh.src.atoms.size, isect)
          }
          for (t <- stats) {
            println(s"  ${t._1 - 1} -> ${t._1}: wRMSD = ${t._2}, cost = ${t._3}, intersections = ${t._4}")
          }
          println(s"  Total cost: ${stats.map(_._3).sum}")
          println(s"  Total intersections: ${stats.map(_._4).sum}")
        }
      case "all" =>
        if (args.length != 5 || !(args(4) == "yes" || args(4) == "no")) {
          usage()
        } else {
          val allPDB = Common.readAll(args(1))
          val namePrefix = args(2)
          val useIntersections = args(4) == "yes"
          val tasksInit = (0 until allPDB.size).flatMap(src => (src + 1 until allPDB.size).map(trg => (src, trg)))
          val tasks = tasksInit.filter { case (src, trg) =>
            val logFile = namePrefix.format(src, trg) + ".log"
            if (new File(logFile).exists()) {
              val src = scala.io.Source.fromFile(logFile)
              val last = src.getLines().fold(null)((a, b) => b)
              !last.startsWith("Final")
            } else true
          }
          tasks.par.foreach { case (srcI, trgI) =>
            workFor(allPDB(srcI), allPDB(trgI), namePrefix.format(srcI, trgI), args(3).toInt,
              useIntersections, beParallel = false, evaluateAll = false)
          }
        }
      case "pair" =>
        if (args.length != 6 || !(args(5) == "yes" || args(5) == "no")) {
          usage()
        } else {
          val pdb1 = Common.readOne(args(1))
          val pdb2 = Common.readOne(args(2))
          val namePrefix = args(3)
          val useIntersections = args(5) == "yes"
          workFor(pdb1, pdb2, namePrefix, args(4).toInt, useIntersections, beParallel = true, evaluateAll = false)
        }
      case "multi" =>
        if (args.length != 6 || !(args(5) == "yes" || args(5) == "no")) {
          usage()
        } else {
          val pdb1 = Common.readOne(args(1))
          val pdb2 = Common.readOne(args(2))
          val namePrefix = args(3)
          val useIntersections = args(5) == "yes"
          workFor(pdb1, pdb2, namePrefix, args(4).toInt, useIntersections, beParallel = true, evaluateAll = true)
        }
      case _ => usage()
    }
  } else {
    usage()
  }
}
