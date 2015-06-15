package ru.ifmo.steady.inds;

import java.util.*;

public class StorageAllHulls extends StorageWithConvexHull {
    public String getName() {
        return "INDS-AllHulls";
    }

    private int maxLastLayerSize = 0;

    @Override
    protected void llNodeLinkHook(LLNode node, boolean isSetPrev) {
        int upperLimit = maxLastLayerSize = Math.max(maxLastLayerSize, (int) (Math.sqrt(layerRoot.rightmost().key().size())));
        if (isSetPrev) {
            LLNode prev = node.prev();
            if (prev == null) {
                new ConvexHull(node, node.hull.last);
            } else if (prev.next() == node) {
                if (node.hull == null || prev.hull == null || node.hull != prev.hull) {
                    int sumRange = (node.hull == null ? 0 : node.hull.rangeSize) + (prev.hull == null ? 0 : prev.hull.rangeSize);
                    LLNode theFirst = prev.hull == null ? prev : prev.hull.first;
                    LLNode theLast = node.hull == null ? node : node.hull.last;
                    if (sumRange <= upperLimit) {
                        new ConvexHull(theFirst, theLast);
                    } else {
                        LLNode mid = theFirst;
                        for (int i = 2; i < sumRange; i += 2) {
                            mid = mid.next();
                        }
                        new ConvexHull(theFirst, mid);
                        new ConvexHull(mid.next(), theLast);
                    }
                }
            }
        } else {
            LLNode next = node.next();
            if (next == null) {
                new ConvexHull(node.hull.first, node);
            } else if (next.prev() == node) {
                if (node.hull == null || next.hull == null || node.hull != next.hull) {
                    int sumRange = (node.hull == null ? 0 : node.hull.rangeSize) + (next.hull == null ? 0 : next.hull.rangeSize);
                    LLNode theFirst = node.hull == null ? node : node.hull.first;
                    LLNode theLast = next.hull == null ? next : next.hull.last;
                    if (sumRange <= upperLimit) {
                        new ConvexHull(theFirst, theLast);
                    } else {
                        LLNode mid = theFirst;
                        for (int i = 2; i < sumRange; i += 2) {
                            mid = mid.next();
                        }
                        new ConvexHull(theFirst, mid);
                        new ConvexHull(mid.next(), theLast);
                    }
                }
            }
        }
    }
}
