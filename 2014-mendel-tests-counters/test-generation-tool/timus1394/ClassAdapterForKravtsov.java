package timus1394;

import java.io.File;
import java.lang.invoke.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * This adapter assumes that the class it is created atop
 * has a default public constructor, a public method <tt>solve</tt>
 * which takes two arguments of the type <pre>java.util.List<Integer></pre>
 * and returns void, as well as a public static method
 * <tt>profilerCleanup</tt> with no arguments returning void
 * and a public static method <tt>profilerData</tt> with no arguments
 * returning <pre>java.lang.Map<String, Long></pre>.
 *
 * The map returned by the <tt>profilerData</tt> method
 * should contain the criteria values.
 * The set of criteria MUST always be the same.
 *
 * @author Maxim Buzdalov
 */
public final class ClassAdapterForKravtsov implements Adapter {
    private final MethodHandle constructor;
    private final MethodHandle solve;
    private final MethodHandle cleanup;
    private final MethodHandle data;
    private final Set<String> keys;
    private final String name;

    public ClassAdapterForKravtsov(String jarPath, String className) throws Exception {
        try (URLClassLoader classLoader = new URLClassLoader(new URL[] { new File(jarPath).toURI().toURL() }, this.getClass().getClassLoader())) {
            Class<?> clazz = classLoader.loadClass(className);
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            name = className;
            constructor = lookup.findConstructor(clazz, MethodType.methodType(Void.TYPE));
            solve = lookup.findVirtual(clazz, "solve", MethodType.methodType(Void.TYPE, List.class, List.class));
            cleanup = lookup.findStatic(clazz, "profilerCleanup", MethodType.methodType(Void.TYPE));
            data = lookup.findStatic(clazz, "profilerData", MethodType.methodType(Map.class));
            keys = Collections.unmodifiableSet(invoke(Arrays.asList(1, 1, 1), Arrays.asList(1, 2), 1000).keySet());
        }
    }

    @Override
    public Map<String, Long> invoke(List<Integer> ships, List<Integer> havens, long timeLimit) {
        TimeoutChecker.setTimeLimit(timeLimit);
        try {
            Object object = constructor.invoke();
            cleanup.invoke();
            solve.invoke(object, ships, havens);
            @SuppressWarnings("unchecked")
            Map<String, Long> rv = (Map<String, Long>) (data.invoke());
            rv.put("time", TimeoutChecker.getTimeConsumed());
            return rv;
        } catch (TimeoutChecker.TimeLimitExceededException ex) {
            Map<String, Long> rv = new HashMap<>();
            for (String key : keys) {
                rv.put(key, Long.MAX_VALUE);
            }
            return rv;
        } catch (Throwable th) {
            th.printStackTrace();
            return Collections.emptyMap();
        } finally {
            TimeoutChecker.clearTimeLimit();
        }
    }

    @Override
    public Set<String> keys() {
        return keys;
    }

    @Override
    public String name() {
        return name;
    }
}
