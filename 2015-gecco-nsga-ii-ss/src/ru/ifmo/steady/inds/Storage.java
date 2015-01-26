package ru.ifmo.steady.inds;

import java.util.*;
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
        if (layer.key().size() <= 2) {
            return new QueryResult(s, Double.POSITIVE_INFINITY, layerIndex);
        } else {
            Solution layerL = layer.key().leftmost().key();
            Solution layerR = layer.key().rightmost().key();
            LLNode np = llNode.prev();
            LLNode nn = llNode.next();
            double crowd = s.crowdingDistance(
                np == null ? null : np.key(),
                nn == null ? null : nn.key(),
                layerL, layerR
            );
            return new QueryResult(s, crowd, layerIndex);
        }
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
        LLNode lastLayerRoot = lastLayer.key();
        if (lastLayerRoot.size() == 1) {
            cutRightmost(layerRoot, hSplit);
            layerRoot = hSplit.left;
            return lastLayer.key();
        } else {
            Random rnd = FastRandom.threadLocal();
            List<Integer> equal = new ArrayList<>();
            double crowding = Double.POSITIVE_INFINITY;

            LLNode lastLayerL = lastLayerRoot.leftmost();
            LLNode lastLayerR = lastLayerRoot.rightmost();
            Solution lKey = lastLayerL.key();
            Solution rKey = lastLayerR.key();
            LLNode curr = lastLayerL;
            LLNode prev = null;
            int index = 0;
            while (curr != null) {
                LLNode next = curr.next();
                double currCrowd = curr.key().crowdingDistance(
                    prev == null ? null : prev.key(),
                    next == null ? null : next.key(),
                    lKey, rKey
                );
                if (crowding > currCrowd) {
                    crowding = currCrowd;
                    equal.clear();
                }
                if (crowding == currCrowd) {
                    equal.add(index);
                }
                ++index;
                prev = curr;
                curr = next;
            }
            index = equal.get(rnd.nextInt(equal.size()));
            splitK(lastLayerRoot, index, lSplit);
            LLNode left = lSplit.left;
            splitK(lSplit.right, 1, lSplit);
            LLNode rv = lSplit.left;
            LLNode right = lSplit.right;
            LLNode newLayer = merge(left, right);
            if (newLayer == null) {
                throw new AssertionError("This layer should be non-empty but it isn't");
            }
            lastLayer.setKey(newLayer);
            int rcIndex = layerRoot.size() - 1;
            recomputeInterval(layerRoot, rcIndex, rcIndex + 1);
            return rv;
        }
    }

    /* Node classes */

    private static final class LLNode extends TreapNode<Solution, LLNode> {
        public LLNode(Solution key) {
            super(key);
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
