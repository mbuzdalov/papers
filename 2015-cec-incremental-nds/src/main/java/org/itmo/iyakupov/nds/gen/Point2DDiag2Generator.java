package org.itmo.iyakupov.nds.gen;

/**
 * Point generator: two diagonals at (x, x + 5) and (x + 5, x) in decreasing order.
 * A worst-case for our sorting.
 * @author Maxim Buzdalov
 */
public class Point2DDiag2Generator implements ITestDataGen<int[][]> {

	@Override
	public int[][] generate(int dim, int max) {
		int[][] res = new int[max][2];
		for (int i = max; i > 0; --i) {
		    if (i % 2 == 0) {
    			res[max - i][0] = i / 2;
	    		res[max - i][1] = i / 2 + 5;
		    } else {
    			res[max - i][0] = i / 2 + 5;
	    		res[max - i][1] = i / 2;
		    }
		}
		return res;
	}

}
