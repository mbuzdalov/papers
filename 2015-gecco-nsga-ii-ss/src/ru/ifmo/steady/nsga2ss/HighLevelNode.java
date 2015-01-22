package ru.ifmo.steady.nsga2ss;

import ru.ifmo.steady.Solution;
import ru.ifmo.steady.treap.*;

public class HighLevelNode extends SimpleNode<HighLevelNode> {
	protected LowLevelNode key;

	/* Statistics */
	protected int layerCount;
	protected int solutionCount;

	public HighLevelNode(LowLevelNode key) {
		this.key = key;
		/* Statistic initialization: as if it is a single one in tree */
		this.layerCount = 1;
		this.solutionCount = key.size();
	}

	public LowLevelNode key() {
		return key;
	}

	public void setKey(LowLevelNode newKey) {
		this.key = newKey;
	}

	public int layerCount() {
		return layerCount;
	}

	public int solutionCount() {
		return solutionCount;
	}

	public HighLevelNode next() {
		return next;
	}

	public static final TypeClass TYPE_CLASS = new TypeClass();
	public static class TypeClass extends SimpleNode.TypeClass<HighLevelNode> {
		@Override
		public void makeNodeHook(HighLevelNode tree) {
			HighLevelNode left = tree.left;
			HighLevelNode right = tree.right;

			/* Statistic update */
			tree.layerCount = 1;
			tree.solutionCount = tree.key.size();
			if (left != null) {
				tree.layerCount += left.layerCount;
				tree.solutionCount += left.solutionCount;
			}
			if (right != null) {
				tree.layerCount += right.layerCount;
				tree.solutionCount += right.solutionCount;
			}
		}
	}
}
