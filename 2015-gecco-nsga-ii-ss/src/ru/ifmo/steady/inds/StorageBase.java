package ru.ifmo.steady.inds;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import ru.ifmo.steady.ComparisonCounter;
import ru.ifmo.steady.Solution;
import ru.ifmo.steady.SolutionStorage;
import ru.ifmo.steady.inds.TreapNode.SplitResult;
import ru.ifmo.steady.util.FastRandom;

import static ru.ifmo.steady.inds.TreapNode.split;
import static ru.ifmo.steady.inds.TreapNode.splitK;
import static ru.ifmo.steady.inds.TreapNode.merge;
import static ru.ifmo.steady.inds.TreapNode.cutRightmost;

public abstract class StorageBase<LLType extends TreapNode<Solution, LLType> & StorageBase.LLNodeAdditionals<LLType>> extends SolutionStorage {
    protected abstract LLType newLLNode(Solution s);
    public abstract String getName();

    public void add(Solution s) {
        addToLayers(newLLNode(s));
    }

    public int getLayerCount() {
        return layerRoot == null ? 0 : layerRoot.size();
    }

    public int getLayerSize(int index) {
        if (index < 0 || index >= getLayerCount()) {
            throw new IllegalArgumentException("No such layer: " + index);
        }
        return TreapNode.getKth(layerRoot, index).totalSize;
    }

