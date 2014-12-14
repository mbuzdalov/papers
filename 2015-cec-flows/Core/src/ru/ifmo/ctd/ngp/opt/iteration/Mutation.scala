package ru.ifmo.ctd.ngp.opt.iteration

import scala.collection.SeqLike
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

import ru.ifmo.ctd.ngp.opt.{RandomSource, SequenceExecutor, Evaluated}
import ru.ifmo.ctd.ngp.opt.types.{CodomainType, DomainType}

/**
 * A base class for mutation operators.
 *
 * @author Maxim Buzdalov
 */
abstract class Mutation[D: DomainType, C: CodomainType] {
  /**
   * Mutates the given evaluated objects in any way to construct some new values from the domain.
   * It is not necessary to return the same number of values as in the argument. For example,
   * the evolution strategies often do exactly the opposite.
   * @param evaluated the evaluated objects to mutate.
   * @return the new values from the domain.
   */
  def apply(evaluated: IndexedSeq[Evaluated[D, C]]): IndexedSeq[D]
}

object Mutation {
  object Standard {
    object Seq {
      def singlePointMutation[T, Repr <: SeqLike[T, Repr]](elementMutation: T => T)(
        implicit domain: DomainType[Repr],
                 cbf: CanBuildFrom[Repr, T, Repr],
                 random: RandomSource
      ): Repr => Repr = {
        v => {
          val idx = random().nextInt(v.size)
          v.updated(idx, elementMutation(v(idx)))
        }
      }
      def independentPointMutation[T, Repr <: SeqLike[T, Repr]](elementMutation: T => T, probability: Double)(
        implicit domain: DomainType[Repr],
                 cbf: CanBuildFrom[Repr, T, Repr],
                 random: RandomSource
      ): Repr => Repr = {
        val rng = random()
        _.map(i => if (rng.nextDouble() < probability) elementMutation(i) else i)
      }
      def singlePointCrossover[T, R[Z] <: SeqLike[Z, R[Z]]]()(
        implicit domain: DomainType[R[T]],
                 cbf: CanBuildFrom[R[T], T, R[T]],
                 random: RandomSource
      ): (R[T], R[T]) => (R[T], R[T]) = {
        (a, b) => {
          if (a.size >= 2 && b.size >= 2) {
            val idx = random().nextInt(math.min(a.size, b.size) - 1) + 1
            val (a1, a2) = a.splitAt(idx)
            val (b1, b2) = b.splitAt(idx)
            (a1 ++ b2, b1 ++ a2)
          } else {
            (a, b)
          }
        }
      }
      def twoPointCrossover[T, R[Z] <: SeqLike[Z, R[Z]]]()(
        implicit domain: DomainType[R[T]],
                 cbf: CanBuildFrom[R[T], T, R[T]],
                 random: RandomSource
      ): (R[T], R[T]) => (R[T], R[T]) = {
        (a, b) => {
          if (a.size >= 2 && b.size >= 2) {
            val idx1, idx2 = random().nextInt(math.min(a.size, b.size) - 1) + 1
            val (idxL, idxR) = if (idx1 < idx2) (idx1, idx2) else (idx2, idx1)
            val (a1, aX) = a.splitAt(idxL)
            val (b1, bX) = b.splitAt(idxL)
            val (a2, a3) = aX.splitAt(idxR - idxL)
            val (b2, b3) = bX.splitAt(idxR - idxL)
            (a1 ++ b2 ++ a3, b1 ++ a2 ++ b3)
          } else {
            (a, b)
          }
        }
      }
      def twoPointCrossoverWithShift[T, R[Z] <: SeqLike[Z, R[Z]]]()(
        implicit domain: DomainType[R[T]],
                 cbf: CanBuildFrom[R[T], T, R[T]],
                 random: RandomSource
      ): (R[T], R[T]) => (R[T], R[T]) = {
        (a, b) => {
          if (a.size >= 2 && b.size >= 2) {
            val len = random().nextInt(math.min(a.size, b.size) - 1) + 1
            val (a1, aX) = a.splitAt(random().nextInt(a.size - len))
            val (b1, bX) = b.splitAt(random().nextInt(b.size - len))
            val (a2, a3) = aX.splitAt(len)
            val (b2, b3) = bX.splitAt(len)
            (a1 ++ b2 ++ a3, b1 ++ a2 ++ b3)
          } else {
            (a, b)
          }
        }
      }
    }
  }

