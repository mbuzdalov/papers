package org.itmo.iyakupov.nds.gen;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Point generator: randomly shuffled diagonal points.
 * @author Maxim Buzdalov
 */
public class Point2DDiagGeneratorRandom implements ITestDataGen<int[][]> {
    private final Random random = new Random();

	@Override
	public int[][] generate(int dim, int max) {
		int[][] res = new int[max][2];
		for (int i = max; i > 0; --i) {
			res[max - i][0] = i;
			res[max - i][1] = i;
		}
		Collections.shuffle(Arrays.asList(res), random);
		return res;
	}

}
