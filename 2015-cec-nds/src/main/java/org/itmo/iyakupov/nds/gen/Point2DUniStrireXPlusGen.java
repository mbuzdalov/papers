package org.itmo.iyakupov.nds.gen;

import java.util.Random;

/**
 * Generator of points sampled from a 10 pixel stripe.
 *
 * Note (Maxim Buzdalov): due to space constraints, not included in the paper.
 *
 * @author Ilya Yakupov
 */
public class Point2DUniStrireXPlusGen implements ITestDataGen<int[][]> {
    private final Random random = new Random();

	protected int radius;
	public Point2DUniStrireXPlusGen(int radius) {
		this.radius = radius;
	}

	@Override
	public int[][] generate(int dim, int max) {
		int[][] res = new int[dim][2];
		for (int i = 0; i < dim; ++i) {
			res[i][0] = random.nextInt(max);
			res[i][1] = random.nextInt(max);
			while (res[i][1] < res[i][0] - radius || res[i][1] > res[i][0] + radius) {
				res[i][1] = random.nextInt(max);
			}
		}
		return res;
	}

}
