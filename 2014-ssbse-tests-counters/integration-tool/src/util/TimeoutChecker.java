package util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * This class is a gate to a very simple method of time-limited
 * invocation. Each target needs to call
 * the {@link TimeoutChecker#check()}
 * method in its loops. The method either returns <tt>true</tt>
 * or throws a {@link TimeLimitExceededException}.
 */
public final class TimeoutChecker {
    public static class TimeLimitExceededException extends RuntimeException {
        private static final long serialVersionUID = -4742197833110866292L;
    }

    private static final ThreadLocal<Long> expectedFinishTime = new ThreadLocal<>();
    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();
    private static final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
    static {
        bean.setThreadCpuTimeEnabled(true);
    }

    public static void check() {
        Long expected = expectedFinishTime.get();
        if (expected == null) {
            return;
        }
        long current = bean.getCurrentThreadUserTime();
        long started = startTime.get();
        if (current > expected || started < expected && current < started) {
            throw new TimeLimitExceededException();
        }
    }

    public static long getTimeConsumed() {
        long to = bean.getCurrentThreadUserTime();
        long from = startTime.get();
        long rv = (from <= to) ? to - from : (Long.MAX_VALUE - from) + (to - Long.MIN_VALUE) + 1;
        return Math.round(rv / 1000000.0);
    }

    public static void clearTimeLimit() {
        startTime.remove();
        expectedFinishTime.remove();
    }

    public static void setTimeLimit(long timeLimitMS) {
        long current = bean.getCurrentThreadUserTime();
        startTime.set(current);
        if (timeLimitMS != 0) {
            expectedFinishTime.set(current + timeLimitMS * 1000000);
        }
    }
}
