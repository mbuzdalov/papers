package ru.ifmo.ctd.ngp.demo.testgen.flows.solvers;

import java.util.*;

import ru.ifmo.ctd.ngp.demo.testgen.flows.EdgeRec;
import ru.ifmo.ctd.ngp.demo.testgen.flows.MaxFlowSolver;
import ru.ifmo.ctd.ngp.demo.testgen.flows.solvers.util.BasicGraph;
import ru.ifmo.ctd.ngp.demo.testgen.flows.solvers.util.Edge;

/**
 * An implementation of the "improved shortest path" algorithm for the maximum flow problem
 * from the book
 * Ravindra K. Ahuja, Thomas L. Magnanti, and James B. Orlin. Network Flows: Theory, Algorithms, and Applications.
 *
 * The implementation is copied and translated from C++ code taken here:
 * http://community.topcoder.com/tc?module=Static&d1=tutorials&d2=maxFlowRevisited#1
 *
 * @author Maxim Buzdalov
 */
public final class ImprovedShortestPath extends MaxFlowSolver {
    static final class Graph extends BasicGraph {
        Edge[] predecessor;
        int[] currentEdge;
        int[] queue;
        int[] distance;
        int[] numbs;
        int V;

        List<List<Edge>> revEdges = new ArrayList<>();

        int edgeCount = 0;
        int vertexCount = 0;
        int retreatCount = 0;

        public Graph(Iterable<EdgeRec> edges) {
            super(edges);
        }

        @Override
        public Map<String, Long> collectMeasures() {
            Map<String, Long> rv = new HashMap<>();
            rv.put("edgeCount", (long) edgeCount);
            rv.put("vertexCount", (long) vertexCount);
            rv.put("retreatCount", (long) retreatCount);
            return rv;
        }

        private void revBFS(int t) {
            Arrays.fill(distance, V);
            numbs[V] = V - 1;
            numbs[0] = 1;
            distance[t] = 0;

            int head = 0, tail = 0;
            queue[head++] = t;
            while (head > tail) {
                ++vertexCount;
                int curr = queue[tail++];
                for (Edge e : revEdges.get(curr)) {
                    ++edgeCount;
                    if (e.cap == 0) continue;
                    if (distance[e.src] == V) {
                        distance[e.src] = 1 + distance[curr];
                        queue[head++] = e.src;
                        --numbs[V];
                        ++numbs[distance[e.src]];
                    }
                }
            }
        }

        private int augment(int s, int t) {
            int flow = Integer.MAX_VALUE;

            for (int i = t; i != s; i = predecessor[i].src)  {
                flow = Math.min(flow, predecessor[i].cap);
            }
            for (int i = t; i != s; i = predecessor[i].src)  {
                predecessor[i].cap -= flow;
                predecessor[i].rev.cap += flow;
            }

            return flow;
        }

        private int retreat(int i, int s) {
            ++retreatCount;
            int tmp, mind = V - 1;

            for (Edge e : edges.get(i)) {
                ++edgeCount;
                if (e.cap == 0) continue;
                mind = Math.min(mind, distance[e.trg]);
            }
            tmp = distance[i];
            --numbs[distance[i]];
            distance[i] = 1 + mind;
            ++numbs[distance[i]];

            if (i != s) {
                i = predecessor[i].src;
            }

            return numbs[tmp] == 0 ? -1 : i;
        }

        public long maxFlow(int s, int t) {
            V = edges.size();
            predecessor = new Edge[V];
            currentEdge = new int[V];
            queue = new int[V];
            distance = new int[V];
            numbs = new int[V + 1];

            for (int i = 0; i < V; ++i) {
                revEdges.add(new ArrayList<Edge>());
            }
            for (List<Edge> le : edges) {
                for (Edge e : le) {
                    revEdges.get(e.trg).add(e);
                }
            }

            long flow = 0;
            revBFS(t);

            int curr = s;
            while (distance[curr] < V) {
                ++vertexCount;
                List<Edge> currE = edges.get(curr);
                // Start searching an admissible arc from the current arc
                while (currentEdge[curr] < currE.size()) {
                    ++edgeCount;
                    Edge e = currE.get(currentEdge[curr]);
                    if (e.cap > 0 && distance[curr] == distance[e.trg] + 1) {
                        break;
                    }
                    ++currentEdge[curr];
                }
                if (currentEdge[curr] < currE.size()) {
                    Edge e = currE.get(currentEdge[curr]);
                    predecessor[e.trg] = e;
                    curr = e.trg;

                    if (e.trg == t) {
                        flow += augment(s, t);
                        curr = s;
                    }
                } else {
                    currentEdge[curr] = 0;
                    curr = retreat(curr, s);
                    if (curr < 0) {
                        return flow;
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
