package org.itmo.iyakupov.nds.gen;

/**
 * An interface for test generators.
 * @author Ilya Yakupov
 */
public interface ITestDataGen<E> {
	public E generate(int dim, int max);
}
