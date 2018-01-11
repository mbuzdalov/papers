package ru.ifmo.cma

class ApacheCMATest extends ConstrainedCMATestBase {
  override def cma: CMALike = ApacheCMA
}
