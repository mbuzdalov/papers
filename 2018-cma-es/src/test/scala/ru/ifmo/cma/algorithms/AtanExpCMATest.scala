package ru.ifmo.cma.algorithms

import ru.ifmo.cma.CMALike

class AtanExpCMATest extends ConstrainedCMATestBase {
  override protected val eps: Double = 1e-7
  override def cma: CMALike = AtanExpCMA
}
