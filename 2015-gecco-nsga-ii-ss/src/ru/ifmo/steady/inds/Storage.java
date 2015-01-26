package ru.ifmo.steady.inds;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Predicate;

import ru.ifmo.steady.Solution;
import ru.ifmo.steady.SolutionStorage;
import ru.ifmo.steady.inds.TreapNode.SplitResult;
import ru.ifmo.steady.util.FastRandom;

import static ru.ifmo.steady.inds.TreapNode.split;
import static ru.ifmo.steady.inds.TreapNode.splitK;
import static ru.ifmo.steady.inds.TreapNode.getKth;
import static ru.ifmo.steady.inds.TreapNode.merge;
import static ru.ifmo.steady.inds.TreapNode.cutRightmost;

public class Storage implements SolutionStorage {
    public void add(Solution s) {
        LLNode node = new LLNode(s);
        addToLayers(node);
    }

    public Iterator<Solution> nonDominatedSolutionsIncreasingX() {
        if (layerRoot == null) {
            return Collections.emptyIterator();
        } else {
            return new Iterator<Solution>() {
                private LLNode curr = layerRoot.leftmost().key().leftmost();
                public boolean hasNext() {
                    return curr != null;
                }
                public Solution next() {
                    if (!hasNext()) {
                        throw new IllegalStateException("No more elements");
                    } else {
                        Solution rv = curr.key();
                        curr = curr.next();
                        return rv;
                    }
                }
            };
        }
    }

    public String getName() {
        return "INDS";
    }

    public Solution removeWorst() {
        LLNode node = removeWorstByCrowding();
        return node.key();
    }

    public int size() {
        return layerRoot == null ? 0 : layerRoot.totalSize;
    }

    public void clear() {
        layerRoot = null;
    }

    public QueryResult getRandom() {
        if (layerRoot == null) {
            throw new IllegalStateException("empty data structure");
        }
        int index = FastRandom.threadLocal().nextInt(size());
        HLNode layer = layerRoot;
        int layerIndex = 0;
        while (true) {
            HLNode ll = layer.left();
            if (ll != null) {
                if (index < ll.totalSize) {
                    layer = ll;
                    continue;
                }
                layerIndex += ll.size();
                index -= ll.totalSize;
            }
            if (index < layer.key().size()) {
                break;
            }
            layerIndex += 1;
            index -= layer.key().size();
            layer = layer.right();
        }
        LLNode llNode = getKth(layer.key(), index);
        Solution s = llNode.key();
        return new QueryResult(s, llNode.crowding, layerIndex);
    }

    /* Internals */

    /**
     * The tree of layers: each element corresponds to a single layer.
     * Ordering: by layer number (implicit key).
     * Used to determine layers.
     */
    private HLNode layerRoot = null;
    /**
     * For the sake of simplicity, everything is single-threaded,
     * so we can bake these in advance.
     */
    private final SplitResult<LLNode> lSplit = new SplitResult<>();
    private final SplitResult<HLNode> hSplit = new SplitResult<>();
    private final LayerWithIndex lwi = new LayerWithIndex();

    private static boolean dominates(LLNode layer, final Solution s) {
        LLNode best = null;
        int cx = -1;
        while (layer != null) {
            int cmp = s.compareX(layer.key());
            if (cmp >= 0) {
                best = layer;
                layer = layer.right();
                cx = cmp;
            } else {
                layer = layer.left();
            }
        }
        if (best == null) {
            return false;
        } else {
            int cy = s.compareY(best.key());
            return cx == 0 ? cy > 0 : cy >= 0;
        }
    }

    private static final class LayerWithIndex {
        public int index;
        public HLNode layer;
    }

    private LayerWithIndex smallestNonDominatingLayer(Solution s) {
        int index = 0;
        HLNode best = null;
        HLNode curr = layerRoot;
        while (curr != null) {
            HLNode cr = curr.left();
            if (dominates(curr.key(), s)) {
                index += 1;
                if (cr != null) {
                    index += cr.size();
                }
                curr = curr.right();
            } else {
                best = curr;
                curr = cr;
            }
        }
        lwi.layer = best;
        lwi.index = index;
        return lwi;
    }

    private void recomputeInterval(HLNode node, int from, int until) {
        if (node == null) {
            return;
        }
        HLNode left = node.left();
        if (left != null) {
            int ls = left.size();
            if (from < ls) {
                recomputeInterval(left, from, Math.min(until, ls));
            }
            from -= ls;
            until -= ls;
        }
        from -= 1;
        until -= 1;
        HLNode right = node.right();
        if (right != null) {
            int rs = right.size();
            if (until >= 0) {
                recomputeInterval(right, Math.max(0, from), until);
            }
        }
        node.recomputeInternals();
    }

