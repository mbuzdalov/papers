package ru.ifmo.ctd.ngp.demo.testgen.flows.solvers.util;

/**
 * A network flow edge.
 */
public final class Edge {
    public final int src, trg;
    public int cap;
    public final Edge rev;

    public Edge(int src, int trg, int cap) {
        this.src = src;
        this.trg = trg;
        this.cap = cap;
        this.rev = new Edge(trg, src, this);
    }

    private Edge(int src, int trg, Edge reverse) {
        this.src = src;
        this.trg = trg;
        this.cap = 0;
        this.rev = reverse;
    }
}
