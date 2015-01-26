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

    public final K key() {
        return key;
    }

    public final void setKey(K key) {
        this.key = key;
    }

    public final int heapKey() {
        return heapKey;
    }

    public final ThisType left() {
        return left;
    }

    public final ThisType right() {
        return right;
    }

    public final ThisType prev() {
        return prev;
    }

    public final ThisType next() {
        return next;
    }

    public final ThisType leftmost() {
        @SuppressWarnings({"unchecked"})
        ThisType curr = (ThisType) (this);
        while (curr.left() != null) {
            curr = curr.left();
        }
        return curr;
    }

    public final ThisType rightmost() {
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

    private static <
        K,
        T extends TreapNode<K, T>
    > void connectRightmost(T left, T right) {
        T lr = left.right();
        if (lr == null) {
            if (left.next() != null || right.prev() != null) {
                throw new AssertionError("Links should not exist prior creation");
            }
            left.setNext(right);
            right.setPrev(left);
        } else {
            connectRightmost(lr, right);
            left.recomputeInternals();
        }
    }

    private static <
        K,
        T extends TreapNode<K, T>
    > void connectLeftmost(T left, T right) {
        T rl = right.left();
        if (rl == null) {
            if (left.next() != null || right.prev() != null) {
                throw new AssertionError("Links should not exist prior creation");
            }
            left.setNext(right);
            right.setPrev(left);
        } else {
            connectLeftmost(left, rl);
            right.recomputeInternals();
        }
    }

    private static <
        K,
        T extends TreapNode<K, T>
    > T mergeImpl(T left, T right, T last) {
        if (left == null) {
            if (last != null && right != null) {
                connectLeftmost(last, right);
            }
            return right;
        } else if (right == null) {
            if (last != null) {
                connectRightmost(left, last);
            }
            return left;
        } else if (left.heapKey() < right.heapKey()) {
            left.setRight(mergeImpl(left.right(), right, left));
            return left;
        } else {
            right.setLeft(mergeImpl(left, right.left(), right));
            return right;
        }
    }

    public static <
        K,
        T extends TreapNode<K, T>
    > T merge(T left, T right) {
        return mergeImpl(left, right, null);
    }

    private static <
        K,
        T extends TreapNode<K, T>
    > void splitImpl(T node, Predicate<T> isLeft, SplitResult<T> split, T closestLeft, T closestRight) {
        if (node == null) {
            split.left = null;
            split.right = null;
            if (closestLeft != null && closestRight != null) {
                if (closestLeft.next() != closestRight && closestRight.prev() != closestLeft) {
                    throw new AssertionError("Links should exist before breaking");
                }
                closestLeft.setNext(null);
                closestRight.setPrev(null);
            }
        } else if (isLeft.test(node)) {
            splitImpl(node.right(), isLeft, split, node, closestRight);
            node.setRight(split.left);
            split.left = node;
        } else {
            splitImpl(node.left(), isLeft, split, closestLeft, node);
            node.setLeft(split.right);
            split.right = node;
        }
    }

    public static <
        K,
        T extends TreapNode<K, T>
    > void split(T node, Predicate<T> isLeft, SplitResult<T> split) {
        splitImpl(node, isLeft, split, null, null);
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
