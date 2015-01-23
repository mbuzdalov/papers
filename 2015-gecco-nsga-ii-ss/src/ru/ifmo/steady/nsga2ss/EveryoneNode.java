package ru.ifmo.steady.nsga2ss;

import ru.ifmo.steady.Solution;
import ru.ifmo.steady.treap.*;

public class EveryoneNode extends SimpleNode<EveryoneNode> {
	protected final LowLevelNode key;

	/* Statistics */
	protected int size;

	public EveryoneNode(LowLevelNode key) {
		this.key = key;
		/* Statistic initialization: as if it is single in the tree*/
		this.size = 1;
	}

	public EveryoneNode next() {
		return next;
	}

	public LowLevelNode key() {
		return key;
	}

	public int size() {
		return size;
	}

	public static final TypeClass TYPE_CLASS = new TypeClass();
	public static class TypeClass extends SimpleNode.TypeClass<EveryoneNode> {
		@Override
		public void makeNodeHook(EveryoneNode tree) {
			/* Statistic update */
			EveryoneNode left = tree.left;
			EveryoneNode right = tree.right;

			tree.size = 1
					+ (left == null ? 0 : left.size)
					+ (right == null ? 0 : right.size);
		}
	}
}
