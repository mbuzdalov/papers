package ru.ifmo.steady.nsga2ss;

import ru.ifmo.steady.Solution;
import ru.ifmo.steady.treap.*;

public class LowLevelNode extends SimpleNode<LowLevelNode> {
	protected final Solution key;
	protected final int id;

	/* Statistics */
	protected int size;
	protected double crowding;
	protected LowLevelNode worst;

	public LowLevelNode(Solution key, int id) {
		this.id = id;
		this.key = key;
		/* Statistic initialization: as if it is single in the tree*/
		this.size = 1;
		this.crowding = Double.POSITIVE_INFINITY;
		this.worst = this;
	}

	public Solution key() {
		return key;
	}

	public int size() {
		return size;
	}

	public int id() {
		return id;
	}

	public LowLevelNode next() {
		return next;
	}

	public double crowdingDistance() {
		return crowding;
	}

	public LowLevelNode worstSubtreeNode() {
		return worst;
	}

	public static final TypeClass TYPE_CLASS = new TypeClass();
	public static class TypeClass extends SimpleNode.TypeClass<LowLevelNode> {
		@Override
		public void makeNodeHook(LowLevelNode tree) {
			/* Statistic update */
			LowLevelNode left = tree.left;
			LowLevelNode right = tree.right;

			/* 1. Crowding distance */
			if (left == null || right == null) {
				tree.crowding = Double.POSITIVE_INFINITY;
			} else {
				tree.crowding = tree.key.crowdingDistance(left.key, right.key);
			}
			/* 2. Worst node */
			tree.worst = tree;
			if (left != null && left.worst.crowding < tree.worst.crowding) {
				tree.worst = left.worst;
			}
			if (right != null && right.worst.crowding < tree.worst.crowding) {
				tree.worst = right.worst;
			}
			/* 3. Size */
			tree.size = 1
					+ (left == null ? 0 : left.size)
					+ (right == null ? 0 : right.size);
		}
	}
}
