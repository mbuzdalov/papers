package ru.ifmo.cma

class CMAWithRescalingToBoundaryTest extends ConstrainedCMATestBase {
  override def cma: CMALike = CMAWithRescalingToBoundary
}
