package ru.ifmo.cma

class CMATest extends UnconstrainedCMATestBase {
  override def name: String = "CMA"
  override def cma: CMALike = CMA
}
