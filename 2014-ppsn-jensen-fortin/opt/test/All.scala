package opt.test

object All extends App {
  new NonDominatedSortingDebTest().runTests()
  new NonDominatedSortingJensenFortinTest().runTests()
  new NonDominatedSortingJensenFortinBuzdalovTest().runTests()
}
