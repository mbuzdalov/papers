package opt.test

/**
 * A test suite for Jensen non-dominated sorting.
 *
 * @author Maxim Buzdalov
 */
class NonDominatedSortingJensenFortinBuzdalovTest extends NonDominatedSortingTestBase {
  def getSorter(criteria: Int) = NonDominatedSorter.createJensenFortin(criteria)
}