    public Iterator<Solution> getLayer(final int index) {
        if (index < 0 || index >= getLayerCount()) {
            throw new IllegalArgumentException("No such layer: " + index);
        }
        return new Iterator<Solution>() {
            private LLType curr = TreapNode.getKth(layerRoot, index).key().leftmost();
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

    public Solution removeWorst() {
        LLType node = removeWorstByCrowding(1);
        return node.key();
    }

    public void removeWorst(int count) {
        removeWorstByCrowding(count);
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
        return getKth(FastRandom.geneticThreadLocal().nextInt(size()));
    }

    public QueryResult getKth(int index) {
        if (index < 0 || index >= size()) {
            throw new IllegalArgumentException("index = " + index + " size = " + size());
        }
        HLNode layer = layerRoot;
        int layerIndex = 0;
        while (true) {
            HLNode ll = layer.left();
            if (ll != null) {
                int llts = ll.totalSize;
                if (index < llts) {
                    layer = ll;
                    continue;
                }
                layerIndex += ll.size();
                index -= llts;
            }
            int lks = layer.key().size();
            if (index < lks) {
                break;
            }
            layerIndex += 1;
            index -= lks;
            layer = layer.right();
        }
        LLType layerKey = layer.key();
        LLType llNode = TreapNode.getKth(layerKey, index);
        Solution s = llNode.key();
        if (layerKey.size() <= 2) {
            return new QueryResult(s, Double.POSITIVE_INFINITY, layerIndex);
        } else {
            Solution layerL = layerKey.leftmost().key();
            Solution layerR = layerKey.rightmost().key();
            double globalDX = Solution.crowdingDistanceDX(layerL, layerR, counter);
            double globalDY = Solution.crowdingDistanceDY(layerL, layerR, counter);
            double crowd = llNode.crowdingDistance(globalDX, globalDY);
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
    private final SplitResult<LLType> lSplit = new SplitResult<>();
    private final SplitResult<HLNode> hSplit = new SplitResult<>();
    private final LayerWithIndex lwi = new LayerWithIndex();

    private boolean dominates(LLType layer, final Solution s) {
        LLType best = null;
        int cx = -1;
        while (layer != null) {
            int cmp = s.compareX(layer.key(), counter);
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
            int cy = s.compareY(best.key(), counter);
            return cx == 0 ? cy > 0 : cy >= 0;
        }
    }

    private final class LayerWithIndex {
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

    private void addToLayers(LLType node) {
        LLType currPush = node;
        LayerWithIndex currLWI = smallestNonDominatingLayer(node.key());
        HLNode currLayer = currLWI.layer;
        int currIndex = currLWI.index;
        int initIndex = currIndex;
        boolean firstTime = true;
        while (currLayer != null) {
            Solution min = currPush.leftmost().key();
            Solution max = currPush.rightmost().key();
            split(currLayer.key(), t -> min.compareX(t.key(), counter) > 0, lSplit);
            LLType tL = lSplit.left;
            split(lSplit.right, t -> max.compareY(t.key(), counter) <= 0, lSplit);
            LLType tM = lSplit.left;
            LLType tR = lSplit.right;
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

    public void removeWorstDebCompatible(int count) {
        if (size() < count) {
            throw new IllegalStateException("Insufficient size of data structure");
        }
        int expected = size() - count;
        HLNode lastLayer = layerRoot.rightmost();
        while (lastLayer.key().size() <= count) {
            count -= lastLayer.key().size();
            cutRightmost(layerRoot, hSplit);
            layerRoot = hSplit.left;
            lastLayer = layerRoot.rightmost();
        }
        if (count > 0) {
            LLType root = lastLayer.key();
            LLType min = root.leftmost();
            Solution minS = min.key();
            LLType max = root.rightmost();
            Solution maxS = max.key();
            int lls = root.size();
            double globalDX = Solution.crowdingDistanceDX(minS, maxS, counter);
            double globalDY = Solution.crowdingDistanceDY(minS, maxS, counter);
            double[] crowding = new double[lls];
            Integer[] indices = new Integer[lls];
            LLType curr = min;
            int index = 0;
            while (curr != null) {
                indices[index] = index;
                crowding[index] = curr.crowdingDistance(globalDX, globalDY);
                ++index;
                curr = curr.next();
            }
            Arrays.sort(indices, (l, r) -> Double.compare(crowding[r], crowding[l]));
            int remain = lls - count;
            Arrays.sort(indices, 0, remain);
            LLType newLayer = null;
            for (int i = 0, j = 0; j < remain; ++i) {
                splitK(root, 1, lSplit);
                root = lSplit.right;
                if (indices[j] == i) {
                    newLayer = merge(newLayer, lSplit.left);
                    ++j;
                }
            }
            lastLayer.setKey(newLayer);
            int sz = layerRoot.size();
            recomputeInterval(layerRoot, sz - 1, sz);
        }
        if (size() != expected) {
            throw new AssertionError();
        }
    }

    private LLType removeWorstByCrowding(int count) {
        if (size() < count) {
            throw new IllegalStateException("Insufficient size of data structure");
        }
        HLNode lastLayer = layerRoot.rightmost();
        while (lastLayer.key().size() < count) {
            count -= lastLayer.key().size();
            cutRightmost(layerRoot, hSplit);
            layerRoot = hSplit.left;
            lastLayer = layerRoot.rightmost();
        }
        Random rnd = FastRandom.geneticThreadLocal();
        List<LLType> equal = new ArrayList<>();
        LLType last = null;
        while (count-- > 0) {
            LLType lastLayerRoot = lastLayer.key();
            if (lastLayerRoot.size() == 1) {
                cutRightmost(layerRoot, hSplit);
                layerRoot = hSplit.left;
                last = lastLayer.key();
            } else if (lastLayerRoot.size() == 2) {
                splitK(lastLayerRoot, 1, lSplit);
                boolean choice = rnd.nextInt(2) == 1;
                lastLayer.setKey(choice ? lSplit.left : lSplit.right);
                int rcIndex = layerRoot.size() - 1;
                recomputeInterval(layerRoot, rcIndex, rcIndex + 1);
                last = choice ? lSplit.right : lSplit.left;
            } else {
                equal.clear();
                double[] crowding = { Double.POSITIVE_INFINITY };

                LLType lastLayerL = lastLayerRoot.leftmost();
                LLType lastLayerR = lastLayerRoot.rightmost();
                Solution lKey = lastLayerL.key();
                Solution rKey = lastLayerR.key();
                double globalDX = Solution.crowdingDistanceDX(lKey, rKey, counter);
                double globalDY = Solution.crowdingDistanceDY(lKey, rKey, counter);

                lastLayerRoot.forEachWorstCrowdingDistanceCandidate(globalDX, globalDY, (LLType curr) -> {
                    double currCrowd = curr.crowdingDistance(globalDX, globalDY);
                    if (crowding[0] > currCrowd) {
                        crowding[0] = currCrowd;
                        equal.clear();
                    }
                    if (crowding[0] == currCrowd) {
                        equal.add(curr);
                    }
                });

                LLType chosen = equal.get(rnd.nextInt(equal.size()));
                Solution chosenKey = chosen.key();
                split(lastLayerRoot, lln -> lln.key().compareX(chosenKey, counter) < 0, lSplit);
                LLType left = lSplit.left;
                splitK(lSplit.right, 1, lSplit);
                LLType rv = lSplit.left;
                LLType right = lSplit.right;
                LLType newLayer = merge(left, right);
                if (newLayer == null) {
                    throw new AssertionError("This layer should be non-empty but it isn't");
                }
                lastLayer.setKey(newLayer);
                int rcIndex = layerRoot.size() - 1;
                recomputeInterval(layerRoot, rcIndex, rcIndex + 1);
                last = rv;
            }
        }
        return last;
    }

    /* Node classes */

    public static interface LLNodeAdditionals<FinalType extends LLNodeAdditionals<FinalType>> {
        public double crowdingDistance(double globalDX, double globalDY);
        public void forEachWorstCrowdingDistanceCandidate(double globalDX, double globalDY, Consumer<FinalType> consumer);
    }

    private final class HLNode extends TreapNode<LLType, HLNode> {
        int totalSize;

        public HLNode(LLType key) {
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
