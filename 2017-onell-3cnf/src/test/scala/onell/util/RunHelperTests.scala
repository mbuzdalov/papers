package onell.util

import org.scalatest.{FlatSpec, Matchers}

class RunHelperTests extends FlatSpec with Matchers {
  private[this] val order: Ordering[String] = RunHelpers.numberTokenSorting

  "numberTokenSorting" should "sort non-number tokens as expected" in {
    order.compare("abc", "def") should (be < 0)
    order.compare("abcde", "f") should (be < 0)
    order.compare("test", "test") shouldBe 0
    order.compare("abc", "ab") should (be > 0)
  }

  it should "sort positive numbers as expected" in {
    order.compare("0", "1") should (be < 0)
    order.compare("26", "026") shouldBe 0
    order.compare("10", "9") should (be > 0)
  }

  it should "sort mixed values as expected" in {
    order.compare("test22", "test42") should (be < 0)
    order.compare("test99", "test100") should (be < 0)
    order.compare("test100b", "test100a") should (be > 0)
  }
}
