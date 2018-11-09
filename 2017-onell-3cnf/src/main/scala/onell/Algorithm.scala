package onell

/**
  * A trait for algorithms solving pseudo-Boolean mutation-aware problems.
  */
trait Algorithm[@specialized(Specializable.BestOfBreed) F] {
  /**
    * Returns the legend to be displayed on plots.
    * @return the legend to be displayed on plots.
    */
  def pgfPlotLegend: String
  /**
    * Returns the name of the algorithm.
    * @return the name of the algorithm.
    */
  def name: String

  /**
    * Returns the revision string of the algorithm. If one changes this string, all experiments will be re-evaluated.
    * @return the revision string of the algorithm.
    */
  def revision: String

  /**
    * Returns the description of the performance metrics.
    * @return the description of the performance metrics.
    */
  def metrics: Seq[String]

  /**
    * Solves the given problem and returns the performance metrics (for meaning of these metrics,
    * see the return of `metrics`).
    * @param problem the problem to be solved.
    * @return the performance metrics.
    */
  def solve(problem: MutationAwarePseudoBooleanProblem.Instance[F]): Seq[Double]
}
