package ru.ifmo.steady.nsga2ss;

import ru.ifmo.steady.Solution;
import ru.ifmo.steady.treap.TreapTypeClass.SplitResult;
import ru.ifmo.steady.util.FastRandom;

public class SolutionStorage {
	private static final LowLevelNode.TypeClass LLT = LowLevelNode.TYPE_CLASS;
	private static final HighLevelNode.TypeClass HLT = HighLevelNode.TYPE_CLASS;

	private HighLevelNode root = null;
	private final int maxSize;

    public static class QueryResult {
        public final Solution solution;
        public final int layerNumber;
        public final double crowdingDistance;

        public QueryResult(Solution solution, int layerNumber, double crowdingDistance) {
            this.solution = solution;
            this.layerNumber = layerNumber;
            this.crowdingDistance = crowdingDistance;
        }
    }

	public SolutionStorage(int maxSize) {
		this.maxSize = maxSize;
	}

	public void add(Solution solution) {
		addImpl(solution);
		if (root.solutionCount() > maxSize) {
			deleteWorst();
		}
	}

    public QueryResult chooseRandom() {
        if (root == null) {
            throw new IllegalStateException("no elements right now");
        }
        int index = FastRandom.threadLocal().nextInt(size());
        int layer = 0;
        HighLevelNode hlt = root;
        while (true) {
            HighLevelNode left = HLT.left(hlt);
            HighLevelNode right = HLT.right(hlt);
            if (index < 0 || index > hlt.solutionCount()) {
                throw new AssertionError("must not happen");
            }
            if (left != null) {
                if (index < left.solutionCount()) {
                    hlt = left;
                    continue;
                } else {
                    index -= left.solutionCount();
                    layer += left.layerCount();
                }
            }
            if (index < hlt.key().size()) {
                LowLevelNode ll = getKth(hlt.key(), index);
                return new QueryResult(ll.key(), layer, ll.crowdingDistance());
            } else {
                index -= hlt.key().size();
                layer += 1;
                hlt = right;
            }
        }
    }

    public int size() {
        return root == null ? 0 : root.solutionCount();
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

    private LowLevelNode getKth(LowLevelNode curr, int k) {
        LowLevelNode left = LLT.left(curr);
        if (left != null) {
            if (k < left.size()) {
                return getKth(left, k);
            }
            k -= left.size();
        }
        if (k == 0) {
            return curr;
        } else {
            return getKth(LLT.right(curr), k - 1);
        }
    }

	private boolean dominates(LowLevelNode tree, Solution solution) {
		Solution s = null;
		int scx = 0;
		while (tree != null) {
			Solution curr = tree.key();
			int cx = curr.compareX(solution);
			if (cx <= 0) {
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

            //TODO: эта хрень сбивает всю статистику HLT
   			layer.setKey(LLT.merge(splitL, LLT.merge(curr, splitR)));

			if (splitM == null) {
			    System.out.println("Exit 1");
				return;
			}
			if (splitL == null && splitR == null) {
				HighLevelNode newLayer = new HighLevelNode(splitM);

				// Okay, that's done only once, so we can afford
				// one more piece with complexity of smallestNonDominatingLayer.
				// Unfortunately it comes at a cost of several comparisons.
				Solution example = splitM.key();
				SplitResult<HighLevelNode> hlSplit = new SplitResult<>();
				HLT.split(root, t -> dominates(t.key(), example), hlSplit);
				root = HLT.merge(hlSplit.left, HLT.merge(newLayer, hlSplit.right));
			    System.out.println("Exit 2");
				return;
			}

			curr = splitM;
			layer = layer.next();
		}

		HighLevelNode newLayer = new HighLevelNode(curr);
		root = HLT.merge(root, newLayer);
        System.out.println("Exit 3");
	}

	public Solution deleteWorst() {
		if (root != null) {
		    // getting the last layer
			HighLevelNode rightmost = HLT.rightmost(root);
			LowLevelNode layer = rightmost.key();
			// getting the worst individual
			LowLevelNode worst = layer.worstSubtreeNode();
			Solution worstKey = worst.key();
			// taking out all individuals equal to worst
			SplitResult<LowLevelNode> split = new SplitResult<>();
			LLT.split(layer, t -> t.key().compareX(worstKey) < 0, split);
			LowLevelNode splitL = split.left;
			LLT.split(split.right, t -> t.key().compareX(worstKey) <= 0, split);
			LowLevelNode splitM = split.left;
			LowLevelNode splitR = split.right;
			if (splitM == null) {
			    throw new AssertionError("splitM must not be empty here");
			}
			// removing any of them (the rightmost in this case)
			Solution reallyRemoved = LLT.rightmost(splitM).key();
			LowLevelNode splitMNew = LLT.removeRightmost(splitM);
			// gathering them all together
			LowLevelNode newContents = LLT.merge(splitL, LLT.merge(splitMNew, splitR));
			if (newContents == null) {
			    // removing the last layer
			    root = HLT.removeRightmost(root);
			} else {
			    // updating the last layer
			    rightmost.setKey(newContents);
			}
			return reallyRemoved;
		} else {
		    throw new IllegalStateException("no elements");
		}
	}
}
