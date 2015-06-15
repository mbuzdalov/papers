package ru.ifmo.steady.inds;

import java.util.*;

public class StorageLastHull extends StorageWithConvexHull {
    private int REBUILD_LAST_EVERY = 16;
    private int PART_SIZE = 6;

    public String getName() {
        return "INDS-LastHull";
    }

    /* Internals */

    private int numberOfRemovalsSinceLastRebuild = 0;
    private LLNode previousLastLayer = null;

    @Override
    protected LLNode removeWorstByCrowding(int count) {
        LLNode last = super.removeWorstByCrowding(count);
        LLNode lastLayer = layerRoot == null ? null : layerRoot.rightmost().key();
        if (lastLayer != previousLastLayer || (lastLayer != null && lastLayer.changed)) {
            ++numberOfRemovalsSinceLastRebuild;
            previousLastLayer = lastLayer;
            if (previousLastLayer != null) {
                previousLastLayer.changed = false;
            }
            if (numberOfRemovalsSinceLastRebuild >= REBUILD_LAST_EVERY) {
                rebuildLastLayer();
                numberOfRemovalsSinceLastRebuild = 0;
            }
        }
        return last;
    }

    private void rebuildLastLayer() {
        if (layerRoot != null) {
            LLNode lastLayer = layerRoot.rightmost().key();
            int size = lastLayer.size();
            if (size < 3 * PART_SIZE) {
                return;
            }
            LLNode first = lastLayer.leftmost();
            int hullCount = 0;
            for (LLNode curr = first; curr != null; curr = curr.next(), ++hullCount) {
                if (curr.hull != null) {
                    if (curr.hull.isAlive) {
                        curr = curr.hull.last;
                        curr.hull.destroy();
                    }
                }
            }
            int sectionSize = (size + PART_SIZE - 1) / PART_SIZE;
            LLNode curr = first;
            while (curr != null) {
                LLNode p = curr;
                for (int i = 1; i < sectionSize && p.next() != null; ++i, p = p.next());
                new ConvexHull(curr, p);
                curr = p.next();
            }
        }
    }
}