    private void addToLayers(LLNode node) {
        LLNode currPush = node;
        LayerWithIndex currLWI = smallestNonDominatingLayer(node.key());
        HLNode currLayer = currLWI.layer;
        int currIndex = currLWI.index;
        int initIndex = currIndex;
        boolean firstTime = true;
        while (currLayer != null) {
            Solution min = currPush.leftmost().key();
            Solution max = currPush.rightmost().key();
            split(currLayer.key(), t -> min.compareX(t.key()) > 0, lSplit);
            LLNode tL = lSplit.left;
            split(lSplit.right, t -> max.compareY(t.key()) <= 0, lSplit);
            LLNode tM = lSplit.left;
            LLNode tR = lSplit.right;
            if (firstTime && tM != null && tM.key().equals(node.key())) {
                currPush = merge(currPush, tM);
                tM = null;
            }
            firstTime = false;
            currLayer.setKey(merge(tL, merge(currPush, tR)));
            if (tM == null) {
                recomputeInterval(layerRoot, initIndex, currIndex + 1);
                return;
            }
            if (tL == null && tR == null) {
                recomputeInterval(layerRoot, initIndex, currIndex + 1);
                splitK(layerRoot, currIndex + 1, hSplit);
                layerRoot = merge(hSplit.left, merge(new HLNode(tM), hSplit.right));
                return;
            }
            currPush = tM;
            currLayer = currLayer.next();
            ++currIndex;
        }
        recomputeInterval(layerRoot, initIndex, currIndex);
        currLayer = new HLNode(currPush);
        layerRoot = merge(layerRoot, currLayer);
    }

    private LLNode removeWorstByCrowding() {
        if (layerRoot == null) {
            throw new IllegalStateException("Empty data structure");
        }
        HLNode lastLayer = layerRoot.rightmost();
        if (lastLayer.key().size() == 1) {
            cutRightmost(layerRoot, hSplit);
            layerRoot = hSplit.left;
            return lastLayer.key();
        } else {
            LLNode worstExample = lastLayer.key().worst;
            Solution worstExampleKey = worstExample.key();
            split(lastLayer.key(), t -> worstExampleKey.compareX(t.key()) > 0, lSplit);
            LLNode left = lSplit.left;
            split(lSplit.right, t -> worstExampleKey.compareX(t.key()) >= 0, lSplit);
            LLNode equal = lSplit.left;
            LLNode right = lSplit.right;
            if (equal == null) {
                throw new AssertionError("Finding out the worst individual cluster failed");
            }
            cutRightmost(equal, lSplit);
            LLNode rv = lSplit.right;
            LLNode newLayer = merge(left, merge(lSplit.left, right));
            if (newLayer == null) {
                throw new AssertionError("This layer should be non-empty but it isn't");
            }
            lastLayer.setKey(newLayer);
            int index = layerRoot.size() - 1;
            recomputeInterval(layerRoot, index, index + 1);
            return rv;
        }
    }

    /* Node classes */

    private static final class LLNode extends TreapNode<Solution, LLNode> {
        double crowding;
        LLNode worst;

        public LLNode(Solution key) {
            super(key);
        }

        @Override
        public final void recomputeInternals() {
            super.recomputeInternals();

            // 1. Crowding distance
            LLNode p = prev(), n = next();
            if (p != null && n != null) {
                crowding = key().crowdingDistance(p.key(), n.key());
            } else {
                crowding = Double.POSITIVE_INFINITY;
            }

            // 2. Worst node.
            LLNode l = left(), r = right();
            worst = this;
            if (l != null) {
                if (l.worst.crowding < worst.crowding) {
                    worst = l.worst;
                }
            }
            if (r != null) {
                if (r.worst.crowding < worst.crowding) {
                    worst = r.worst;
                }
            }
        }
    }

    private static final class HLNode extends TreapNode<LLNode, HLNode> {
        int totalSize;

        public HLNode(LLNode key) {
            super(key);
        }

        @Override
        public final void recomputeInternals() {
            super.recomputeInternals();
            totalSize = key().size();
            HLNode left = left(), right = right();
            if (left != null) {
                totalSize += left.totalSize;
            }
            if (right != null) {
                totalSize += right.totalSize;
            }
        }
    }
}
