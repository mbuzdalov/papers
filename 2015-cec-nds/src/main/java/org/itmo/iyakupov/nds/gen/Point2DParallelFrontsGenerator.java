package org.itmo.iyakupov.nds.gen;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Generates two parallel fronts.
 *
 * @author Ilya Yakupov
 * @author Maxim Buzdalov
 */
public class Point2DParallelFrontsGenerator implements ITestDataGen<int[][]> {
	private final Random random = new Random();

	@Override
	public int[][] generate(int dim, int max) {
		int[][] res = new int[max][2];
		for (int i = 0; i < max; ++i) {
		    int sum = max * 2 + i % 2;
		    res[i][0] = random.nextInt(max);
		    res[i][1] = sum - res[i][0];
		}
		Collections.shuffle(Arrays.asList(res), random);
		return res;
	}
}
