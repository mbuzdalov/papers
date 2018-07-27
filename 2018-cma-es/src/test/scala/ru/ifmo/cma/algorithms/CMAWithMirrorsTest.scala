package ru.ifmo.cma.algorithms

import ru.ifmo.cma.CMALike

class CMAWithMirrorsTest extends ConstrainedCMATestBase {
  override def cma: CMALike = CMAWithMirrors
}
