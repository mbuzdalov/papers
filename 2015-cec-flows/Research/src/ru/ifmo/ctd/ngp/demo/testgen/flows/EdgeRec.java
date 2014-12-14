package ru.ifmo.ctd.ngp.demo.testgen.flows;

/**
 * A graph's edge from the view of evolutionary configurations.
 *
 * @author Maxim Buzdalov
 */
public class EdgeRec {
    public final int source;
    public final int target;
    public final int capacity;

    public EdgeRec(int source, int target, int capacity) {
        this.source = source;
        this.target = target;
        this.capacity = capacity;
    }

    public int source() { return source; }
    public int target() { return target; }
    public int capacity() { return capacity; }
}
