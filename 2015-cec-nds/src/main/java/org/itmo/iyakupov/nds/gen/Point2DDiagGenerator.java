package org.itmo.iyakupov.nds.gen;

/**
 * A "diag1" generator from the paper. The best case for ENLU.
 *
 * @author Ilya Yakupov
 */
public class Point2DDiagGenerator implements ITestDataGen<int[][]> {
	@Override
	public int[][] generate(int dim, int max) {
		int[][] res = new int[max][2];
		for (int i = max; i > 0; --i) {
			res[max - i][0] = i;
			res[max - i][1] = i;
		}
		return res;
	}
}
