package ru.ifmo.ctd.ngp.demo.testgen.flows.solvers;

import java.util.*;

import ru.ifmo.ctd.ngp.demo.testgen.flows.EdgeRec;
import ru.ifmo.ctd.ngp.demo.testgen.flows.MaxFlowSolver;
import ru.ifmo.ctd.ngp.demo.testgen.flows.solvers.util.BasicGraph;
import ru.ifmo.ctd.ngp.demo.testgen.flows.solvers.util.Edge;

/**
 * An implementation of the Edmonds-Carp algorithm for the maximum flow problem.
 *
 * @author Maxim Buzdalov
 */
public final class EdmondsCarpScaling extends MaxFlowSolver {
    static final class Graph extends BasicGraph {
        int bfsCount = 0;
        int vertexCount = 0;
        int testedEdges = 0;

        public Graph(Iterable<EdgeRec> edges) {
            super(edges);
        }

        @Override
        public Map<String, Long> collectMeasures() {
            Map<String, Long> rv = new HashMap<>();
            rv.put("bfsCount", (long) bfsCount);
            rv.put("vertexCount", (long) vertexCount);
            rv.put("edgeCount", (long) testedEdges);
            return rv;
        }

        Edge[] back;
        int[] queue;
        int scale;

        int bfs(int s, int t) {
            int head = 0, tail = 0;
            queue[head++] = s;
            while (head > tail) {
                ++vertexCount;
                int curr = queue[tail++];
                if (curr == t) {
                    break;
                }
                for (Edge e : edges.get(curr)) {
                    int trg = e.trg;
                    ++testedEdges;
                    if (trg != s && e.cap >= scale && back[trg] == null) {
                        back[trg] = e;
                        queue[head++] = trg;
                    }
                }
            }
            if (back[t] == null) {
                return 0;
            } else {
                int curr = t;
                int flow = Integer.MAX_VALUE;
                while (curr != s) {
                    flow = Math.min(flow, back[curr].cap);
                    curr = back[curr].src;
                }
                if (flow > 0) {
                    curr = t;
                    while (curr != s) {
                        back[curr].cap -= flow;
                        back[curr].rev.cap += flow;
                        curr = back[curr].src;
                    }
                }
                return flow;
            }
        }

        public long maxFlow(int s, int t) {
            back = new Edge[edges.size()];
            queue = new int[edges.size()];
            long flow = 0;

            for (scale = 1 << 30; scale >= 1; scale >>>= 1) {
                while (true) {
                    Arrays.fill(back, null);
                    int df = bfs(s, t);
                    ++bfsCount;
                    if (df > 0) {
                        flow += df;
                    } else {
                        break;
                    }
                }
            }
            return flow;
        }
    }

    @Override
    protected BasicGraph constructFromEdges(Iterable<EdgeRec> edges) {
        return new Graph(edges);
    }
}
