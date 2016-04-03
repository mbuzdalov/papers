package ru.ifmo.steady.inds;

import java.util.*;
import java.util.function.Consumer;

import ru.ifmo.steady.Solution;

public class StorageHull extends StorageBase<StorageHull.LLNode> {
    private int maxHullSize = 1;

    private LLNode[] hullStack = new LLNode[16];
    private boolean  hullStackContainsOnlyInfinity = true;
    private LLNode[] oneNodeArray1 = new LLNode[1];
    private LLNode[] oneNodeArray2 = new LLNode[1];

    protected class LLNode extends TreapNode<Solution, LLNode> implements StorageBase.LLNodeAdditionals<LLNode> {
        double dx = Double.POSITIVE_INFINITY;
        double dy = Double.POSITIVE_INFINITY;
        LLNode[] hull = null;
        boolean isHullValid = true;

        public LLNode(Solution key) {
            super(key);
        }

        public double crowdingDistance(double globalDX, double globalDY) {
            if (globalDX == 0 || globalDY == 0) {
                return Double.POSITIVE_INFINITY;
            }
            return dx / globalDX + dy / globalDY;
        }

        public void forEachWorstCrowdingDistanceCandidate(double globalDX, double globalDY, Consumer<LLNode> consumer) {
            int mySize = size();
            if (mySize <= maxHullSize) {
                if (mySize == 1) {
                    consumer.accept(this);
                } else {
                    if (!isHullValid) {
                        computeHull();
                    }
                    if (Double.isInfinite(hull[0].dx)) {
                        for (LLNode node : hull) {
                            consumer.accept(node);
                        }
                    } else {
                        double idX = 1 / globalDX, idY = 1 / globalDY;
                        int l = 0, r = hull.length - 1;
                        while (l + 1 < r) {
                            counter.add(2);
                            int m = (l + r) >>> 1;
                            double cdx = hull[m + 1].dx - hull[m].dx;
                            double cdy = hull[m + 1].dy - hull[m].dy;
                            if (cdx * idX + cdy * idY > 0) {
                                r = m;
                            } else {
                                l = m;
                            }
                        }
                        if ((hull[r].dx - hull[l].dx) * idX + (hull[r].dy - hull[l].dy) * idY > 0) {
                            consumer.accept(hull[l]);
                        } else {
                            consumer.accept(hull[r]);
                        }
                    }
                }
            } else {
                LLNode left = left();
                if (left != null) {
                    left.forEachWorstCrowdingDistanceCandidate(globalDX, globalDY, consumer);
                }
                consumer.accept(this);
                LLNode right = right();
                if (right != null) {
                    right.forEachWorstCrowdingDistanceCandidate(globalDX, globalDY, consumer);
                }
            }
        }

        private void reevaluateCrowding() {
            LLNode prev = prev();
            LLNode next = next();
            if (prev == null || next == null) {
                dx = dy = Double.POSITIVE_INFINITY;
            } else {
                Solution prevKey = prev.key();
                Solution nextKey = next.key();
                dx = Solution.crowdingDistanceDX(prevKey, nextKey, counter);
                dy = Solution.crowdingDistanceDY(prevKey, nextKey, counter);
            }
            hull = null;
            isHullValid = false;
        }

        private int addToStack(LLNode node, int stackSize) {
            if (hullStackContainsOnlyInfinity) {
                if (Double.isInfinite(node.dx)) {
                    hullStack[stackSize] = node;
                    return stackSize + 1;
                } else {
                    hullStack[0] = node;
                    hullStackContainsOnlyInfinity = false;
                    return 1;
                }
            } else {
                if (Double.isInfinite(node.dx)) {
                    return stackSize;
                }
                if (stackSize > 0 && hullStack[stackSize - 1].dx == node.dx && hullStack[stackSize - 1].dy == node.dy) {
                    // Skipping equal points
                    return stackSize;
                }
                if (stackSize > 0 && hullStack[stackSize - 1].dy <= node.dy) {
                    // We actually don't build this part of the hull
                    return stackSize;
                }
                if (stackSize == 1 && hullStack[0].dx == node.dx) {
                    // Vertical lines are OK for the hull but useless for crowding distance
                    hullStack[0] = node;
                    return stackSize;
                }
                while (stackSize > 1) {
                    LLNode a = hullStack[stackSize - 2];
                    LLNode b = hullStack[stackSize - 1];
                    if ((b.dx - a.dx) * (node.dy - a.dy) <= (b.dy - a.dy) * (node.dx - a.dx)) {
                        --stackSize;
                    } else {
                        break;
                    }
                }
                hullStack[stackSize] = node;
                return stackSize + 1;
            }
        }

