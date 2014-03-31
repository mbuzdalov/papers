package opt.test

/**
 * A test suite for Deb non-dominated sorting.
 *
 * @author Maxim Buzdalov
 */
class NonDominatedSortingDebTest extends NonDominatedSortingTestBase {
  def getSorter(criteria: Int) = NonDominatedSorter.createDeb(criteria)
}
