package timus1394;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adapter from a 1394 solution to a more suitable interface.
 *
 * @author Maxim Buzdalov
 */
public interface Adapter {
    /**
     * Invokes the solution on the given problem instance and the given time limit.
     * Returns a map from criterion name to criterion value of type Long.
     *
     * If the execution ended without runtime errors,
     * the returned map is guaranteed to contain the key "time", which is the running
     * time, in milliseconds, with the platform allowed precision. In addition,
     * in this situation, the key set of the returned map is guaranteed to be always the same.
     *
     * If the execution's time limit is exceeded, the returned map may contain all the necessary
     * keys mapped to the values of <pre>Long.MAX_VALUE</pre>.
     *
     * If the execution ended with a runtime error, an empty map is returned.
     *
     * @param ships the list of ship lengths.
     * @param havens the list of haven lengths.
     * @param timeLimit the time limit, in milliseconds.
     * @return the map from criterion names to values.
     */
    public Map<String, Long> invoke(List<Integer> ships, List<Integer> havens, long timeLimit);

    /**
     * Returns a set of keys that will be returned by #invoke.
     * @return the set of keys.
     */
    public Set<String> keys();

    /**
     * Returns the name of this adapter.
     * @return the name of the adapter.
     */
    public String name();
}
