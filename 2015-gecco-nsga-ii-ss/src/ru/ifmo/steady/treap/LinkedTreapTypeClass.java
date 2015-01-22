package ru.ifmo.steady.treap;

import java.util.function.Predicate;

/**
 * A type class for treaps which maintain order links.
 *
 * @author Maxim Buzdalov
 */
public interface LinkedTreapTypeClass<T> extends TreapTypeClass<T> {
	/**
	 * Delets an order link between the given treap nodes.
	 */
	public void deleteOrderLink(T left, T right);

	/**
	 * Creates an order link between the given treap nodes.
	 */
	public void createOrderLink(T left, T right);

	/**
	 * Splits a treap by a predicate.
	 * After that, deletes an order link between split results.
	 */
	public default void split(T treap, Predicate<T> isLeft, SplitResult<T> result) {
		TreapTypeClass.super.split(treap, isLeft, result);
		if (result.left != null && result.right != null) {
			T leftRightmost = rightmost(result.left);
			T rightLeftmost = leftmost(result.right);
			deleteOrderLink(leftRightmost, rightLeftmost);
		}
	}

	/**
	 * Merges two given treaps.
	 * Before that, creates an order link between the arguments.
	 */
	public default T merge(T left, T right) {
		if (left != null && right != null) {
			T leftRightmost = rightmost(left);
			T rightLeftmost = leftmost(right);
			createOrderLink(leftRightmost, rightLeftmost);
		}
		return TreapTypeClass.super.merge(left, right);
	}
}