  class Detected[D: DomainType, C: CodomainType] {
    def usingEvaluatedToDomains(function: IndexedSeq[Evaluated[D, C]] => IndexedSeq[D]) = {
      new Mutation[D, C] {
        def apply(evaluated: IndexedSeq[Evaluated[D, C]]) = function(evaluated)
      }
    }
    def usingDomainsToDomains(function: IndexedSeq[D] => IndexedSeq[D]) = {
      new Mutation[D, C] {
        def apply(evaluated: IndexedSeq[Evaluated[D, C]]) = function(evaluated.map(_.input))
      }
    }
    def using(function: D => D)(implicit seq: SequenceExecutor) = {
      new Mutation[D, C] {
        def apply(evaluated: IndexedSeq[Evaluated[D, C]]) = seq.map[Evaluated[D, C], D](evaluated, t => function(t.input))
      }
    }
    def using(function: D => D, times: => Int)(implicit seq: SequenceExecutor) = {
      new Mutation[D, C] {
        def apply(evaluated: IndexedSeq[Evaluated[D, C]]) = seq.mapN[Evaluated[D, C], D](evaluated, t => function(t.input), times)
      }
    }
    def usingCrossoverTwoAndMutation(
       crossover: (D, D) => (D, D), crossoverProbability: Double,
       mutation: D => D, mutationProbability: Double
    )(implicit seq: SequenceExecutor, random: RandomSource) = {
      new Mutation[D, C] {
        def apply(evaluated: IndexedSeq[Evaluated[D, C]]) = {
          def mutate(a: D) = if (random().nextDouble() < mutationProbability) mutation(a) else a
          val pairedBuilder = IndexedSeq.newBuilder[(D, D)]
          for (i <- 0 until evaluated.size if (i & 1) == 1) {
            pairedBuilder += ((evaluated(i - 1).input, evaluated(i).input))
          }
          val resultPairs = seq.map[(D, D), (D, D)](
            pairedBuilder.result(),
            v => {
              val (z1, z2) = if (random().nextDouble() < crossoverProbability) {
                crossover(v._1, v._2)
              } else {
                v
              }
              (mutate(z1), mutate(z2))
            }
          )
          val resultBuilder = IndexedSeq.newBuilder[D]
          for ((a, b) <- resultPairs) {
            resultBuilder += a
            resultBuilder += b
          }
          if (evaluated.size % 2 == 1) {
            resultBuilder += evaluated.last.input
          }
          resultBuilder.result()
        }
      }
    }
    def usingCrossoverOneAndMutation(
       crossover: (D, D) => D, crossoverProbability: Double,
       mutation: D => D, mutationProbability: Double
    )(implicit seq: SequenceExecutor, random: RandomSource) = {
      new Mutation[D, C] {
        def apply(evaluated: IndexedSeq[Evaluated[D, C]]) = {
          def mutate(a: D) = if (random().nextDouble() < mutationProbability) mutation(a) else a
          val pairedBuilder = IndexedSeq.newBuilder[(D, D)]
          for (i <- 0 until evaluated.size if (i & 1) == 1) {
            pairedBuilder += ((evaluated(i - 1).input, evaluated(i).input))
          }
          val results = seq.map[(D, D), D](
            pairedBuilder.result(),
            v => mutate {
              if (random().nextDouble() < crossoverProbability) {
                crossover(v._1, v._2)
              } else {
                v._1
              }
            }
          )
          if (evaluated.size % 2 == 1) {
            results :+ evaluated.last.input
          } else {
            results
          }
        }
      }
    }
  }

  /**
   * Detects the domain, codomain, `Evaluated` object type and the working set type, and allows to select more options.
   * @tparam D the domain type.
   * @tparam C the codomain type.
   * @return the object for more options to build an `Iteration` object.
   */
  def apply[D: DomainType, C: CodomainType]() = new Detected
}
