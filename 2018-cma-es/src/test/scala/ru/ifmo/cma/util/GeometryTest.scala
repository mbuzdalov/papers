package ru.ifmo.cma.util

import org.scalatest.{FlatSpec, Matchers}

class GeometryTest extends FlatSpec with Matchers {
  def checkOne(arg: Double, lower: Double, upper: Double, expected: Double): Unit = {
    val result = Geometry.mirror(arg, lower, upper)
    if (result < expected - 1e-9 || result > expected + 1e-9) {
      fail(s"mirror($arg, $lower, $upper) = $result did not equal $expected")
    }
  }

  def check(arg: Double, lower: Double, upper: Double, expected: Double): Unit = {
    checkOne(arg, lower, upper, expected)
    checkOne(arg + 26, lower + 26, upper + 26, expected + 26)
    checkOne(arg - math.Pi, lower - math.Pi, upper - math.Pi, expected - math.Pi)
  }

  "mirror" should "retain the argument when in bounds" in {
    check(0.0, 0.0, 1.0, 0.0)
    check(0.2, 0.0, 1.0, 0.2)
    check(0.5, 0.0, 1.0, 0.5)
    check(0.8, 0.0, 1.0, 0.8)
    check(1.0, 0.0, 1.0, 1.0)
  }

  it should "work correctly when argument is just above the upper bound" in {
    check(1.2, 0.0, 1.0, 0.8)
    check(1.5, 0.0, 1.0, 0.5)
    check(1.8, 0.0, 1.0, 0.2)
    check(2.0, 0.0, 1.0, 0.0)
  }

  it should "work correctly when argument is just below the lower bound" in {
    check(-0.2, 0.0, 1.0, 0.2)
    check(-0.5, 0.0, 1.0, 0.5)
    check(-0.8, 0.0, 1.0, 0.8)
    check(-1.0, 0.0, 1.0, 1.0)
  }

  it should "work correctly when argument is well above the upper bound" in {
    check(2.2, 0.0, 1.0, 0.2)
    check(2.5, 0.0, 1.0, 0.5)
    check(2.8, 0.0, 1.0, 0.8)
    check(3.0, 0.0, 1.0, 1.0)
    check(3.2, 0.0, 1.0, 0.8)
    check(3.5, 0.0, 1.0, 0.5)
    check(3.8, 0.0, 1.0, 0.2)
    check(4.0, 0.0, 1.0, 0.0)
    check(4.4, 0.0, 1.0, 0.4)
    check(5.2, 0.0, 1.0, 0.8)
  }

  it should "work correctly when argument is well below the lower bound" in {
    check(-1.2, 0.0, 1.0, 0.8)
    check(-1.5, 0.0, 1.0, 0.5)
    check(-1.8, 0.0, 1.0, 0.2)
    check(-2.0, 0.0, 1.0, 0.0)
    check(-2.4, 0.0, 1.0, 0.4)
    check(-2.8, 0.0, 1.0, 0.8)
    check(-3.0, 0.0, 1.0, 1.0)
    check(-3.6, 0.0, 1.0, 0.4)
    check(-4.2, 0.0, 1.0, 0.2)
  }
}
