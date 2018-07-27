package ru.ifmo.cma.algorithms

import ru.ifmo.cma.CMALike

class CMAWithResamplingTest extends ConstrainedCMATestBase {
  override def cma: CMALike = CMAWithResampling
}
