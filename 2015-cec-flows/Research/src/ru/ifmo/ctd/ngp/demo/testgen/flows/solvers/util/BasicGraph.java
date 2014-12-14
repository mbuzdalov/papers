package ru.ifmo.ctd.ngp.demo.testgen.flows.solvers.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.ifmo.ctd.ngp.demo.testgen.flows.EdgeRec;

/**
 * A superclass for graphs to solve maximum flows.
 */
public abstract class BasicGraph {
    protected List<List<Edge>> edges = new ArrayList<>();

    public BasicGraph(Iterable<EdgeRec> edges) {
        for (EdgeRec e : edges) {
            addEdge(e.source(), e.target(), e.capacity());
        }
    }

    public void addEdge(int s, int t, int c) {
        int maxVertex = Math.max(s, t);
        while (edges.size() <= maxVertex) {
            edges.add(new ArrayList<Edge>());
        }
        Edge e = new Edge(s, t, c);
        edges.get(s).add(e);
        edges.get(t).add(e.rev);
    }

    public abstract long maxFlow(int s, int t);
    public abstract Map<String, Long> collectMeasures();
}
