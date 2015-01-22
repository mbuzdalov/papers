package ru.ifmo.steady.nsga2ss;

import ru.ifmo.steady.Solution;
import ru.ifmo.steady.treap.TreapTypeClass.SplitResult;

public class SolutionStorage {
	private static final LowLevelNode.TypeClass LLT = LowLevelNode.TYPE_CLASS;
	private static final HighLevelNode.TypeClass HLT = HighLevelNode.TYPE_CLASS;

	private HighLevelNode root = null;
	private final int maxSize;

	public SolutionStorage(int maxSize) {
		this.maxSize = maxSize;
	}

	public void add(Solution solution) {
		addImpl(solution);
		if (root.solutionCount() > maxSize) {
			deleteWorst();
		}
	}

	public HighLevelNode smallestNonDominatingLayer(Solution solution) {
		HighLevelNode curr = root;
		HighLevelNode rv = null;
		while (curr != null) {
			if (!dominates(curr.key(), solution)) {
				rv = curr;
				curr = HLT.left(curr);
			} else {
				curr = HLT.right(curr);
			}
		}
		return rv;
	}

	private boolean dominates(LowLevelNode tree, Solution solution) {
		Solution s = null;
		int scx = 0;
		while (tree != null) {
			Solution curr = tree.key();
			int cx = curr.compareX(solution);
			if (cmp <= 0) {
				s = curr;
				scx = cx;
				tree = LLT.right(tree);
			} else {
				tree = LLT.left(tree);
			}
		}
		if (s == null) {
			return false;
		} else {
			int scy = s.compareY(solution);
			return scx == 0 ? scy < 0 : scy <= 0;
		}
	}

	private void addImpl(Solution solution) {
		LowLevelNode curr = new LowLevelNode(solution);
		HighLevelNode layer = smallestNonDominatingLayer(solution);
		SplitResult<LowLevelNode> split = new SplitResult<>();

		while (layer != null) {
			LLT.split(layer.key(), t -> t.key().compareX(solution) < 0, split);
			LowLevelNode splitL = split.left;
			LLT.split(split.right, t -> t.key().compareY(solution) >= 0, split);
			LowLevelNode splitM = split.left;
			LowLevelNode splitR = split.right;

			layer.setKey(LLT.merge(splitL, LLT.merge(curr, splitR)));
			curr = splitM;

			if (curr == null) {
				return;
			}
			if (splitL == null && splitR == null) {
				HighLevelNode newLayer = new HighLevelNode(curr);
				//TODO: insert newLayer after layer
			}

			layer = layer.next();
		}

		HighLevelNode newLayer = new HighLevelNode(curr);
		root = HLT.merge(root, newLayer);
	}

	private void deleteWorst() {
		if (root != null) {
			HighLevelNode rightmost = HLT.rightmost(root);
			LowLevelNode layer = rightmost.key();
			LowLevelNode worst = layer.worstNode();
			SplitResult<LowLevelNode> split = new SplitResult<>();
			//TODO: actually delete
		}
	}
}
