package org.itmo.iyakupov.nds.gen;

/**
 * An especially hard test case for ENLU.
 * @author Maxim Buzdalov
 */
public class Point2DParPerGenerator implements ITestDataGen<int[][]> {

	@Override
	public int[][] generate(int dim, int max) {
		int[][] res = new int[max][2];
		int q = max / 3;
		int addL = -3;
		for (int i = 0; i < q; ++i) {
		    res[i][0] = i + 1 + addL;
		    res[i][1] = q - i + 1 + addL;
		}
		for (int i = 0; i < q; ++i) {
		    res[q + i][0] = i + addL;
		    res[q + i][1] = q - i + addL;
		}
		for (int i = 2 * q; i < max; ++i) {
		    if (i % 2 == 0) {
		        res[i][0] = -((i - 2 * q) / 2) - 1;
		        res[i][1] = -((i - 2 * q) / 2) - 6;
		    } else {
		        res[i][0] = -((i - 2 * q) / 2) - 6;
		        res[i][1] = -((i - 2 * q) / 2) - 1;
		    }
		}
		return res;
	}
}
