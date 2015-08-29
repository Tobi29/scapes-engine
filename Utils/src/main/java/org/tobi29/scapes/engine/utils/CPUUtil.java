package org.tobi29.scapes.engine.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CPUUtil {
    private static final ThreadMXBean THREADS =
            ManagementFactory.getThreadMXBean();

    private CPUUtil() {
    }

    public static Reader reader() {
        return new Reader();
    }

    public static class Reader {
        private Map<Long, Long> lastThreadTimes = new ConcurrentHashMap<>();
        private long lastTime;

        public double totalCPU() {
            return totalCPU(THREADS.getAllThreadIds());
        }

        public double totalCPU(long[] threads) {
            double cpu = 0.0;
            if (THREADS.isThreadCpuTimeSupported()) {
                if (!THREADS.isThreadCpuTimeEnabled()) {
                    THREADS.setThreadCpuTimeEnabled(true);
                }
                long time = System.nanoTime();
                double delta = time - lastTime;
                Map<Long, Long> threadTimes;
                if (threads.length >= lastThreadTimes.size()) {
                    threadTimes = lastThreadTimes;
                } else {
                    threadTimes = new ConcurrentHashMap<>();
                }
                for (long thread : threads) {
                    long threadTime = THREADS.getThreadCpuTime(thread);
                    double threadDelta = threadTime -
                            lastThreadTimes.getOrDefault(thread, 0L);
                    threadTimes.put(thread, threadTime);
                    threadDelta /= delta;
                    cpu += threadDelta;
                }
                lastTime = time;
            } else {
                cpu = Double.NaN;
            }
            return cpu;
        }
    }
}
