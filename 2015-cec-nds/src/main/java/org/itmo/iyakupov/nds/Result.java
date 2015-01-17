package org.itmo.iyakupov.nds;

import java.util.Arrays;

/**
 * @author Ilya Yakupov
 */
public class Result {
	public int rank;
	public int[] point;
	public Result(int rank, int[] point) {
		super();
		this.rank = rank;
		this.point = point;
	}

	public String toString() {
		return "Rank = " + rank + ", value = " + Arrays.toString(point);
	}
}
