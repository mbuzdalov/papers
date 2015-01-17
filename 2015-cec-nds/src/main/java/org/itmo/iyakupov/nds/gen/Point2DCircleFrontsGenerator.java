package org.itmo.iyakupov.nds.gen;

import java.util.Random;

/**
 * A generator of circle-like fronts.
 *
 * Note (Maxim Buzdalov): it doesn't do what it is expected to do.
 * Currently it is unused.
 *
 * @author Ilya Yakupov
 */
public class Point2DCircleFrontsGenerator implements ITestDataGen<int[][]> {
	final int nLevels = 10;

    private final Random random = new Random();

	@Override
	public int[][] generate(int dim, int max) {
		int step = max / nLevels;
		int levelSize = dim / nLevels;
		int[][] res = new int[levelSize * nLevels][2];
		for (int i = 0; i < nLevels; ++i) {
			int r = i * step;
			for (int j = 0; j < levelSize; ++j) {
				res[i * levelSize + j][0] = random.nextInt(max);
				res[i * levelSize + j][1] = (int) Math.sqrt(Math.pow(res[i * levelSize + j][0], 2) - Math.pow(r, 2));
			}
		}
		return res;
	}
}
