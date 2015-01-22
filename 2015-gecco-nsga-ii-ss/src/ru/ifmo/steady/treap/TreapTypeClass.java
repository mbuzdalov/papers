package ru.ifmo.steady.treap;

import java.util.function.Predicate;

/**
 * A basic type class for treaps.
 * @author Maxim Buzdalov
 */
public interface TreapTypeClass<T> {
	public class SplitResult<T> {
		public T left, right;
	}

	/**
	 * Makes a treap with left and right children as specified
	 * and all meaningful information taken from #middleSource.
	 * Typically, this is called when #middleSource is no longer of use.
	 *
	 * Type classes for immutable treaps typically should create
	 * new Ts with all relevant information from #middleSource copied.
	 * Type classes for mutable trees should better reuse #middleSource:
	 * relink its pointers and return it.
	 */
	public T makeNode(T left, T middleSource, T right);

	/**
	 * Returns a heap key of the given treap node.
	 */
	public int heapKey(T treap);

	/**
	 * Returns a left child of the given treap node.
	 */
	public T left(T treap);

	/**
	 * Returns a left child of the given treap node.
	 */
	public T right(T treap);

	/**
	 * Returns the leftmost child of the given treap node (or null if it is null).
	 */
	public default T leftmost(T treap) {
		if (treap == null) {
			return null;
		} else {
			while (true) {
				T left = left(treap);
				if (left == null) {
					return treap;
				}
				treap = left;
			}
		}
	}

	/**
	 * Returns the rightmost child of the given treap node (or null if it is null).
	 */
	public default T rightmost(T treap) {
		if (treap == null) {
			return null;
		} else {
			while (true) {
				T right = right(treap);
				if (right == null) {
					return treap;
				}
				treap = right;
			}
		}
	}

    /**
     * Removes the rightmost child of the given treap.
     */
    public default T removeRightmost(T treap) {
        //This is a TERRIBLE HORRIBLE hack.
        //But it works.

        //We make a predicate ("has a non-null right child")
        //which leads us to the rightmost element
        //and when it first hit "false"
        //it will always return "true" after that.
        boolean[] firstTime = { false };
        SplitResult<T> split = new SplitResult<>();
        split(treap, t -> {
            if (!firstTime[0]) {
                return true;
            } else {
                boolean rv = right(t) != null;
                firstTime[0] |= !rv;
                return rv;
            }
        }, split);
        return split.left;
    }

    public default void splitImpl(T treap, Predicate<T> isLeft, SplitResult<T> result) {
		if (treap == null) {
			result.left = null;
			result.right = null;
		} else if (isLeft.test(treap)) {
			splitImpl(right(treap), isLeft, result);
			result.left = makeNode(left(treap), treap, result.left);
		} else {
			splitImpl(left(treap), isLeft, result);
			result.right = makeNode(result.right, treap, right(treap));
		}
    }

	/**
	 * Splits a treap by a predicate.
	 */
	public default void split(T treap, Predicate<T> isLeft, SplitResult<T> result) {
	    splitImpl(treap, isLeft, result);
	}

    public default T mergeImpl(T left, T right) {
		if (left == null) {
			return right;
		} else if (right == null) {
			return left;
		} else if (heapKey(left) < heapKey(right)) {
			return makeNode(left(left), left, mergeImpl(right(left), right));
		} else {
			return makeNode(mergeImpl(left, left(right)), right, right(right));
		}
    }

	/**
	 * Merges two given treaps.
	 */
	public default T merge(T left, T right) {
	    return mergeImpl(left, right);
	}
}
