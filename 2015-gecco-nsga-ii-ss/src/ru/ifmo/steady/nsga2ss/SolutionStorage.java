package ru.ifmo.steady.nsga2ss;

import ru.ifmo.steady.Solution;
import ru.ifmo.steady.treap.TreapTypeClass.SplitResult;
import ru.ifmo.steady.util.FastRandom;

public class SolutionStorage {
	private static final LowLevelNode.TypeClass LLT = LowLevelNode.TYPE_CLASS;
	private static final HighLevelNode.TypeClass HLT = HighLevelNode.TYPE_CLASS;
	private static final EveryoneNode.TypeClass ET = EveryoneNode.TYPE_CLASS;

	private HighLevelNode root = null;
	private EveryoneNode everyoneRoot = null;
	private final int maxSize;
	private int idSource = 0;

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
		printRoot();
		if (size() > maxSize) {
			System.out.println("Calling deleteWorst()");
			deleteWorst();
		}
	}

    public QueryResult chooseRandom() {
        if (root == null) {
            throw new IllegalStateException("no elements right now");
        }
        int index = FastRandom.threadLocal().nextInt(size());
        LowLevelNode node = getKth(everyoneRoot, index);
        int layer = smallestNonDominatingLayerIndex(node.key());
        return new QueryResult(node.key(), layer, node.crowdingDistance());
    }

    public int size() {
        return everyoneRoot == null ? 0 : everyoneRoot.size();
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

	public int smallestNonDominatingLayerIndex(Solution solution) {
		HighLevelNode curr = root;
		int layer = 0;
		while (curr != null) {
			if (dominates(curr.key(), solution)) {
				HighLevelNode left = HLT.left(curr);
				if (left != null) {
					layer += left.layerCount();
				}
				layer += 1;
				curr = HLT.right(curr);
			} else {
				curr = HLT.left(curr);
			}
		}
		return layer;
	}

	private void printRoot() {
		if (root == null) {
			System.out.println("{}");
		} else {
			HighLevelNode left = HLT.leftmost(root);
			int layerNo = 0;
			while (left != null) {
				System.out.println(layerNo + " {");
				System.out.print("    ");
				LowLevelNode n = LLT.leftmost(left.key());
				while (n != null) {
					System.out.print(n.key() + "(" + n.crowdingDistance() + ") ");
					n = n.next();
				}
				System.out.println("\n}");
				left = left.next();
				++layerNo;
			}
		}
	}

    private LowLevelNode getKth(EveryoneNode curr, int k) {
        EveryoneNode left = ET.left(curr);
        if (left != null) {
            if (k < left.size()) {
                return getKth(left, k);
            }
            k -= left.size();
        }
        if (k == 0) {
            return curr.key();
        } else {
            return getKth(ET.right(curr), k - 1);
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
		System.out.println("Adding " + solution);
		LowLevelNode curr = new LowLevelNode(solution, ++idSource);
		HighLevelNode layer = smallestNonDominatingLayer(solution);
		SplitResult<LowLevelNode> split = new SplitResult<>();

		// Inserting a node in the "everyone" pool
		EveryoneNode every = new EveryoneNode(curr);
		if (everyoneRoot == null) {
			everyoneRoot = every;
		} else {
			int currID = curr.id();
			SplitResult<EveryoneNode> everySplit = new SplitResult<>();
			ET.split(everyoneRoot, t -> t.key().id() < currID, everySplit);
			everyoneRoot = ET.merge(everySplit.left, ET.merge(every, everySplit.right));
		}

		while (layer != null) {
			LLT.split(layer.key(), t -> t.key().compareX(solution) < 0, split);
			LowLevelNode splitL = split.left;
			LLT.split(split.right, t -> t.key().compareY(solution) >= 0, split);
			LowLevelNode splitM = split.left;
			LowLevelNode splitR = split.right;

   			layer.setKey(LLT.merge(splitL, LLT.merge(curr, splitR)));

			if (splitM == null) {
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
				return;
			}

			curr = splitM;
			layer = layer.next();
		}

		HighLevelNode newLayer = new HighLevelNode(curr);
		root = HLT.merge(root, newLayer);
	}

	public Solution deleteWorst() {
		if (root != null) {
			printRoot();
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
			LowLevelNode reallyRemoved = LLT.rightmost(splitM);
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

			System.out.println("Trying to delete node " + reallyRemoved.key());
			// removing from the "everyone" pool
			SplitResult<EveryoneNode> everySplit = new SplitResult<>();
			ET.split(everyoneRoot, t -> t.key().id() < reallyRemoved.id(), everySplit);
			EveryoneNode everyLeft = everySplit.left;
			ET.split(everySplit.right, t -> t.key().id() <= reallyRemoved.id(), everySplit);

			if (everySplit.left == null || everySplit.left.size() != 1) {
				throw new AssertionError("Deletion by ID failed");
			}
			everyoneRoot = ET.merge(everyLeft, everySplit.right);

			printRoot();

			// done
			return reallyRemoved.key();
		} else {
		    throw new IllegalStateException("no elements");
		}
	}
}
