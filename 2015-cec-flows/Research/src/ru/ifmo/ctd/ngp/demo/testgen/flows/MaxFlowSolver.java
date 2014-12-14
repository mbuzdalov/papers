package ru.ifmo.ctd.ngp.demo.testgen.flows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import ru.ifmo.ctd.ngp.demo.testgen.TimeoutChecker;
import ru.ifmo.ctd.ngp.demo.testgen.flows.solvers.util.BasicGraph;

/**
 * A base class for solvers of the maximum flow problem.
 *
 * @author Maxim Buzdalov
 */
public abstract class MaxFlowSolver {
    private final String name;
    public static final String TIME_KEY = "time";
    public static final String WC_TIME_KEY = "wcTime";
    public static final String ANSWER_KEY = "answer";

    private Set<String> possibleKeys = new HashSet<>();

    protected MaxFlowSolver() {
        this.name = this.getClass().getSimpleName();
    }

    public synchronized Set<String> getPossibleKeys() {
        if (possibleKeys.isEmpty()) {
            Map<String, Long> testMap = solveImpl(Arrays.asList(new EdgeRec(0, 1, 1)), 0, 1);
            possibleKeys.addAll(testMap.keySet());
            possibleKeys.add(TIME_KEY);
        }
        return possibleKeys;
    }

    private static final Comparator<EdgeRec> cmpEdges = new Comparator<EdgeRec>() {
        @Override
        public int compare(EdgeRec o1, EdgeRec o2) {
            int ds = o1.source() - o2.source();
            if (ds != 0) return ds;
            return o1.target() - o2.target();
        }
    };

    private List<EdgeRec> mergeEdges(Iterable<EdgeRec> source) {
        List<EdgeRec> rv = new ArrayList<>();
        for (EdgeRec e : source) {
            rv.add(e);
        }
        Collections.sort(rv, cmpEdges);
        int t = 0;
        int sum = 0;
        for (int s = 0; s < rv.size(); ++s) {
            EdgeRec se = rv.get(s);
            EdgeRec te = rv.get(t);
            if (cmpEdges.compare(se, te) == 0) {
                sum += se.capacity();
            } else {
                if (te.capacity() != sum) {
                    rv.set(t, new EdgeRec(te.source(), te.target(), sum));
                }
                ++t;
                rv.set(t, se);
                sum = se.capacity();
            }
        }
        EdgeRec te = rv.get(t);
        if (te.capacity() != sum) {
            rv.set(t, new EdgeRec(te.source(), te.target(), sum));
        }
        ++t;
        while (rv.size() > t) {
            rv.remove(rv.size() - 1);
        }
        return rv;
    }

    public final Map<String, Long> solve(Iterable<EdgeRec> edges, int source, int target, long timeLimit) {
        TimeoutChecker.setTimeLimit(timeLimit);
        try {
            boolean hasSource = false, hasTarget = false;
            for (EdgeRec e : edges) {
                hasSource |= e.source() == source;
                hasTarget |= e.target() == target;
            }
            if (!hasSource || !hasTarget) {
                Map<String, Long> rv = new HashMap<>();
                for (String s : getPossibleKeys()) {
                    rv.put(s, 0L);
                }
                return rv;
            } else {
                Map<String, Long> answer = solveImpl(mergeEdges(edges), source, target);
                TimeoutChecker.check();
                long consumed = TimeoutChecker.getTimeConsumed();
                answer.put(TIME_KEY, consumed);
                return answer;
            }
        } catch (TimeoutChecker.TimeLimitExceededException ex) {
            Map<String, Long> rv = new HashMap<>();
            for (String s : getPossibleKeys()) {
                rv.put(s, Long.MAX_VALUE);
            }
            return rv;
        } finally {
             TimeoutChecker.clearTimeLimit();
        }
    }

    public final Map<String, Long> solveDimacs(File file, long timeLimit) throws IOException {
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            List<EdgeRec> edges = new ArrayList<>();
            int source = -1;
            int target = -1;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("n")) {
                    String[] tokens = tokenize(line);
                    switch (tokens[2]) {
                        case "s": source = Integer.parseInt(tokens[1]) - 1; break;
                        case "t": target = Integer.parseInt(tokens[1]) - 1; break;
                    }
                } else if (line.startsWith("a")) {
                    String[] tokens = tokenize(line);
                    edges.add(new EdgeRec(
                            Integer.parseInt(tokens[1]) - 1,
                            Integer.parseInt(tokens[2]) - 1,
                            Integer.parseInt(tokens[3])
                    ));
                }
            }
            if (source == -1) {
                throw new IOException("source is undefined");
            }
            if (target == -1) {
                throw new IOException("target is undefined");
            }
            return solve(edges, source, target, timeLimit);
        }
    }

    private static String[] tokenize(String s) {
        StringTokenizer st = new StringTokenizer(s);
        String[] rv = new String[st.countTokens()];
        for (int i = 0; i < rv.length; ++i) {
            rv[i] = st.nextToken();
        }
        return rv;
    }

    public final String getName() {
        return name;
    }

    protected abstract BasicGraph constructFromEdges(Iterable<EdgeRec> edges);

    protected final Map<String, Long> solveImpl(Iterable<EdgeRec> edges, int source, int target) {
        long t0 = System.nanoTime();
        BasicGraph g = constructFromEdges(edges);
        long answer = g.maxFlow(source, target);
        long wcTime = System.nanoTime() - t0;
        Map<String, Long> rv = g.collectMeasures();
        rv.put(ANSWER_KEY, answer);
        rv.put(WC_TIME_KEY, wcTime);
        return rv;
    }
}
