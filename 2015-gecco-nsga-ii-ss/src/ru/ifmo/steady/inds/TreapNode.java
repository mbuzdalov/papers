package ru.ifmo.steady.inds;

import java.util.function.Predicate;

import ru.ifmo.steady.util.FastRandom;

public class TreapNode<K, ThisType extends TreapNode<K, ThisType>> {
	private ThisType left, right, prev, next;
	private K key;
	private final int heapKey = FastRandom.threadLocal().nextInt();

	public TreapNode(K key) {
		this.key = key;
		this.left = null;
		this.right = null;
		this.prev = null;
		this.next = null;
		recomputeInternals();
	}

	public K key() {
		return key;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public int heapKey() {
		return heapKey;
	}

	public ThisType left() {
		return left;
	}

	public ThisType right() {
		return right;
	}

	public ThisType prev() {
		return prev;
	}

	public ThisType next() {
		return next;
	}

	public ThisType leftmost() {
		@SuppressWarnings({"unchecked"})
		ThisType curr = (ThisType) (this);
		while (curr.left() != null) {
			curr = curr.left();
		}
		return curr;
	}

	public ThisType rightmost() {
		@SuppressWarnings({"unchecked"})
		ThisType curr = (ThisType) (this);
		while (curr.right() != null) {
			curr = curr.right();
		}
		return curr;
	}

	protected void recomputeInternals() {}

	protected void setLeft(ThisType left) {
		this.left = left;
		recomputeInternals();
	}

	protected void setRight(ThisType right) {
		this.right = right;
		recomputeInternals();
	}

	protected void setPrev(ThisType prev) {
		this.prev = prev;
		recomputeInternals();
	}

	protected void setNext(ThisType next) {
		this.next = next;
		recomputeInternals();
	}

    protected void propagateLeftmostUp() {
        if (left != null) {
            left.propagateLeftmostUp();
        }
        recomputeInternals();
    }

    protected void propagateRightmostUp() {
        if (right != null) {
            right.propagateRightmostUp();
        }
        recomputeInternals();
    }

	private static <
		K,
		T extends TreapNode<K, T>
	> T mergeImpl(T left, T right) {
		if (left == null) {
			return right;
		} else if (right == null) {
			return left;
		} else if (left.heapKey() < right.heapKey()) {
			left.setRight(mergeImpl(left.right(), right));
			return left;
		} else {
			right.setLeft(mergeImpl(left, right.left()));
			return right;
		}
	}

	public static <
		K,
		T extends TreapNode<K, T>
	> T merge(T left, T right) {
		if (left != null && right != null) {
			T lb = left.rightmost();
			T rb = right.leftmost();
			if (lb.next() != null || rb.prev() != null) {
				throw new AssertionError("Links should not exist prior creation");
			}
			lb.setNext(rb);
			rb.setPrev(lb);
			left.propagateRightmostUp();
			right.propagateLeftmostUp();
		}
		return mergeImpl(left, right);
	}

	private static <
		K,
		T extends TreapNode<K, T>
	> void splitImpl(T node, Predicate<T> isLeft, SplitResult<T> split) {
		if (node == null) {
			split.left = null;
			split.right = null;
		} else if (isLeft.test(node)) {
			splitImpl(node.right(), isLeft, split);
			node.setRight(split.left);
			split.left = node;
		} else {
			splitImpl(node.left(), isLeft, split);
			node.setLeft(split.right);
			split.right = node;
		}
	}

	public static <
		K,
		T extends TreapNode<K, T>
	> void split(T node, Predicate<T> isLeft, SplitResult<T> split) {
		splitImpl(node, isLeft, split);
		if (split.left != null && split.right != null) {
			T lb = split.left.rightmost();
			T rb = split.right.leftmost();
			if (lb.next() != rb || rb.prev() != lb) {
				throw new AssertionError("Links should exist prior cutting");
			}
			lb.setNext(null);
			rb.setPrev(null);
			split.left.propagateRightmostUp();
			split.right.propagateLeftmostUp();
		}
	}

	public static<
		K,
		T extends TreapNode<K, T>
	> void cutRightmost(T node, SplitResult<T> split) {
		if (node == null) {
			split.left = null;
			split.right = null;
		} else if (node.right() == null) {
			split.left = node.left();
			split.right = node;
			node.setLeft(null);
			if (node.prev() != null) {
				node.prev().setNext(null);
				node.setPrev(null);
			}
		} else {
			cutRightmost(node.right(), split);
			node.setRight(split.left);
			split.left = node;
		}
	}

	public static class SplitResult<T> {
		public T left, right;
	}
}