        private void initStackFitting(int value) {
            int hob = Integer.highestOneBit(value);
            int stackSize = hob == value ? hob : hob + hob;
            if (hullStack.length < stackSize) {
                hullStack = new LLNode[stackSize];
            }
            hullStackContainsOnlyInfinity = true;
        }

        private void mergeWithOne(LLNode[] hull) {
            initStackFitting(hull.length + 1);
            int stackSize = 0;
            int index = 0;
            while (index < hull.length && (hull[index].dx < dx || hull[index].dx == dx && hull[index].dy > dy)) {
                stackSize = addToStack(hull[index++], stackSize);
            }
            stackSize = addToStack(this, stackSize);
            while (index < hull.length) {
                stackSize = addToStack(hull[index++], stackSize);
            }
            this.hull = Arrays.copyOf(hullStack, stackSize);
        }

        private void mergeWithTwo(LLNode[] lhull, LLNode[] rhull) {
            initStackFitting(lhull.length + rhull.length + 1);
            int stackSize = 0;
            int lIndex = 0, rIndex = 0;
            boolean usedMe = false;
            while (lIndex < lhull.length || rIndex < rhull.length || !usedMe) {
                LLNode current = null;
                int choice = 0;
                if (lIndex < lhull.length) {
                    current = lhull[lIndex];
                    choice = 1;
                }
                if (rIndex < rhull.length) {
                    if (current == null || current.dx > rhull[rIndex].dx || current.dx == rhull[rIndex].dx && current.dy < rhull[rIndex].dy) {
                        current = rhull[rIndex];
                        choice = 2;
                    }
                }
                if (!usedMe) {
                    if (current == null || current.dx > dx || current.dx == dx && current.dy < dy) {
                        current = this;
                        choice = 3;
                    }
                }
                stackSize = addToStack(current, stackSize);
                switch (choice) {
                    case 1: ++lIndex;       break;
                    case 2: ++rIndex;       break;
                    case 3: usedMe = true;  break;
                    default: throw new AssertionError();
                }
            }
            this.hull = Arrays.copyOf(hullStack, stackSize);
        }

        private LLNode[] makeHullFor(LLNode node, LLNode[] oneNodeArray) {
            if (node == null) {
                return null;
            }
            if (node.hull == null) {
                node.computeHull();
            }
            if (node.hull == null) {
                oneNodeArray[0] = node;
                return oneNodeArray;
            } else {
                return node.hull;
            }
        }

        private void computeHull() {
            if (size() > 1) {
                LLNode[] leftHull  = makeHullFor(left(),  oneNodeArray1);
                LLNode[] rightHull = makeHullFor(right(), oneNodeArray2);
                if (leftHull == null) {
                    mergeWithOne(rightHull);
                } else if (rightHull == null) {
                    mergeWithOne(leftHull);
                } else {
                    mergeWithTwo(leftHull, rightHull);
                }
            }
            isHullValid = true;
        }

        @Override
        public void recomputeInternals() {
            super.recomputeInternals();
            if (isHullValid) {
                LLNode left = left();
                LLNode right = right();
                if (left != null && !left.isHullValid || right != null && !right.isHullValid) {
                    isHullValid = false;
                    hull = null;
                }
            }
        }

        @Override
        protected void setLeft(LLNode that) {
            super.setLeft(that);
            isHullValid = false;
            hull = null;
        }

        @Override
        protected void setRight(LLNode that) {
            super.setRight(that);
            isHullValid = false;
            hull = null;
        }

        @Override
        protected void setPrev(LLNode that) {
            super.setPrev(that);
            reevaluateCrowding();
        }

        @Override
        protected void setNext(LLNode that) {
            super.setNext(that);
            reevaluateCrowding();
        }
    }

    @Override
    public void add(Solution s) {
        super.add(s);
        int lastLayerSize = getLayerSize(getLayerCount() - 1);
        while (true) {
            double mhsOverLog = maxHullSize / Math.log(1 + maxHullSize) * Math.log(2);
            if (mhsOverLog * mhsOverLog > lastLayerSize) {
                break;
            }
            ++maxHullSize;
        }
    }

    @Override
    protected LLNode newLLNode(Solution s) {
        return new LLNode(s);
    }

    @Override
    public String getName() {
        return "INDS-Hull";
    }
}
