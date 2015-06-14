package ru.ifmo.steady.inds;

import java.util.*;
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

public class StorageWithConvexHull extends SolutionStorage {
    private static final int REBUILD_LAST_EVERY = 20;
    public static enum Mode {
        Dummy, RebuildLastEvery
    };

    public StorageWithConvexHull(Mode mode) {
        this.mode = mode;
    }

    public void add(Solution s) {
        LLNode node = new LLNode(s);
        addToLayers(node);
    }

    public int getLayerCount() {
        return layerRoot == null ? 0 : layerRoot.size();
    }

    public Iterator<Solution> getLayer(final int index) {
        if (index < 0 || index >= getLayerCount()) {
            throw new IllegalArgumentException("No such layer: " + index);
        }
        return new Iterator<Solution>() {
            private LLNode curr = TreapNode.getKth(layerRoot, index).key().leftmost();
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

    public String getName() {
        return "INDS-" + mode;
    }

    public Solution removeWorst() {
        LLNode node = removeWorstByCrowding(1);
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
        LLNode layerKey = layer.key();
        LLNode llNode = TreapNode.getKth(layerKey, index);
        Solution s = llNode.key();
        if (layerKey.size() <= 2) {
            return new QueryResult(s, Double.POSITIVE_INFINITY, layerIndex);
        } else {
            Solution layerL = layerKey.leftmost().key();
            Solution layerR = layerKey.rightmost().key();
            double crowd = llNode.crowdingDistance(layerL, layerR);
            return new QueryResult(s, crowd, layerIndex);
        }
    }

    /* Internals */

    /**
     * The convex hull processing mode.
     */
    private final Mode mode;
    private int numberOfRemovalsSinceLastRebuild = 0;

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

    private boolean dominates(LLNode layer, final Solution s) {
        LLNode best = null;
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
            split(currLayer.key(), t -> min.compareX(t.key(), counter) > 0, lSplit);
            LLNode tL = lSplit.left;
            split(lSplit.right, t -> max.compareY(t.key(), counter) <= 0, lSplit);
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
            LLNode root = lastLayer.key();
            LLNode min = root.leftmost();
            Solution minS = min.key();
            LLNode max = root.rightmost();
            Solution maxS = max.key();
            int lls = root.size();
            double[] crowding = new double[lls];
            Integer[] indices = new Integer[lls];
            LLNode curr = min;
            int index = 0;
            while (curr != null) {
                indices[index] = index;
                crowding[index] = curr.crowdingDistance(minS, maxS);
                ++index;
                curr = curr.next();
            }
            Arrays.sort(indices, (l, r) -> Double.compare(crowding[r], crowding[l]));
            int remain = lls - count;
            Arrays.sort(indices, 0, remain);
            LLNode newLayer = null;
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

    private LLNode removeWorstByCrowding(int count) {
        int initCount = count;
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
        List<LLNode> equal = new ArrayList<>();
        LLNode last = null;
        while (count-- > 0) {
            LLNode lastLayerRoot = lastLayer.key();
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
                double crowding = Double.POSITIVE_INFINITY;

                LLNode lastLayerL = lastLayerRoot.leftmost();
                LLNode lastLayerR = lastLayerRoot.rightmost();
                Solution lKey = lastLayerL.key();
                Solution rKey = lastLayerR.key();

                Iterator<LLNode> candidateIterator = lastLayerL.nextWorstNodeIterator(lKey, rKey);

                while (candidateIterator.hasNext()) {
                    LLNode curr = candidateIterator.next();
                    double currCrowd = curr.crowdingDistance(lKey, rKey);
                    if (crowding > currCrowd) {
                        crowding = currCrowd;
                        equal.clear();
                    }
                    if (crowding == currCrowd) {
                        equal.add(curr);
                    }
                }

                LLNode chosen = equal.get(rnd.nextInt(equal.size()));
                Solution chosenKey = chosen.key();
                split(lastLayerRoot, lln -> lln.key().compareX(chosenKey, counter) < 0, lSplit);
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
                last = rv;
            }
        }
        if (mode == Mode.RebuildLastEvery) {
            numberOfRemovalsSinceLastRebuild += initCount;
            if (numberOfRemovalsSinceLastRebuild >= REBUILD_LAST_EVERY) {
                rebuildLastLayer();
                numberOfRemovalsSinceLastRebuild = 0;
            }
        }
        return last;
    }

    private void rebuildLastLayer() {
        if (layerRoot != null) {
            LLNode first = layerRoot.rightmost().key().leftmost();
            int size = 0;
            for (LLNode curr = first; curr != null; curr = curr.next()) {
                if (curr.hull != null) {
                    curr.hull.destroy();
                    curr.hull = null;
                }
                ++size;
            }
            int sectionSize = (size + REBUILD_LAST_EVERY - 1) / REBUILD_LAST_EVERY;
            LLNode curr = first;
            while (curr != null) {
                LLNode p = curr;
                for (int i = 1; i < sectionSize && p.next() != null; ++i, p = p.next());
                new ConvexHull(curr, p);
                curr = p.next();
            }
        }
    }

    /* Node classes */

    private final class LLNode extends TreapNode<Solution, LLNode> {
        ConvexHull hull = null;

        public LLNode(Solution key) {
            super(key);
        }

        public double crowdingDistance(Solution leftmost, Solution rightmost) {
            LLNode prev = prev();
            LLNode next = next();
            return key().crowdingDistance(
                prev == null ? null : prev.key(),
                next == null ? null : next.key(),
                leftmost, rightmost, counter
            );
        }

        public CrowdingPoint makeCrowdingPoint() {
            return new CrowdingPoint();
        }

        private final class CrowdingPoint {
            final double x, y;

            CrowdingPoint() {
                LLNode prev = prev();
                LLNode next = next();
                Solution key = key();
                Solution ps = prev == null ? null : prev.key();
                Solution ns = next == null ? null : next.key();
                this.x = key.crowdingDistanceDX(ps, ns, counter);
                this.y = key.crowdingDistanceDY(ps, ns, counter);
            }

            public int turn(CrowdingPoint l, CrowdingPoint r) {
                boolean lInf = Double.isInfinite(l.x);
                boolean rInf = Double.isInfinite(r.x);
                if (lInf || rInf) {
                    if (lInf && rInf) {
                        return 0;
                    } else {
                        return lInf ? 1 : -1;
                    }
                }
                double lx = l.x - x;
                double ly = l.y - y;
                double rx = r.x - x;
                double ry = r.y - y;
                double vm = lx * ry - ly * rx;
                if (Math.abs(vm) < EPS) {
                    return Double.compare(lx * lx + ly * ly, rx * rx + ry * ry);
                } else {
                    return vm > 0 ? -1 : 1;
                }
            }

            public LLNode node() {
                return LLNode.this;
            }
        }

        public Iterator<LLNode> nextWorstNodeIterator(Solution leftmost, Solution rightmost) {
            final double dX = leftmost.crowdingDistanceDX(leftmost, rightmost, counter);
            final double dY = leftmost.crowdingDistanceDY(leftmost, rightmost, counter);
            return new Iterator<LLNode>() {
                private LLNode curr = LLNode.this;

                public boolean hasNext() {
                    return curr != null;
                }

                public LLNode next() {
                    if (curr.hull == null || !curr.hull.isAlive) {
                        LLNode rv = curr;
                        curr = curr.next();
                        return rv;
                    } else {
                        LLNode rv = curr.hull.worstByCrowding(dX, dY);
                        curr = curr.hull.last.next();
                        return rv;
                    }
                }
            };
        }

        @Override
        protected void setPrev(LLNode that) {
            // Independently of anything, break a hull if it is split
            LLNode oldPrev = prev();
            if (hull != null && hull.isAlive && oldPrev != null && hull == oldPrev.hull) {
                hull.destroy();
            }
            super.setPrev(that);
        }

        @Override
        protected void setNext(LLNode that) {
            // Independently of anything, break a hull if it is split
            LLNode oldNext = next();
            if (hull != null && hull.isAlive && oldNext != null && hull == oldNext.hull) {
                hull.destroy();
            }
            super.setNext(that);
        }
    }

    private final class HLNode extends TreapNode<LLNode, HLNode> {
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

    private static final double EPS = 1e-9;

    private final class ConvexHull {
        boolean isAlive = true;
        final boolean isOnlyInfinity;
        final int rangeSize;
        final LLNode first;
        final LLNode last;
        final LLNode.CrowdingPoint[] hull;

        public ConvexHull(LLNode first, LLNode last) {
            this.first = first;
            this.last = last;
            int count = 1;
            LLNode curr = first;
            while (curr != last) {
                curr = curr.next();
                ++count;
            }
            rangeSize = count;
            LLNode.CrowdingPoint[] all = new LLNode.CrowdingPoint[count];
            boolean hasFiniteX = false;
            while (first != last) {
                all[--count] = first.makeCrowdingPoint();
                hasFiniteX |= !Double.isInfinite(all[count].x);
                first.hull = this;
                first = first.next();
            }
            all[--count] = last.makeCrowdingPoint();
            last.hull = this;

            if (!hasFiniteX) {
                isOnlyInfinity = true;
                hull = all;
            } else {
                isOnlyInfinity = false;
                for (int i = 1; i < all.length; ++i) {
                    LLNode.CrowdingPoint f = all[0];
                    LLNode.CrowdingPoint c = all[i];
                    if (c.x < f.x || c.x == f.x && c.y < f.y) {
                        all[0] = c;
                        all[i] = f;
                    }
                }
                LLNode.CrowdingPoint base = all[0];
                Arrays.sort(all, 1, all.length, (l, r) -> base.turn(l, r));
                LLNode.CrowdingPoint[] stack = new LLNode.CrowdingPoint[all.length];
                stack[0] = all[0];
                int sp = 0;
                for (int i = 1; i < all.length; ++i) {
                    if (Double.isInfinite(all[i].x)) {
                        break;
                    }
                    while (sp > 0 && stack[sp - 1].turn(stack[sp], all[i]) > 0) {
                        --sp;
                    }
                    if (stack[sp].y < all[i].y) {
                        break;
                    }
                    stack[++sp] = all[i];
                }
                hull = Arrays.copyOf(stack, sp + 1);
            }
        }

        public LLNode worstByCrowding(double dX, double dY) {
            if (isOnlyInfinity || hull.length == 1) {
                return hull[0].node();
            } else {
                double idX = 1 / dX, idY = 1 / dY;
                //Ternary so far
                int l = 0, r = hull.length - 1;
                while (l + 2 < r) {
                    int ll = (l * 2 + r) / 3;
                    int rr = (l + 2 * r) / 3;
                    double vll = hull[ll].x * idX + hull[ll].y * idY;
                    double vrr = hull[rr].x * idX + hull[rr].y * idY;
                    if (vll < vrr) {
                        r = rr;
                    } else {
                        l = ll;
                    }
                }
                LLNode.CrowdingPoint best = hull[l];
                double bestV = best.x * idX + best.y * idY;
                for (int i = l + 1; i <= r; ++i) {
                    LLNode.CrowdingPoint curr = hull[i];
                    double currV = curr.x * idX + curr.y * idY;
                    if (currV < bestV) {
                        bestV = currV;
                        best = curr;
                    }
                }
                return best.node();
            }
        }

        public void destroy() {
            this.isAlive = false;
        }
    }
}