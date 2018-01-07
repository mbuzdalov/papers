package ru.ifmo.cma

class CMAWithResamplingTest extends ConstrainedCMATestBase {
  override def name: String = "CMA with Resampling"
  override def newCMA(problem: Problem): CMA = new CMAWithResampling(problem)
}
