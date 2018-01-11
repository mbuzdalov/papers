package ru.ifmo.cma

class CMAWithResamplingTest extends ConstrainedCMATestBase {
  override def cma: CMALike = CMAWithResampling
}
