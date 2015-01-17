package org.itmo.iyakupov.nds;

import java.util.Arrays;

/**
 * An individual which is an integer 2D point.
 * @author Ilya Yakupov
 */
public class Int2DIndividual {
	private int x1, x2;
	public static long dominationComparsionCount;

	public Int2DIndividual(int x1, int x2) {
		this.x1 = x1;
		this.x2 = x2;
	}

	public Int2DIndividual(int[] x) {
		if (x.length != 2) {
			throw new RuntimeException("Can't cast to Int2DIndividual this array: " + Arrays.toString(x));
		}
		this.x1 = x[0];
		this.x2 = x[1];
	}

	public int getX1() {
		return x1;
	}

	public int getX2() {
		return x2;
	}

	public int compareX1(Int2DIndividual o) {
		++dominationComparsionCount;
		return Integer.compare(x1, o.x1);
	}

	public int compareX2(Int2DIndividual o) {
		++dominationComparsionCount;
		return Integer.compare(x2, o.x2);
	}

	public int compareDom(Int2DIndividual o) {
		dominationComparsionCount += 2;
		int xc = Integer.compare(x1, o.x1);
		int yc = Integer.compare(x2, o.x2);
		return xc + yc;
	}

	@Override
	public boolean equals(Object o) {
		dominationComparsionCount += 2;
		if (o instanceof Int2DIndividual) {
			Int2DIndividual oo = (Int2DIndividual)o;
			return oo.x1 == x1 && oo.x2 == x2;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (x1 + x2) * (x1 + x2 + 1) + x2;
	}

	@Override
	public String toString() {
		return String.format("ind %d %d", x1, x2);
	}
}
