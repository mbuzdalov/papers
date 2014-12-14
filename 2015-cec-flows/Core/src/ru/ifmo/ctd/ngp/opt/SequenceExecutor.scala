package ru.ifmo.ctd.ngp.opt

import scala.collection.parallel.ParSeq

abstract class SequenceExecutor {
  def map[S, T](source: IndexedSeq[S], mapper: S => T) = mapN(source, mapper, 1)
  def mapN[S, T](source: IndexedSeq[S], mapper: S => T, times: Int): IndexedSeq[T]
}

object SequenceExecutor {
  val sequential = new SequenceExecutor {
    override def map[S, T](source: IndexedSeq[S], mapper: S => T) = {
      source.map(mapper)
    }
    override def mapN[S, T](source: IndexedSeq[S], mapper: S => T, times: Int) = {
      source.flatMap(v => IndexedSeq.fill(times)(mapper(v)))
    }
  }
  val scalaParallel = new SequenceExecutor {
    override def map[S, T](source: IndexedSeq[S], mapper: S => T) = {
      source.par.map(mapper).toIndexedSeq
    }
    override def mapN[S, T](source: IndexedSeq[S], mapper: S => T, times: Int) = {
      source.par.flatMap(v => ParSeq.fill(times)(mapper(v))).toIndexedSeq
    }
  }
}
