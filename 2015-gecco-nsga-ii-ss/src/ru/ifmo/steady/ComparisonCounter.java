package ru.ifmo.steady;

public class ComparisonCounter {
    protected long value;

    public void add(int value) {
        this.value += value;
    }

    public long get() {
        return value;
    }

    public void reset() {
        value = 0;
    }
}
