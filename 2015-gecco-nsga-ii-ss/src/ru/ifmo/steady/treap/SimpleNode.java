package ru.ifmo.steady.treap;

import ru.ifmo.steady.util.FastRandom;

public class SimpleNode<S extends SimpleNode<S>> {
	protected final int heapKey = FastRandom.threadLocal().nextInt();
	protected S left, right, prev, next;

	public static class TypeClass<S extends SimpleNode<S>> implements LinkedTreapTypeClass<S> {
		public void makeNodeHook(S newNode) {}

		@Override
		public S makeNode(S left, S middleSource, S right) {
			middleSource.left = left;
			middleSource.right = right;
			makeNodeHook(middleSource);
			return middleSource;
		}

        @Override
        public int heapKey(S tree) {
            return tree.heapKey;
        }

        @Override
        public S left(S tree) {
            return tree.left;
        }

        @Override
        public S right(S tree) {
            return tree.right;
        }

        @Override
        public void createOrderLink(S left, S right) {
            if (left.next != null || right.prev != null) {
                throw new AssertionError("links should not exist prior creation");
            }
            left.next = right;
            right.prev = left;
        }

        @Override
        public void deleteOrderLink(S left, S right) {
            if (left.next != right || right.prev != left) {
                throw new AssertionError("links should have existed before deletion");
            }
            left.next = null;
            right.prev = null;
        }
	}
}
