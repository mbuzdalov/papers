package ru.ifmo.cma.algorithms

import ru.ifmo.cma.CMALike

class ApacheCMATest extends ConstrainedCMATestBase {
  override def cma: CMALike = ApacheCMA
}
