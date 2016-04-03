package ru.ifmo.steady.inds;

import java.util.*;
import java.util.function.Consumer;

import ru.ifmo.steady.Solution;

public class Storage extends StorageBase<Storage.LLNode> {
    protected class LLNode extends TreapNode<Solution, LLNode> implements StorageBase.LLNodeAdditionals<LLNode> {
        public LLNode(Solution key) {
            super(key);
        }

        public double crowdingDistance(double globalDX, double globalDY) {
            LLNode prev = prev();
            LLNode next = next();
            Solution prevKey = prev == null ? null : prev.key();
            Solution nextKey = next == null ? null : next.key();
            return key().crowdingDistanceDX(prevKey, nextKey, counter) / globalDX +
                   key().crowdingDistanceDY(prevKey, nextKey, counter) / globalDY;
        }

        public void forEachWorstCrowdingDistanceCandidate(double globalDX, double globalDY, Consumer<LLNode> consumer) {
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

    @Override
    protected LLNode newLLNode(Solution s) {
        return new LLNode(s);
    }
    @Override
    public String getName() {
        return "INDS";
    }
}
