package ru.ifmo.ctd.ngp.demo.testgen.flows.solvers;

import java.util.*;

import ru.ifmo.ctd.ngp.demo.testgen.flows.EdgeRec;
import ru.ifmo.ctd.ngp.demo.testgen.flows.MaxFlowSolver;
import ru.ifmo.ctd.ngp.demo.testgen.flows.solvers.util.BasicGraph;
import ru.ifmo.ctd.ngp.demo.testgen.flows.solvers.util.Edge;

/**
 * An implementation of the Dinic algorithm for the maximum flow problem.
 *
 * @author Maxim Buzdalov
 */
public final class Dinic extends MaxFlowSolver {
    static final class Graph extends BasicGraph {
        int phaseCount;
        int dfsCount;
        int vertexCount;
        int edgeCount;

        public Graph(Iterable<EdgeRec> edges) {
            super(edges);
        }

        @Override
        public Map<String, Long> collectMeasures() {
            Map<String, Long> rv = new HashMap<>();
            rv.put("phaseCount", (long) phaseCount);
            rv.put("dfsCount", (long) dfsCount);
            rv.put("vertexCount", (long) vertexCount);
            rv.put("edgeCount", (long) edgeCount);
            return rv;
        }

        List<Edge>[] layerEdges;

        int[] dist;
        int[] queue;

        boolean bfs(int s, int t) {
            ++phaseCount;
            for (int curr = 0; curr < edges.size(); ++curr) {
                layerEdges[curr].clear();
            }
            int head = 0, tail = 0;
            int inf = edges.size() + 1;
            Arrays.fill(dist, inf);
            dist[s] = 0;
            queue[head++] = s;
            while (head > tail) {
                int curr = queue[tail++];
                ++vertexCount;
                if (curr == t) {
                    break;
                }
                for (Edge e : edges.get(curr)) {
                    int trg = e.trg;
                    ++edgeCount;
                    if (e.cap > 0) {
                        if (dist[trg] == inf) {
                            dist[trg] = 1 + dist[curr];
                            queue[head++] = trg;
                        }
                        if (dist[trg] == 1 + dist[curr]) {
                            layerEdges[curr].add(e);
                        }
                    }
                }
            }
            return dist[t] != inf;
        }

        int iDFS(int s, int t, int flow) {
            if (s == t) {
                return flow;
            }
            ++vertexCount;
            List<Edge> currEdges = layerEdges[s];
            for (int i = currEdges.size() - 1; i >= 0; --i) {
                Edge ce = currEdges.get(i);
                ++edgeCount;
                int rec = 0;
                if (ce.cap != 0) {
                    rec = iDFS(ce.trg, t, Math.min(flow, ce.cap));
                }
                if (rec == 0 || ce.cap == rec) {
                    currEdges.remove(i);
                }
                if (rec > 0) {
                    ce.cap -= rec;
                    ce.rev.cap += rec;
                    return rec;
                }
            }
            return 0;
        }

        public long maxFlow(int s, int t) {
            int sz = edges.size();
            @SuppressWarnings({"unchecked"}) List<Edge>[] theEdges = new List[sz];
            layerEdges = theEdges;
            dist = new int[sz];
            queue = new int[sz];
            for (int i = 0; i < sz; ++i) {
                layerEdges[i] = new ArrayList<>();
            }

            long flow = 0;

            while (bfs(s, t)) {
                int currFlow;
                ++dfsCount;
                while ((currFlow = iDFS(s, t, Integer.MAX_VALUE)) != 0) {
                    flow += currFlow;
                    ++dfsCount;
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
