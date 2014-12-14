package ru.ifmo.ctd.ngp.demo.testgen;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * This class is a gate to a very simple method of time-limited
 * invocation. Each target needs to call
 * the {@link TimeoutChecker#check()}
 * method in its loops. The method either returns <tt>true</tt>
 * or throws a {@link TimeLimitExceededException}.
 *
 * @author Maxim Buzdalov
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
        //For thread local time, wrapping is very unlikely (Long.MAX_VALUE / 10^9 seconds is 292 years)
        //We could nevertheless consider this case.
        //However, Java's thread userTime on Linux SMP machines can accidentally jump back
        //when migrated to a different core.
        if (current > expected) {
            throw new TimeLimitExceededException();
        }
    }

    public static long checkOrGetTimeConsumed() {
        Long from = startTime.get();
        if (from == null) {
            throw new IllegalStateException("getTimeConsumed invoked not after setTimeLimit");
        }
        long current = bean.getCurrentThreadUserTime();
        long rv = (from <= current) ? current - from : (Long.MAX_VALUE - from) + (current - Long.MIN_VALUE) + 1;
        Long expected = expectedFinishTime.get();
        if (expected == null || current <= expected) {
            return Math.round(rv / 1000000.0);
        }
        throw new TimeLimitExceededException();
    }

    public static long getTimeConsumed() {
        long to = bean.getCurrentThreadUserTime();
        if (startTime.get() == null) {
            throw new IllegalStateException("getTimeConsumed invoked not after setTimeLimit");
        }
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
