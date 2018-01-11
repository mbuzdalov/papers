package ru.ifmo.cma

class ApacheCMATest extends ConstrainedCMATestBase {
  override def name: String = "Apache CMA"
  override def cma: CMALike = ApacheCMA
}
