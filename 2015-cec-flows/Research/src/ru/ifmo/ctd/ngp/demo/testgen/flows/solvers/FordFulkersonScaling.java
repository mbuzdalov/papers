package ru.ifmo.ctd.ngp.demo.testgen.flows.solvers;

import java.util.*;

import ru.ifmo.ctd.ngp.demo.testgen.flows.EdgeRec;
import ru.ifmo.ctd.ngp.demo.testgen.flows.MaxFlowSolver;
import ru.ifmo.ctd.ngp.demo.testgen.flows.solvers.util.BasicGraph;
import ru.ifmo.ctd.ngp.demo.testgen.flows.solvers.util.Edge;

/**
 * An implementation of the Ford-Fulkerson algorithm with capacity scaling for the maximum flow problem.
 *
 * @author Maxim Buzdalov
 */
public final class FordFulkersonScaling extends MaxFlowSolver {
    static final class Graph extends BasicGraph {
        int dfsCount = 0;
        int vertexCount = 0;
        int testedEdges = 0;

        public Graph(Iterable<EdgeRec> edges) {
            super(edges);
        }

        @Override
        public Map<String, Long> collectMeasures() {
            Map<String, Long> rv = new HashMap<>();
            rv.put("dfsCount", (long) dfsCount);
            rv.put("vertexCount", (long) vertexCount);
            rv.put("edgeCount", (long) testedEdges);
            return rv;
        }

        boolean[] u;
        int a;

        int dfs(int x, int t, int curCapacity) {
            u[x] = true;
            if (x == t) {
                return curCapacity;
            }
            ++vertexCount;
            for (Edge e : edges.get(x)) {
                ++testedEdges;
                if (!u[e.trg] && a <= e.cap) {
                    int cc = dfs(e.trg, t, Math.min(curCapacity, e.cap));
                    if (cc > 0) {
                        e.cap -= cc;
                        e.rev.cap += cc;
                        return cc;
                    }
                }
            }

            return 0;
        }

        public long maxFlow(int s, int t) {
            u = new boolean[edges.size()];
            a = 1 << 30;
            long flow = 0;
            while (true) {
                Arrays.fill(u, false);
                int df = dfs(s, t, Integer.MAX_VALUE);
                ++dfsCount;
                if (df > 0) {
                    flow += df;
                } else {
                    if (a == 1) {
                        break;
                    }
                    a >>= 1;
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
