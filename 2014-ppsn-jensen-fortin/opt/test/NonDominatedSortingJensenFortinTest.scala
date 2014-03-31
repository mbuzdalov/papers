package opt.test

/**
 * A test suite for Jensen non-dominated sorting.
 *
 * @author Maxim Buzdalov
 */
class NonDominatedSortingJensenFortinTest extends NonDominatedSortingTestBase {
  def getSorter(criteria: Int) = NonDominatedSorter.createJensenFortin(criteria)
}
