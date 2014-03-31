package knapsack;

/**
 * This is an interface for knapsack solvers.
 *
 * @author Maxim Buzdalov
 */
public interface KnapsackSolver {
    public KnapsackResultEx solve(ProblemInstance problem);
    public String getName();
}
