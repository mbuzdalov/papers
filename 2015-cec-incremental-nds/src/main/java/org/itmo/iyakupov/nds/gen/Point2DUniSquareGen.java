package org.itmo.iyakupov.nds.gen;

import java.util.Random;

/**
 * Generator of points randomly sampled from a square.
 * @author Ilya Yakupov
 */
public class Point2DUniSquareGen implements ITestDataGen<int[][]> {

    private final Random random = new Random();

	@Override
	public int[][] generate(int dim, int max) {
		int[][] res = new int[dim][2];
		for (int i = 0; i < dim; ++i) {
			res[i][0] = random.nextInt(max);
			res[i][1] = random.nextInt(max);
		}
		return res;
	}

}
